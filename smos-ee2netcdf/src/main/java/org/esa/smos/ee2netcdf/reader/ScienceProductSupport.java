package org.esa.smos.ee2netcdf.reader;

class ScienceProductSupport implements ProductTypeSupport{

    @Override
    public String getLatitudeBandName() {
        return "Grid_Point_Latitude";
    }

    @Override
    public String getLongitudeBandName() {
        return "Grid_Point_Longitude";
    }
}
