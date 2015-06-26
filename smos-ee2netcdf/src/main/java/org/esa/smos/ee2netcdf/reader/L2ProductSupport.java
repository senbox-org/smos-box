package org.esa.smos.ee2netcdf.reader;

class L2ProductSupport implements ProductTypeSupport {

    @Override
    public String getLatitudeBandName() {
        return "Latitude";
    }

    @Override
    public String getLongitudeBandName() {
        return "Longitude";
    }
}
