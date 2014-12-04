package org.esa.beam.dataio.smos.bufr;

import ucar.ma2.DataType;
import ucar.ma2.StructureData;

class ValueAccessors {

    static ValueAccessor get(String datasetName) {
        if (datasetName.equalsIgnoreCase(SmosBufrFile.AZIMUTH_ANGLE) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.FARADAY_ROTATIONAL_ANGLE) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.GEOMETRIC_ROTATIONAL_ANGLE) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.INCIDENCE_ANGLE) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.DIRECT_SUN_BRIGHTNESS_TEMPERATURE)) {
            return new IntValueAccessor(datasetName);
        } else if (datasetName.equalsIgnoreCase(SmosBufrFile.BRIGHTNESS_TEMPERATURE_REAL_PART) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.BRIGHTNESS_TEMPERATURE_IMAGINARY_PART)) {
            return new UnsignedShortValueAccessor(datasetName);
        } else if (datasetName.equalsIgnoreCase(SmosBufrFile.FOOTPRINT_AXIS_1) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.FOOTPRINT_AXIS_2) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.PIXEL_RADIOMETRIC_ACCURACY) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.WATER_FRACTION) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.SMOS_INFORMATION_FLAG) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.NUMBER_OF_GRID_POINTS) ||
                datasetName.equalsIgnoreCase("Year") ||
                datasetName.equalsIgnoreCase(SmosBufrFile.SNAPSHOT_ACCURACY) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.RADIOMETRIC_ACCURACY_PP) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.RADIOMETRIC_ACCURACY_CP)
                ) {
            return new ShortValueAccessor(datasetName);
        } else if (datasetName.equalsIgnoreCase(SmosBufrFile.POLARISATION) ||
                datasetName.equalsIgnoreCase("Month") ||
                datasetName.equalsIgnoreCase("Day") ||
                datasetName.equalsIgnoreCase("Hour") ||
                datasetName.equalsIgnoreCase("Minute") ||
                datasetName.equalsIgnoreCase("Second") ||
                datasetName.equalsIgnoreCase(SmosBufrFile.TOTAL_ELECTRON_COUNT)
                ){
            return new ByteValueAccessor(datasetName);
        }

        throw new IllegalStateException("unsupported dataset: " + datasetName);
    }

    static class IntValueAccessor extends AbstractValueAccessor {

        IntValueAccessor(String datasetName) {
            super(datasetName);
        }

        @Override
        public void read(StructureData snapshotData) {
            rawValue = snapshotData.getScalarInt(datasetName);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        }
    }

    static class UnsignedShortValueAccessor extends AbstractValueAccessor {

        UnsignedShortValueAccessor(String datasetName) {
            super(datasetName);
        }

        @Override
        public void read(StructureData snapshotData) {
            final short shortRawValue = snapshotData.getScalarShort(datasetName);
            rawValue = DataType.unsignedShortToInt(shortRawValue);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        }
    }

    static class ShortValueAccessor extends AbstractValueAccessor {

        ShortValueAccessor(String datasetName) {
            super(datasetName);
        }

        @Override
        public void read(StructureData snapshotData) {
            rawValue = snapshotData.getScalarShort(datasetName);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        }
    }

    static class ByteValueAccessor extends AbstractValueAccessor {

        ByteValueAccessor(String datasetName) {
            super(datasetName);
        }

        @Override
        public void read(StructureData snapshotData) {
            rawValue = snapshotData.getScalarByte(datasetName);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        }
    }

    private abstract static class AbstractValueAccessor implements ValueAccessor {
        protected final String datasetName;

        protected int gridPointId;
        protected int rawValue;

        protected AbstractValueAccessor(String datasetName) {
            this.datasetName = datasetName;
        }

        @Override
        public int getGridPointId() {
            return gridPointId;
        }

        @Override
        public int getRawValue() {
            return rawValue;
        }
    }
}
