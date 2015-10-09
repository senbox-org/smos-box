package org.esa.smos.ee2netcdf.reader;

class IdentityScaler implements Scaler {

    @Override
    public double scale(double rawValue) {
        return rawValue;
    }
}
