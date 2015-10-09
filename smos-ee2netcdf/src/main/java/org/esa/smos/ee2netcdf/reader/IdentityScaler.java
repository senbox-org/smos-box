package org.esa.smos.ee2netcdf.reader;

class IdentityScaler implements Scaler {

    private final double fillValue;

    IdentityScaler(double fillValue) {
        this.fillValue = fillValue;
    }

    @Override
    public double scale(double rawValue) {
        return rawValue;
    }

    @Override
    public boolean isValid(double rawValue) {
        return fillValue != rawValue;
    }
}
