package org.esa.smos.dataio.smos.dffg;

import com.bc.ceres.binio.*;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.CellGridOpImage;
import org.esa.smos.dataio.smos.CellValueProvider;
import org.esa.smos.dataio.smos.ExplorerFile;
import org.esa.smos.dataio.smos.ProductHelper;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dgg.SmosDgg;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.image.ResolutionLevel;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;


public class DffSnoFile extends ExplorerFile {

    private static final String DFFG_SNO_POINT_DATA_TYPE_NAME = "DFFG_Snow_Point_Data_Type";
    private static final String DELTA_LAT_NAME = "Delta_Lat";
    private static final String TAG_DIGITS_TO_SHIFT = "Digits_To_Shift";
    private static final String LIST_OF_DFFG_SNO_POINT_DATA_NAME = "List_of_DFFG_Snow_Point_Datas";
    private static final String LIST_OF_ROW_STRUCT_DATA_NAME = "List_of_Row_Struct_Datas";
    private static final String CUMULATED_LON_COUNT_NAME = "Cumulated_N_Lon";
    private static final String DELTA_LON_NAME = "Long_Step_Size_Ang";
    private static final String LON_COUNT_NAME = "N_Lon";
    private static final String MAX_LAT_NAME = "Lat_b";
    private static final String MIN_LAT_NAME = "Lat_a";
    private static final String MAX_LON_NAME = "Lon_b";
    private static final String MIN_LON_NAME = "Lon_a";
    private static final String DFFG_SNO_NAME = "DFFG_Snow";

    private final long zoneIndexMultiplier;
    private volatile java.util.List<Dffg> gridList = null;

    public DffSnoFile(EEFilePair eeFilePair, DataContext context) throws IOException {
        super(eeFilePair, context);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element specificProductHeader = getElement(document.getRootElement(), TAG_SPECIFIC_PRODUCT_HEADER);

        final int k = Integer.valueOf(specificProductHeader.getChildText(TAG_DIGITS_TO_SHIFT, namespace));
        zoneIndexMultiplier = (long) Math.pow(10.0, k);
    }

    @Override
    public Product createProduct() throws IOException {
        // @todo 1 tb/tb common code - merge with LAI file 2017-02-28
        final String productName = FileUtils.getFilenameWithoutExtension(getDataFile());
        final String productType = getProductType();
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDataFile());
        product.setPreferredTileSize(512, 504);
        ProductHelper.addMetadata(product.getMetadataRoot(), this);

        product.setSceneGeoCoding(ProductHelper.createGeoCoding(dimension));
        addBands(product);

