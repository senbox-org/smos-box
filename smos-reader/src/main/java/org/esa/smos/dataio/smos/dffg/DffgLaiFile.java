/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.smos.dataio.smos.dffg;

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.CellValueProvider;
import org.esa.smos.dataio.smos.ProductHelper;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;

public class DffgLaiFile extends DffgFile {

    private static final String DFFG_LAI_NAME = "DFFG_LAI";
    private static final String DFFG_LAI_POINT_DATA_TYPE_NAME = "DFFG_LAI_Point_Data_Type";
    private static final String LIST_OF_DFFG_LAI_POINT_DATA_NAME = "List_of_DFFG_LAI_Point_Datas";

    private static final String TAG_SCALING_OFFSET = "Offset";
    private static final String TAG_SCALING_FACTOR = "Scaling_Factor";

    private final double scalingOffset;
    private final double scalingFactor;

    public DffgLaiFile(EEFilePair eeFilePair, DataContext dataContext) throws IOException {
        super(eeFilePair, dataContext, DFFG_LAI_NAME, LIST_OF_DFFG_LAI_POINT_DATA_NAME);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element specificProductHeader = getElement(document.getRootElement(), TAG_SPECIFIC_PRODUCT_HEADER);

        scalingOffset = Double.valueOf(specificProductHeader.getChildText(TAG_SCALING_OFFSET, namespace));
        scalingFactor = Double.valueOf(specificProductHeader.getChildText(TAG_SCALING_FACTOR, namespace));
    }

    CompoundType getCellType() {
        return (CompoundType) getDataFormat().getTypeDef(DFFG_LAI_POINT_DATA_TYPE_NAME);
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

            if ("LAI".equals(member.getName())) {
                band.setScalingOffset(scalingOffset);
                band.setScalingFactor(scalingFactor);
            } else {
                band.setScalingOffset(descriptor.getScalingOffset());
                band.setScalingFactor(descriptor.getScalingFactor());
            }
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
            band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
        }
    }
}
