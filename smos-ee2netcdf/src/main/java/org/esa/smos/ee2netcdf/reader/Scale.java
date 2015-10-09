package org.esa.smos.ee2netcdf.reader;


public interface Scale {

    double scale(double rawValue);
}
