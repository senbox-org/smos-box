package org.esa.beam.dataio.smos.bufr;


class ValueDecoder {

    private final double scaleFactor;
    private final double offset;
    private final Number missingValue;

    ValueDecoder(double scaleFactor, double offset, Number missingValue) {
        this.scaleFactor = scaleFactor;
        this.offset = offset;
        this.missingValue = missingValue;
    }

    double decode(int value) {
        if (isValid(value)) {
            return scaleFactor * value + offset;
        }
        return Double.NaN;
    }

    boolean isValid(int value) {
        return missingValue == null || value != missingValue.intValue();
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public double getOffset() {
        return offset;
    }

    public Number getMissingValue() {
        return missingValue;
    }
}
