package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.geom.Area;

class BrowseProductSupport extends AbstractProductTypeSupport {

    BrowseProductSupport(NetcdfFile netcdfFile) {
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

    @Override
    public ValueProvider createValueProvider(Variable variable, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo) {
        final int polarization = descriptor.getPolarization();
        if (polarization < 0) {
            return new VariableValueProvider(variable, area, gridPointInfo);
        } else {
            return new BrowseValueProvider(variable, polarization, area, gridPointInfo);
        }
    }
}
