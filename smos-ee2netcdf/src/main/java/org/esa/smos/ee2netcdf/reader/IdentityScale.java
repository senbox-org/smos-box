package org.esa.smos.ee2netcdf.reader;

class IdentityScale implements Scale{

    @Override
    public double scale(double rawValue) {
        return rawValue;
    }
}
