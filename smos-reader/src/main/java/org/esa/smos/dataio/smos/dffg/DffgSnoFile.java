package org.esa.smos.dataio.smos.dffg;

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.CellValueProvider;
import org.esa.smos.dataio.smos.ProductHelper;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ColorPaletteDef;
import org.esa.snap.core.datamodel.ImageInfo;
import org.esa.snap.core.datamodel.Product;

import java.awt.*;
import java.io.IOException;


public class DffgSnoFile extends DffgFile {

    private static final String DFFG_SNO_POINT_DATA_TYPE_NAME = "DFFG_Snow_Point_Data_Type";
    private static final String LIST_OF_DFFG_SNO_POINT_DATA_NAME = "List_of_DFFG_Snow_Point_Datas";
    private static final String DFFG_SNO_NAME = "DFFG_Snow";

    public DffgSnoFile(EEFilePair eeFilePair, DataContext context) throws IOException {
        super(eeFilePair, context, DFFG_SNO_NAME, LIST_OF_DFFG_SNO_POINT_DATA_NAME);
    }

    CompoundType getCellType() {
        return (CompoundType) getDataFormat().getTypeDef(DFFG_SNO_POINT_DATA_TYPE_NAME);
    }

    void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
        if (!descriptor.isVisible()) {
            return;
        }
        final int memberIndex = compoundType.getMemberIndex(descriptor.getMemberName());

        if (memberIndex >= 0) {
            final CompoundMember member = compoundType.getMember(memberIndex);

            final int dataType = ProductHelper.getDataType(member.getType());
            final Band band = product.addBand(descriptor.getBandName(), dataType);

            band.setScalingOffset(descriptor.getScalingOffset());
            band.setScalingFactor(descriptor.getScalingFactor());

            if (descriptor.hasFillValue()) {
                band.setNoDataValueUsed(true);
                band.setNoDataValue(descriptor.getFillValue());
            }
            if (!descriptor.getValidPixelExpression().isEmpty()) {
                band.setValidPixelExpression(descriptor.getValidPixelExpression());
            }
            if (!descriptor.getUnit().isEmpty()) {
                band.setUnit(descriptor.getUnit());
            }
            if (!descriptor.getDescription().isEmpty()) {
                band.setDescription(descriptor.getDescription());
            }
            if (descriptor.getFlagDescriptors() != null) {
                ProductHelper.addFlagsAndMasks(product, band, descriptor.getFlagCodingName(),
                        descriptor.getFlagDescriptors());
            }

            final CellValueProvider valueProvider = createCellValueProvider(descriptor);
            band.setSourceImage(createSourceImage(band, valueProvider));
            band.setImageInfo(createImageInfo());
        }
    }

    private static ImageInfo createImageInfo() {
        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[3];
        points[0] = new ColorPaletteDef.Point(0.0, new Color(0, 0, 0, 0.0f));
        points[1] = new ColorPaletteDef.Point(0.1, new Color(0.1f, 0.1f, 0.1f));
        points[2] = new ColorPaletteDef.Point(100.0, new Color(1.f, 1.f, 1.f));

        return new ImageInfo(new ColorPaletteDef(points));
    }
}