        return product;
    }

    @Override
    public final Area getArea() {
        // @todo 2 tb/tb checke where these values come from 2017-02-28
        return new Area(new Rectangle2D.Double(-180.0, -88.59375, 360.0, 177.1875));
    }

    private void addBands(Product product) {
        final String formatName = getDataFormat().getName();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(formatName);

        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor, getCellType());
            }
        }
    }

    private CompoundType getCellType() {
        return (CompoundType) getDataFormat().getTypeDef(DFFG_SNO_POINT_DATA_TYPE_NAME);
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

            final CellValueProvider valueProvider = createCellValueProvider(descriptor);
            band.setSourceImage(createSourceImage(band, valueProvider));
            band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
        }
    }

    private CellValueProvider createCellValueProvider(BandDescriptor descriptor) {
        final int memberIndex = getCellType().getMemberIndex(descriptor.getMemberName());

        return new CellValueProviderImpl(memberIndex);
    }

    private MultiLevelImage createSourceImage(Band band, CellValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(final Band band, final CellValueProvider valueProvider) {
        return new AbstractMultiLevelSource(SmosDgg.getInstance().getMultiLevelImage().getModel()) {
            @Override
            protected RenderedImage createImage(int level) {
                return new CellGridOpImage(valueProvider, band, getModel(), ResolutionLevel.create(getModel(), level));
            }
        };
    }

    long getCellIndex(double lon, double lat) {
        final int zoneIndex = Eeap.getInstance().getZoneIndex(lon, lat);
        if (zoneIndex != -1) {
            final int gridIndex = getGridList().get(zoneIndex).getIndex(lon, lat);
            if (gridIndex != -1) {
                return gridIndex + zoneIndex * zoneIndexMultiplier;
            }
        }

        return -1;
    }

    private int getZoneIndex(long cellIndex) {
        return (int) (cellIndex / zoneIndexMultiplier);
    }

    private java.util.List<Dffg> getGridList() {
        java.util.List<Dffg> result = gridList;

        if (result == null) {
            synchronized (this) {
                result = gridList;
                if (result == null) {
                    try {
                        gridList = result = createGridList();
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot create zone list.", e);
                    }
                }
            }
        }

        return result;
    }

    private java.util.List<Dffg> createGridList() throws IOException {
        final SequenceData zoneSequenceData = getDataBlock().getSequence(DFFG_SNO_NAME);
        if (zoneSequenceData == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing zone data.", getDataFile().getPath()));
        }
        final ArrayList<Dffg> gridList = new ArrayList<>(
                zoneSequenceData.getElementCount());

        for (int i = 0; i < zoneSequenceData.getElementCount(); i++) {
            final CompoundData zoneCompoundData = zoneSequenceData.getCompound(i);
            final double minLat = zoneCompoundData.getDouble(MIN_LAT_NAME);
            final double maxLat = zoneCompoundData.getDouble(MAX_LAT_NAME);
            final double minLon = zoneCompoundData.getDouble(MIN_LON_NAME);
            final double maxLon = zoneCompoundData.getDouble(MAX_LON_NAME);
            final double deltaLat = zoneCompoundData.getDouble(DELTA_LAT_NAME);

            // due to an unresolved bug in ceres-binio it is essential to remember the LAI sequence data
            final SequenceData sequenceData = zoneCompoundData.getSequence(LIST_OF_DFFG_SNO_POINT_DATA_NAME);
            final Dffg grid = new Dffg(minLat, maxLat, minLon, maxLon, deltaLat, sequenceData);
            final SequenceData rowSequenceData = zoneCompoundData.getSequence(LIST_OF_ROW_STRUCT_DATA_NAME);
            for (int p = 0; p < rowSequenceData.getElementCount(); p++) {
                final CompoundData rowCompoundData = rowSequenceData.getCompound(p);
                final int lonCount = rowCompoundData.getInt(LON_COUNT_NAME);
                final double deltaLon = rowCompoundData.getDouble(DELTA_LON_NAME);
                final int cumulatedLonCount = rowCompoundData.getInt(CUMULATED_LON_COUNT_NAME);

                grid.setRow(p, lonCount, deltaLon, cumulatedLonCount);
            }

            gridList.add(grid);
        }

        return Collections.unmodifiableList(gridList);
    }

    private int getGridIndex(long cellIndex, int zoneIndex) {
        return (int) (cellIndex - zoneIndex * zoneIndexMultiplier);
    }

    private final class CellValueProviderImpl implements CellValueProvider {

        private final int memberIndex;

        public CellValueProviderImpl(int memberIndex) {
            this.memberIndex = memberIndex;
        }

        @Override
        public final Area getArea() {
            return DffSnoFile.this.getArea();
        }

        @Override
        public final long getCellIndex(double lon, double lat) {
            return DffSnoFile.this.getCellIndex(lon, lat);
        }

        @Override
        public final byte getValue(long cellIndex, byte noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getByte(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public final short getValue(long cellIndex, short noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getShort(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public final int getValue(long cellIndex, int noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getInt(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public final float getValue(long cellIndex, float noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getFloat(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public int getSnapshotId() {
            return -1;
        }

        @Override
        public void setSnapshotId(int snapshotId) {

        }
    }
}
