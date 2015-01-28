package org.esa.beam.dataio.smos.bufr;

import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;
import ucar.nc2.iosp.bufr.BufrIosp;
import ucar.nc2.iosp.bufr.SmosBufrIosp;
import ucar.unidata.io.InMemoryRandomAccessFile;
import ucar.unidata.io.RandomAccessFile;
import ucar.unidata.io.bzip2.CBZip2InputStream;

import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a SMOS BUFR or Light-BUFR file.
 *
 * @author Ralf Quast
 */
public class SmosBufrFile extends NetcdfFile implements Closeable {

    public static final String AZIMUTH_ANGLE = "Azimuth_angle";
    public static final String BRIGHTNESS_TEMPERATURE_IMAGINARY_PART = "Brightness_temperature_imaginary_part";
    public static final String BRIGHTNESS_TEMPERATURE_REAL_PART = "Brightness_temperature_real_part";
    public static final String DIRECT_SUN_BRIGHTNESS_TEMPERATURE = "Direct_sun_brightness_temperature";
    public static final String FARADAY_ROTATIONAL_ANGLE = "Faraday_rotational_angle";
    public static final String FOOTPRINT_AXIS_1 = "Footprint_axis_1";
    public static final String FOOTPRINT_AXIS_2 = "Footprint_axis_2";
    public static final String GEOMETRIC_ROTATIONAL_ANGLE = "Geometric_rotational_angle";
    public static final String GRID_POINT_IDENTIFIER = "Grid_point_identifier";
    public static final String INCIDENCE_ANGLE = "Incidence_angle";
    public static final String LATITUDE_HIGH_ACCURACY = "Latitude_high_accuracy";
    public static final String LONGITUDE_HIGH_ACCURACY = "Longitude_high_accuracy";
    public static final String NUMBER_OF_GRID_POINTS = "Number_of_grid_points";
    public static final String PIXEL_RADIOMETRIC_ACCURACY = "Pixel_radiometric_accuracy";
    public static final String POLARISATION = "Polarisation";
    public static final String RADIOMETRIC_ACCURACY_CP = "Radiometric_accuracy_cross_polarisation";
    public static final String RADIOMETRIC_ACCURACY_PP = "Radiometric_accuracy_pure_polarisation";
    public static final String SMOS_INFORMATION_FLAG = "SMOS_information_flag";
    public static final String SNAPSHOT_ACCURACY = "Snapshot_accuracy";
    public static final String SNAPSHOT_OVERALL_QUALITY = "Snapshot_overall_quality";
    public static final String SNAPSHOT_IDENTIFIER = "Snapshot_identifier";
    public static final String TOTAL_ELECTRON_COUNT = "Total_electron_count_per_square_metre";
    public static final String WATER_FRACTION = "Water_fraction";

    private static final String ATTR_NAME_ADD_OFFSET = "add_offset";
    private static final String ATTR_NAME_SCALE_FACTOR = "scale_factor";
    private static final String ATTR_NAME_MISSING_VALUE = "missing_value";

    private final SmosBufrIosp instance;
    private final Map<String, ValueDecoder> valueDecoderMap;

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

