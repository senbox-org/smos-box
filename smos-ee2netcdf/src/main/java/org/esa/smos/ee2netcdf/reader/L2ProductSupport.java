package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.snap.framework.datamodel.Band;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.awt.geom.Area;

class L2ProductSupport extends AbstractProductTypeSupport {

    private final double chi_2_scale;

    L2ProductSupport(NetcdfFile netcdfFile) {
        super(netcdfFile);

        final Attribute chi2ScaleAttribute = netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Chi_2_Scale");
        if (chi2ScaleAttribute != null) {
            chi_2_scale = Double.valueOf(chi2ScaleAttribute.getStringValue());
        } else {
            chi_2_scale = 1.0;
        }
    }

    @Override
    public String getLatitudeBandName() {
        return "Latitude";
    }

    @Override
    public String getLongitudeBandName() {
        return "Longitude";
    }

    @Override
    public void setScalingAndOffset(Band band, BandDescriptor bandDescriptor) {
        final String memberName = bandDescriptor.getMemberName();
        if ("Chi_2".equals(memberName)) {
            band.setScalingFactor(bandDescriptor.getScalingFactor() * chi_2_scale);
            band.setScalingOffset(bandDescriptor.getScalingOffset());
        } else {
            super.setScalingAndOffset(band, bandDescriptor);
        }
    }

    @Override
    public ValueProvider createValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo) {
        return new VariableValueProvider(arrayCache, variableName, area, gridPointInfo);
    }
}
