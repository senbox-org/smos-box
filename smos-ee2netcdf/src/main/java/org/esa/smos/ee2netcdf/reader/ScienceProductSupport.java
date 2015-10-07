package org.esa.smos.ee2netcdf.reader;

import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.smos.SmosUtils;
import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.ProductHelper;
import org.esa.smos.dataio.smos.SmosMultiLevelSource;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductData;
import ucar.nc2.NetcdfFile;

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
        return new InterpolatedValueProvider(arrayCache, variableName, descriptor.getPolarization(), area, gridPointInfo);
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
        BandDescriptor descriptor = bandDescriptors.getMember("BT_Value_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("BT_Value_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("BT_Value_HV_Real");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("BT_Value_HV_Imag");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_HV");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Stokes_1");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Stokes_2");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Stokes_3");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Stokes_4");
        addRotatedBand(product, descriptor, new FaradayValueProvider());
    }

    private void addRotatedDualPoleBands(Product product, Family<BandDescriptor> bandDescriptors) {
        BandDescriptor descriptor = bandDescriptors.getMember("BT_Value_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("BT_Value_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Stokes_1");
        addRotatedBand(product, descriptor, new FaradayValueProvider());

        descriptor = bandDescriptors.getMember("Stokes_2");
        addRotatedBand(product, descriptor, new FaradayValueProvider());
    }

    private void addRotatedBand(Product product, BandDescriptor descriptor, ValueProvider valueProvider) {
        if (!descriptor.isVisible()) {
            return;
        }
        final Band band = product.addBand(descriptor.getBandName(), ProductData.TYPE_FLOAT32);

        band.setUnit(descriptor.getUnit());
        band.setDescription(descriptor.getDescription());

        if (descriptor.hasFillValue()) {
            band.setNoDataValueUsed(true);
            band.setNoDataValue(descriptor.getFillValue());
        }
        final SmosMultiLevelSource smosMultiLevelSource = new SmosMultiLevelSource(band, valueProvider);
        final DefaultMultiLevelImage defaultMultiLevelImage = new DefaultMultiLevelImage(smosMultiLevelSource);
        band.setSourceImage(defaultMultiLevelImage);
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }
}