    private static RandomAccessFile createRandomAccessFile(String location) throws IOException {
        final File file = new File(location);
        final String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".bz2")) {
            final InputStream inputStream = createInputStream(file);
            final ImageInputStream imageInputStream = createImageInputStream(inputStream);
            final long imageInputStreamSize = getLength(imageInputStream);

            return new ImageInputStreamRandomAccessFile(imageInputStream, imageInputStreamSize);
        }

        return new RandomAccessFile(location, "r");
    }

    // not private for purpose of testing only
    static ImageInputStream createImageInputStream(InputStream inputStream) throws IOException {
        return new FileCacheImageInputStream(inputStream, null);
    }

    // not private for purpose of testing only
    static InputStream createInputStream(File file) throws IOException {
        return new CBZip2InputStream(new BufferedInputStream(new FileInputStream(file)), true);
    }

    // not private for purpose of testing only
    static long getLength(ImageInputStream imageInputStream) throws IOException {
        final byte[] b = new byte[16384];

        long length = 0;
        while (true) {
            final int count = imageInputStream.read(b, 0, b.length);
            if (count == -1) {
                break;
            }
            length += count;
        }

        return length;
    }

    private SmosBufrFile(SmosBufrIosp iosp, String location) throws IOException {
        super(iosp, location);
        instance = iosp;
        final RandomAccessFile randomAccessFile = createRandomAccessFile(location);
        instance.open(randomAccessFile, this, null);

        valueDecoderMap = new HashMap<>();

        addValueDecoder(AZIMUTH_ANGLE);
        addValueDecoder(BRIGHTNESS_TEMPERATURE_IMAGINARY_PART);
        addValueDecoder(BRIGHTNESS_TEMPERATURE_REAL_PART);
        addValueDecoder(DIRECT_SUN_BRIGHTNESS_TEMPERATURE);
        addValueDecoder(FARADAY_ROTATIONAL_ANGLE);
        addValueDecoder(FOOTPRINT_AXIS_1);
        addValueDecoder(FOOTPRINT_AXIS_2);
        addValueDecoder(GEOMETRIC_ROTATIONAL_ANGLE);
        addValueDecoder(GRID_POINT_IDENTIFIER);
        addValueDecoder(INCIDENCE_ANGLE);
        addValueDecoder(LATITUDE_HIGH_ACCURACY);
        addValueDecoder(LONGITUDE_HIGH_ACCURACY);
        addValueDecoder(NUMBER_OF_GRID_POINTS);
        addValueDecoder(PIXEL_RADIOMETRIC_ACCURACY);
        addValueDecoder(POLARISATION);
        addValueDecoder(RADIOMETRIC_ACCURACY_CP);
        addValueDecoder(RADIOMETRIC_ACCURACY_PP);
        addValueDecoder(SMOS_INFORMATION_FLAG);
        addValueDecoder(SNAPSHOT_ACCURACY);
        addValueDecoder(SNAPSHOT_OVERALL_QUALITY);
        addValueDecoder(SNAPSHOT_IDENTIFIER);
        addValueDecoder(TOTAL_ELECTRON_COUNT);
        addValueDecoder(WATER_FRACTION);
    }

    // not private for purpose of testing only
    static double getAttributeValue(Variable variable, String attributeName, double defaultValue) {
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().doubleValue();
    }

    // not private for purpose of testing only
    static Number getAttributeValue(Variable variable, String attributeName) {
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute == null) {
            return null;
        }
        return attribute.getNumericValue();
    }

    // not private for purpose of testing only
    static ValueDecoder createValueDecoder(Variable variable) {
        final double scale = getAttributeValue(variable, ATTR_NAME_SCALE_FACTOR, 1.0);
        final double offset = getAttributeValue(variable, ATTR_NAME_ADD_OFFSET, 0.0);
        final Number missingValue = getAttributeValue(variable, ATTR_NAME_MISSING_VALUE);

        return new ValueDecoder(scale, offset, missingValue);
    }

    ValueDecoder getValueDecoder(String variableName) {
        return valueDecoderMap.get(variableName);
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

    private ValueDecoder createValueDecoder(String variableName) {
        return createValueDecoder(getObservationStructure().findVariable(variableName));
    }

    private void addValueDecoder(String variableName) {
        valueDecoderMap.put(variableName, createValueDecoder(variableName));
    }

    private final static class ImageInputStreamRandomAccessFile extends RandomAccessFile {

        private final ImageInputStream imageInputStream;
        private final long length;

        static RandomAccessFile create(ImageInputStream imageInputStream, long length) throws IOException {
            final byte[] b = new byte[(int) length];
            imageInputStream.readFully(b);

            return new InMemoryRandomAccessFile("BUFR", b);
        }

        private ImageInputStreamRandomAccessFile(ImageInputStream imageInputStream, long length) {
            super(8192);
            this.imageInputStream = imageInputStream;
            this.length = length;
        }

        @Override
        public void setBufferSize(int bufferSize) {
            // do nothing
        }

        @Override
        public String getLocation() {
            return "ImageInputStream";
        }

        @Override
        public long length() throws IOException {
            return length;
        }

        @Override
        protected int read_(long pos, byte[] b, int offset, int len) throws IOException {
            imageInputStream.seek(pos);
            return imageInputStream.read(b, offset, len);
        }

        @Override
        public long readToByteChannel(WritableByteChannel dest, long offset, long nbytes) throws IOException {
            final int n = (int) nbytes;
            final byte[] buffer = new byte[n];
            final int done = read_(offset, buffer, 0, n);

            dest.write(ByteBuffer.wrap(buffer));

            return done;
        }

        @Override
        public void close() throws IOException {
            imageInputStream.close();
        }
    }

}
