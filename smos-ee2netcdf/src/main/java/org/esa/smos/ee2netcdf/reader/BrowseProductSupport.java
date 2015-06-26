package org.esa.smos.ee2netcdf.reader;

class BrowseProductSupport implements ProductTypeSupport{

    @Override
    public String getLatitudeBandName() {
        return "Grid_Point_Latitude";
    }

    @Override
    public String getLongitudeBandName() {
        return "Grid_Point_Longitude";
    }
}
