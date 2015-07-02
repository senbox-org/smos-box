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
    public ValueProvider createValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo) {
        return new VariableValueProvider(arrayCache, variableName, area, gridPointInfo);
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
        bandDescriptors.getMember("BT_Value_H");
        bandDescriptors.getMember("BT_Value_V");
        bandDescriptors.getMember("BT_Value_HV_Real");
        bandDescriptors.getMember("BT_Value_HV_Imag");
        bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
        bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
        bandDescriptors.getMember("Pixel_Radiometric_Accuracy_HV");
        bandDescriptors.getMember("Stokes_1");
        bandDescriptors.getMember("Stokes_2");
        bandDescriptors.getMember("Stokes_3");
        bandDescriptors.getMember("Stokes_4");
    }

    private void addRotatedDualPoleBands(Product product, Family<BandDescriptor> bandDescriptors) {
        bandDescriptors.getMember("BT_Value_H");
        bandDescriptors.getMember("BT_Value_V");
        bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
        bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
        bandDescriptors.getMember("Stokes_1");
        bandDescriptors.getMember("Stokes_2");
    }
}
