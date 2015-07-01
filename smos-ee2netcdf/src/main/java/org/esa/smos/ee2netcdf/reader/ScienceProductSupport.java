package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.SmosUtils;
import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.snap.framework.datamodel.Product;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.geom.Area;

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

    @Override
    public ValueProvider createValueProvider(Variable variable, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo) {
        return new VariableValueProvider(variable, area, gridPointInfo);
    }

    @Override
    public void createAdditionalBands(Product product, Family<BandDescriptor> bandDescriptors, String formatName) {
        if (SmosUtils.isDualPolScienceFormat(formatName)) {
            addRotatedDualPoleBands(product, bandDescriptors);
        } else {
            addRotatedFullPoleBands(product, bandDescriptors);
        }
    }

    private void addRotatedFullPoleBands(Product product, Family<BandDescriptor> bandDescriptors) {
    }

    private void addRotatedDualPoleBands(Product product, Family<BandDescriptor> bandDescriptors) {

    }
}
