package org.esa.beam.dataio.smos.bufr;


class ScaleFactor {

    private final double scale;
    private final double offset;
    private final int missingValue;

    ScaleFactor(double scale, double offset, int missingValue) {
        this.scale = scale;
        this.offset = offset;
        this.missingValue = missingValue;
    }

    double scale(int rawValue) {
        if (rawValue == missingValue) {
            return Double.NaN;
        }
        return scale * rawValue + offset;
    }

    boolean isValid(int rawValue) {
        return rawValue != missingValue;
    }
}
