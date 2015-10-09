package org.esa.smos.ee2netcdf.reader;

class LinearScale implements Scale{

    private final double scale;
    private final double offset;

    LinearScale(double scale, double offset) {
        this.scale = scale;
        this.offset = offset;
    }

    public double scale(double value) {
        return scale * value + offset;
    }
}
