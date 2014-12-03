package org.esa.beam.dataio.smos.bufr;

import ucar.ma2.DataType;
import ucar.ma2.StructureData;

class ValueAccessors {

    // @todo 2 tb/tb add test 2014-12-03
    static ValueAccessor get(String datasetName, ValueDecoder valueDecoder) {
        if (datasetName.equalsIgnoreCase(SmosBufrFile.AZIMUTH_ANGLE) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.FARADAY_ROTATIONAL_ANGLE) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.GEOMETRIC_ROTATIONAL_ANGLE) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.INCIDENCE_ANGLE)) {
            return new IntValueAccessor(datasetName, valueDecoder);
        } else if (datasetName.equalsIgnoreCase(SmosBufrFile.BRIGHTNESS_TEMPERATURE_REAL_PART) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.BRIGHTNESS_TEMPERATURE_IMAGINARY_PART)) {
            return new UnsignedShortValueAccessor(datasetName, valueDecoder);
        } else if (datasetName.equalsIgnoreCase(SmosBufrFile.FOOTPRINT_AXIS_1) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.FOOTPRINT_AXIS_2) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.PIXEL_RADIOMETRIC_ACCURACY) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.WATER_FRACTION) ||
                datasetName.equalsIgnoreCase(SmosBufrFile.SMOS_INFORMATION_FLAG)) {
            return new ShortValueAccessor(datasetName, valueDecoder);
        } else if (datasetName.equalsIgnoreCase(SmosBufrFile.POLARISATION)) {
            return new ByteValueAccessor(datasetName, valueDecoder);
        }

        throw new IllegalStateException("unsupported dataset: " + datasetName);
    }

    private static class IntValueAccessor implements ValueAccessor {

        private final String datasetName;
        private final ValueDecoder valueDecoder;

        private int gridPointId;
        private int rawValue;

        private IntValueAccessor(String datasetName, ValueDecoder valueDecoder) {
            this.datasetName = datasetName;
            this.valueDecoder = valueDecoder;
        }

        @Override
        public void read(StructureData snapshotData) {
            rawValue = snapshotData.getScalarInt(datasetName);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
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

    private static class UnsignedShortValueAccessor implements ValueAccessor {

        private final String datasetName;
        private final ValueDecoder valueDecoder;

        private int gridPointId;
        private int rawValue;

        private UnsignedShortValueAccessor(String datasetName, ValueDecoder valueDecoder) {
            this.datasetName = datasetName;
            this.valueDecoder = valueDecoder;
        }

        @Override
        public void read(StructureData snapshotData) {
            final short shortRawValue = snapshotData.getScalarShort(datasetName);
            rawValue = DataType.unsignedShortToInt(shortRawValue);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
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

    private static class ShortValueAccessor implements ValueAccessor {

        private final String datasetName;
        private final ValueDecoder valueDecoder;

        private int gridPointId;
        private int rawValue;

        private ShortValueAccessor(String datasetName, ValueDecoder valueDecoder) {
            this.datasetName = datasetName;
            this.valueDecoder = valueDecoder;
        }

        @Override
        public void read(StructureData snapshotData) {
            rawValue = snapshotData.getScalarShort(datasetName);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
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

    private static class ByteValueAccessor implements ValueAccessor {

        private final String datasetName;
        private final ValueDecoder valueDecoder;

        private int gridPointId;
        private int rawValue;

        private ByteValueAccessor(String datasetName, ValueDecoder valueDecoder) {
            this.datasetName = datasetName;
            this.valueDecoder = valueDecoder;
        }

        @Override
        public void read(StructureData snapshotData) {
            rawValue = snapshotData.getScalarByte(datasetName);

            gridPointId = snapshotData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
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
