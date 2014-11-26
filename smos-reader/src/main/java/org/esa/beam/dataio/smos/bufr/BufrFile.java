package org.esa.beam.dataio.smos.bufr;

import ucar.ma2.StructureDataIterator;
import ucar.nc2.NetcdfFile;
import ucar.nc2.iosp.bufr.SmosBufrIosp;
import ucar.unidata.io.RandomAccessFile;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a SMOS BUFR or Light-BUFR file.
 *
 * @author Ralf Quast
 */
public class BufrFile extends NetcdfFile implements Closeable {

    private final SmosBufrIosp instance;

    public static BufrFile create(String location) throws IOException {
        return new BufrFile(new SmosBufrIosp(), location);
    }

    private BufrFile(SmosBufrIosp iosp, String location) throws IOException {
        super(iosp, location);
        instance = iosp;
        final RandomAccessFile randomAccessFile = new RandomAccessFile(location, "r");
        instance.open(randomAccessFile, this, null);
    }

    public int getMessageCount() {
        return instance.getMessageCount();
    }

    public StructureDataIterator getStructureIterator(int messageIndex) throws IOException {
        return instance.getStructureDataIterator(messageIndex);
    }

}
