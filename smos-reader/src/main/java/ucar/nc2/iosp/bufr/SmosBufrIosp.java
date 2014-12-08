/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.iosp.bufr;

import thredds.catalog.DataFormatType;
import ucar.ma2.Array;
import ucar.ma2.ArraySequence;
import ucar.ma2.ArrayStructure;
import ucar.ma2.ArrayStructureBB;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.ma2.StructureMembers;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

/**
 * An IO service provider for SMOS BUFR files.
 *
 * @author Ralf Quast
 */
public final class SmosBufrIosp extends AbstractIOServiceProvider {

    private final List<Message> messageList = new ArrayList<>();

    private Message prototypeMessage;
    private ConstructNC construct;
    private boolean wantTime;
    private int elementCount = -1;

    @Override
    public boolean isValidFile(ucar.unidata.io.RandomAccessFile raf) throws IOException {
        return MessageScanner.isValidFile(raf);
    }

    @Override
    public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
        this.raf = raf;

        final MessageScanner messageScanner = new MessageScanner(raf);
        while (messageScanner.hasNext()) {
            final Message m = messageScanner.next();
            if (m == null) {
                continue;
            }
            if (m.containsBufrTable()) {
                continue; // not data
            }

            if (prototypeMessage == null) {
                prototypeMessage = m;
                prototypeMessage.getRootDataDescriptor(); // construct the data descriptors, check for complete tables
                if (!prototypeMessage.isTablesComplete()) {
                    throw new IOException("Incomplete data tables in file = " + ncfile.getLocation());
                }
            } else {
                if (!prototypeMessage.equals(m)) {
                    continue; // skip
                }
            }

            messageList.add(m);
        }

        // count where the obs start in the messages
        int observationCount = 0;
        for (final Message m : messageList) {
            observationCount += m.getNumberDatasets();
        }

        if (prototypeMessage == null) {
            throw new IOException("No data messages in file = " + ncfile.getLocation());
        }
        construct = new ConstructNC(prototypeMessage, observationCount, ncfile);
        ncfile.finish();
    }

    @Override
    public Object sendIospMessage(Object message) {
        if (message instanceof String) {
            final String text = (String) message;
            if (text.equals("AddTime")) {
                wantTime = true;
                return true;
            }
        }
        return super.sendIospMessage(message);
    }

    @Override
    public Array readData(Variable v2, Section section) throws IOException, InvalidRangeException {
        final Structure s = construct.recordStructure;
        return new ArraySequence(s.makeStructureMembers(), new SeqIter(), elementCount);
    }

    private void addTime(ArrayStructure as) throws IOException {
        final int n = (int) as.getSize();
        final Array timeData = Array.factory(String.class, new int[]{n});
        final IndexIterator ii = timeData.getIndexIterator();

        if (as instanceof ArrayStructureBB) {
            final ArrayStructureBB asbb = (ArrayStructureBB) as;
            final StructureMembers.Member m = asbb.findMember(ConstructNC.TIME_NAME);
            final StructureDataIterator iter = as.getStructureDataIterator();
            try {
                int recno = 0;
                while (iter.hasNext()) {
                    CalendarDate cd = construct.makeObsTimeValue(iter.next());
                    asbb.addObjectToHeap(recno, m, cd.toString()); // add object into the Heap
                    recno++;
                }
            } finally {
                iter.finish();
            }

        } else {
            final StructureDataIterator iter = as.getStructureDataIterator();
            try {
                while (iter.hasNext()) {
                    final CalendarDate cd = construct.makeObsTimeValue(iter.next());
                    ii.setObjectNext(cd.toString());
                }
            } finally {
                iter.finish();
            }
            final StructureMembers.Member m = as.findMember(ConstructNC.TIME_NAME);
            m.setDataArray(timeData);
        }
    }

    @Override
    public StructureDataIterator getStructureIterator(Structure s, int bufferSize) throws java.io.IOException {
        return new SeqIter();
    }

    public StructureDataIterator getStructureDataIterator(int messageIndex) throws IOException {
        return getStructureDataIterator(messageList.get(messageIndex));
    }

    StructureDataIterator getStructureDataIterator(Message message) throws IOException {
        final ArrayStructure as;
        if (message.dds.isCompressed()) {
            final MessageCompressedDataReader reader = new MessageCompressedDataReader();
            as = reader.readEntireMessage(construct.recordStructure, prototypeMessage, message, raf, null);
        } else {
            final MessageUncompressedDataReader reader = new MessageUncompressedDataReader();
            as = reader.readEntireMessage(construct.recordStructure, prototypeMessage, message, raf, null);
        }
        if (wantTime && construct.isTimeOk()) {
            addTime(as);
        }
        return as.getStructureDataIterator();
    }

    public int getMessageCount() {
        return messageList.size();
    }

    private class SeqIter implements StructureDataIterator {

        final boolean addTime;
        Iterator<Message> messageIterator;
        StructureDataIterator structureDataIterator;
        int recordNumber = 0;
        int bufferSize = -1;

        private SeqIter() {
            addTime = false;
            reset();
        }

        @Override
        public StructureDataIterator reset() {
            recordNumber = 0;
            messageIterator = messageList.iterator();
            structureDataIterator = null;
            return this;
        }

        @Override
        public boolean hasNext() throws IOException {
            if (structureDataIterator == null) {
                structureDataIterator = readNextMessage();
                if (structureDataIterator == null) {
                    elementCount = recordNumber;
                    return false;
                }
            }

            if (!structureDataIterator.hasNext()) {
                structureDataIterator = readNextMessage();
                return hasNext();
            }

            return true;
        }

        @Override
        public StructureData next() throws IOException {
            recordNumber++;
            return structureDataIterator.next();
        }

        private StructureDataIterator readNextMessage() throws IOException {
            if (!messageIterator.hasNext()) {
                return null;
            }
            final Message m = messageIterator.next();
            return getStructureDataIterator(m);
        }

        @Override
        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        @Override
        public int getCurrentRecno() {
            return recordNumber - 1;
        }

        @Override
        public void finish() {
            if (structureDataIterator != null) {
                structureDataIterator.finish();
            }
            structureDataIterator = null;
        }
    }

    @Override
    public String getDetailInfo() {
        final Formatter formatter = new Formatter();
        formatter.format("%s", super.getDetailInfo());
        try {
            prototypeMessage.dump(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return formatter.toString();
    }

    @Override
    public String getFileTypeId() {
        return DataFormatType.BUFR.toString();
    }

    @Override
    public String getFileTypeDescription() {
        return "WMO Binary Universal Form";
    }

}
