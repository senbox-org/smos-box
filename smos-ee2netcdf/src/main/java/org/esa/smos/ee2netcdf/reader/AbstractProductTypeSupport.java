package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.framework.datamodel.Band;
import ucar.nc2.NetcdfFile;

abstract class AbstractProductTypeSupport implements ProductTypeSupport {

    AbstractProductTypeSupport(NetcdfFile netcdfFile) {
    }

    @Override
    public void setScalingAndOffset(Band band, BandDescriptor bandDescriptor) {
        band.setScalingOffset(bandDescriptor.getScalingOffset());
        band.setScalingFactor(bandDescriptor.getScalingFactor());
    }

    @Override
    public String restoreBandName(String ncBandName) {
        return ncBandName;
    }
}
