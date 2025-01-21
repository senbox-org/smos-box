package org.esa.smos.dataio.smos;

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.multilevel.MultiLevelImage;
import com.bc.ceres.multilevel.MultiLevelSource;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.smos.dgg.SmosDgg;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.io.FileUtils;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

class GlobalSmosFile extends ExplorerFile {

    private final SequenceData[] zones;

    protected GlobalSmosFile(EEFilePair eeFilePair, DataContext dataContext) throws IOException {
        super(eeFilePair, dataContext);
        final SequenceData zoneSequence = getDataBlock().getSequence(0);

        zones = new SequenceData[zoneSequence.getElementCount()];
        for (int i = 0; i < zones.length; i++) {
            zones[i] = zoneSequence.getCompound(i).getSequence(1);
        }
    }

    @Override
    public final Area getArea() {
        return new Area(new Rectangle2D.Double(-180.0, -88.59375, 360.0, 177.1875));
    }

    @Override
    public Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getDataFile());
        final String productType = getProductType();
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDataFile());
        product.setPreferredTileSize(512, 504);
        ProductHelper.addMetadata(product.getMetadataRoot(), this);

        product.setSceneGeoCoding(ProductHelper.createGeoCoding(dimension));
        final CompoundType compoundType = (CompoundType) zones[0].getType().getElementType();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(getDataFormat().getName());
        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor, compoundType);
            }
        }

        addAncilliaryBands(product);

        return product;
    }

    private void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
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

            final ValueProvider valueProvider = createValueProvider(descriptor);
            band.setSourceImage(createSourceImage(band, valueProvider));
            band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
        }
    }

    private ValueProvider createValueProvider(final BandDescriptor descriptor) {
        return new ValueProvider() {
            @Override
            public final Area getArea() {
                return GlobalSmosFile.this.getArea();
            }

            @Override
            public final byte getValue(int seqnum, byte noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(seqnum) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(seqnum) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getByte(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }

            @Override
            public final short getValue(int seqnum, short noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(seqnum) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(seqnum) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getShort(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }

            @Override
            public final int getValue(int seqnum, int noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(seqnum) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(seqnum) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getInt(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }

            @Override
            public final float getValue(int gridPointId, float noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(gridPointId) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(gridPointId) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getFloat(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }
        };
    }

    private MultiLevelImage createSourceImage(final Band band, final ValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(Band band, ValueProvider valueProvider) {
        return new SmosMultiLevelSource(band, valueProvider);
    }
}
