package org.esa.smos.ee2netcdf.reader;


public interface Scaler {

    boolean isValid(double rawValue);

    double scale(double rawValue);
}
