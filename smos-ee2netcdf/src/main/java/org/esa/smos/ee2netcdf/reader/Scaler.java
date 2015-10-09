package org.esa.smos.ee2netcdf.reader;


public interface Scaler {

    double scale(double rawValue);
}
