package org.esa.smos.ee2netcdf.reader;

import ucar.nc2.NetcdfFile;

class ScienceProductSupport extends AbstractProductTypeSupport {

    ScienceProductSupport(NetcdfFile netcdfFile) {
        super(netcdfFile);
    }

    @Override
    public String getLatitudeBandName() {
        return "Grid_Point_Latitude";
    }

    @Override
    public String getLongitudeBandName() {
        return "Grid_Point_Longitude";
    }
}
