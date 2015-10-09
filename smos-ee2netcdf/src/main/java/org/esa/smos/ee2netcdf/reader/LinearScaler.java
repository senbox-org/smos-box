package org.esa.smos.ee2netcdf.reader;

class LinearScaler implements Scaler {

    private final double scale;
    private final double offset;

    LinearScaler(double scale, double offset) {
        this.scale = scale;
        this.offset = offset;
    }

    public double scale(double value) {
        return scale * value + offset;
    }
}
