package org.esa.beam.dataio.smos.bufr;

import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
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

    static double getAttributeValue(Variable variable, String attributeName, double defaultValue) {
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().doubleValue();
    }

    static Number getAttributeValue(Variable variable, String attributeName) {
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute == null) {
            return null;
        }
        return attribute.getNumericValue();
    }

    static ValueDecoder createValueDecoder(Variable variable) {
        final double scale = getAttributeValue(variable, ATTR_NAME_SCALE_FACTOR, 1.0);
        final double offset = getAttributeValue(variable, ATTR_NAME_ADD_OFFSET, 0.0);
        final Number missingValue = getAttributeValue(variable, ATTR_NAME_MISSING_VALUE);

        return new ValueDecoder(scale, offset, missingValue);
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
