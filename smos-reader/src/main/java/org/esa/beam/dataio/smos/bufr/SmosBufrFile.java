package org.esa.beam.dataio.smos.bufr;

import ucar.ma2.StructureDataIterator;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;
import ucar.nc2.iosp.bufr.BufrIosp;
import ucar.nc2.iosp.bufr.SmosBufrIosp;
import ucar.unidata.io.RandomAccessFile;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a SMOS BUFR or Light-BUFR file.
 *
 * @author Ralf Quast
 */
public class SmosBufrFile extends NetcdfFile implements Closeable {

    private final SmosBufrIosp instance;

    /**
     * Opens an existing BUFR file.
     *
     * @param location The location of the BUFR file.
     *
     * @return the BUFR file.
     *
     * @throws IOException if an error occurred.
     */
    public static SmosBufrFile open(String location) throws IOException {
        return new SmosBufrFile(new SmosBufrIosp(), location);
    }

    private SmosBufrFile(SmosBufrIosp iosp, String location) throws IOException {
        super(iosp, location);
        instance = iosp;
        final RandomAccessFile randomAccessFile = new RandomAccessFile(location, "r");
        instance.open(randomAccessFile, this, null);
    }

    /**
     * Returns the number of BUFR messages in the BUFR file.
     *
     * @return the number of messages.
     */
    public int getMessageCount() {
        return instance.getMessageCount();
    }

    /**
     * Returns an iterator for the data within a BUFFR message.
     *
     * @param messageIndex The index of the BUFR message.
     *
     * @return the Iterator.
     *
     * @throws IOException if an error occurred.
     */
    public StructureDataIterator getStructureIterator(int messageIndex) throws IOException {
        return instance.getStructureDataIterator(messageIndex);
    }

    /**
     * Returns the observation structure.
     *
     * @return the observation structure.
     */
    public Sequence getObservationStructure() {
        final Variable obs = findVariable(BufrIosp.obsRecord);
        if (obs instanceof Sequence) {
            return (Sequence) obs;
        } else { // cannot happen, unless the UCAR BUFR IOSP implementation has changed
            throw new RuntimeException("BUFR file does not contain an observation sequence.");
        }
    }
}
