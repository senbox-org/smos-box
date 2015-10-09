package org.esa.smos.ee2netcdf.reader;

class LinearScaler implements Scaler {

    private final double scale;
    private final double offset;
    private final double fillValue;

    LinearScaler(double scale, double offset, double fillValue) {
        this.scale = scale;
        this.offset = offset;
        this.fillValue = fillValue;
    }

    public double scale(double value) {
        return scale * value + offset;
    }

    @Override
    public boolean isValid(double rawValue) {
        return rawValue != fillValue;
    }
}
