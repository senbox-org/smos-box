package org.esa.smos.dataio.smos;

import com.bc.ceres.binio.*;
import com.bc.ceres.multilevel.MultiLevelImage;
import com.bc.ceres.multilevel.MultiLevelSource;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import org.esa.smos.dataio.smos.provider.DefaultValueProvider;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.smos.dgg.SmosDgg;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.awt.*;
import java.awt.geom.Area;
import java.io.IOException;
import java.text.MessageFormat;

public class DggFile extends ExplorerFile {

    private final GridPointList gridPointList;
    private final int gridPointIdIndex;

    private final Area area;
    private final GridPointInfo gridPointInfo;


    protected DggFile(EEFilePair eeFilePair, DataContext dataContext, boolean fromZones, Area suppliedArea) throws IOException {
        super(eeFilePair, dataContext);
        try {
            if (fromZones) {
                gridPointList = createGridPointListFromZones(getDataBlock().getSequence(0));
            } else {
                gridPointList = createGridPointList(getDataBlock().getSequence(SmosConstants.GRID_POINT_LIST_NAME));
            }
            gridPointIdIndex = gridPointList.getCompoundType().getMemberIndex(SmosConstants.GRID_POINT_ID_NAME);
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    "Unable to read SMOS File ''{0}'': {1}.", eeFilePair.getDblFile().getPath(), e.getMessage()), e);
        }
        gridPointInfo = createGridPointInfo();
        if (suppliedArea == null) {
            area = DggUtils.computeArea(this.getGridPointList());
        } else {
            area = suppliedArea;
        }
    }

    protected DggFile(EEFilePair eeFilePair, DataContext dataContext, boolean fromZones) throws IOException {
        this(eeFilePair, dataContext, fromZones, null);
    }

    private GridPointList createGridPointList(SequenceData sequence) {
        return new PlainGridPointList(sequence);
    }

    private GridPointList createGridPointListFromZones(SequenceData zoneSequence) throws IOException {
        final SequenceData[] zones = new SequenceData[zoneSequence.getElementCount()];
        for (int i = 0; i < zones.length; i++) {
            zones[i] = zoneSequence.getCompound(i).getSequence(1);
        }
        return new ZoneGridPointList(zones);
    }

    public final int getGridPointCount() {
        return gridPointList.getElementCount();
    }

    public final int getGridPointSeqnum(int i) throws IOException {
        return SmosDgg.gridPointIdToSeqnum(getGridPointId(i));
    }

    private int getGridPointId(int i) throws IOException {
        final int gridPointId = gridPointList.getCompound(i).getInt(gridPointIdIndex);
        if (gridPointId < SmosDgg.MIN_GRID_POINT_ID || gridPointId > SmosDgg.MAX_GRID_POINT_ID) {
            throw new IOException(MessageFormat.format("Invalid Grid Point ID {0} at index {1}.", gridPointId, i));
        }
        return gridPointId;
    }

    public final GridPointList getGridPointList() {
        return gridPointList;
    }

    public int getGridPointIndex(int seqnum) {
        return gridPointInfo.getGridPointIndex(seqnum);
    }

    public final CompoundType getGridPointType() {
        return gridPointList.getCompoundType();
    }

    public CompoundData getGridPointData(int gridPointIndex) throws IOException {
        return gridPointList.getCompound(gridPointIndex);
    }

    @Override
    public Area getArea() {
        return new Area(area);
    }

    @Override
    public final Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getDataFile());
        final String productType = getProductType();
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDataFile());
        product.setPreferredTileSize(512, 504);
        ProductHelper.addMetadata(product.getMetadataRoot(), this);

        product.setSceneGeoCoding(ProductHelper.createGeoCoding(dimension));
        addBands(product);
        addAncilliaryBands(product);
        setTimes(product);

        return product;
    }


    protected void addBands(Product product) {
        final String formatName = getDataFormat().getName();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(formatName);

        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor);
            }
        }
    }

    protected void addBand(Product product, BandDescriptor descriptor) {
        addBand(product, descriptor, getGridPointType());
    }

    protected final void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
        if (!descriptor.isVisible()) {
            return;
        }

        final int memberIndex = compoundType.getMemberIndex(descriptor.getMemberName());

        if (memberIndex >= 0) {
            final CompoundMember member = compoundType.getMember(memberIndex);

            final int dataType = ProductHelper.getDataType(member.getType());
            final Band band = product.addBand(descriptor.getBandName(), dataType);

            band.setScalingOffset(descriptor.getScalingOffset());
            setScaling(band, descriptor);
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

    protected void setScaling(Band band, BandDescriptor descriptor) {
        band.setScalingFactor(descriptor.getScalingFactor());
    }

    protected AbstractValueProvider createValueProvider(BandDescriptor descriptor) {
        final int memberIndex = getGridPointType().getMemberIndex(descriptor.getMemberName());

        switch (descriptor.getSampleModel()) {
            case 1:
                return new DefaultValueProvider(this, memberIndex) {
                    @Override
                    public int getInt(int gridPointIndex) throws IOException {
                        return (int) (getLong(memberIndex) & 0x00000000FFFFFFFFL);
                    }
                };
            case 2:
                return new DefaultValueProvider(this, memberIndex) {
                    @Override
                    public int getInt(int gridPointIndex) throws IOException {
                        return (int) (getLong(memberIndex) >>> 32);
                    }
                };
            default:
                return new DefaultValueProvider(this, memberIndex);
        }
    }

    protected MultiLevelImage createSourceImage(final Band band, final ValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(Band band, ValueProvider valueProvider) {
        return new SmosMultiLevelSource(band, valueProvider);
    }

    private GridPointInfo createGridPointInfo() throws IOException {
        int minSeqnum = getGridPointSeqnum(0);
        int maxSeqnum = minSeqnum;

        final int gridPointCount = getGridPointCount();
        final int[] seqNumbers = new int[gridPointCount];
        seqNumbers[0] = minSeqnum;

        for (int i = 1; i < gridPointCount; i++) {
            final int seqnum = getGridPointSeqnum(i);
            seqNumbers[i] = seqnum;

            if (seqnum < minSeqnum) {
                minSeqnum = seqnum;
            }
            if (seqnum > maxSeqnum) {
                maxSeqnum = seqnum;
            }
        }

        final GridPointInfo gridPointInfo = new GridPointInfo(minSeqnum, maxSeqnum);
        gridPointInfo.setSequenceNumbers(seqNumbers);

        return gridPointInfo;
    }

    private void setTimes(Product product) {
        final String pattern = "'UTC='yyyy-MM-dd'T'HH:mm:ss";
        try {
            final Document document = getDocument();
            final Namespace namespace = document.getRootElement().getNamespace();
            final Element validityPeriod = getElement(document.getRootElement(), "Validity_Period");
            final String validityStart = validityPeriod.getChildText("Validity_Start", namespace);
            final String validityStop = validityPeriod.getChildText("Validity_Stop", namespace);
            product.setStartTime(ProductData.UTC.parse(validityStart, pattern));
            product.setEndTime(ProductData.UTC.parse(validityStop, pattern));
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final class PlainGridPointList implements GridPointList {

        private final SequenceData sequence;
        private final int latIndex;
        private final int lonIndex;

        public PlainGridPointList(SequenceData sequence) {
            this.sequence = sequence;

            lonIndex = getCompoundType().getMemberIndex(SmosConstants.GRID_POINT_LON_NAME);
            latIndex = getCompoundType().getMemberIndex(SmosConstants.GRID_POINT_LAT_NAME);
        }

        @Override
        public final int getElementCount() {
            return sequence.getElementCount();
        }

        @Override
        public final CompoundData getCompound(int i) throws IOException {
            return sequence.getCompound(i);
        }

        @Override
        public final CompoundType getCompoundType() {
            return (CompoundType) sequence.getType().getElementType();
        }

        @Override
        public final double getLon(int i) throws IOException {
            return getCompound(i).getFloat(lonIndex);
        }

        @Override
        public final double getLat(int i) throws IOException {
            return getCompound(i).getFloat(latIndex);
        }
    }

    private static final class ZoneGridPointList implements GridPointList {

        private final SequenceData[] zones;
        private final int lonIndex;
        private final int latIndex;

        public ZoneGridPointList(SequenceData[] zones) {
            this.zones = zones;
            lonIndex = getCompoundType().getMemberIndex(SmosConstants.GRID_POINT_LON_NAME);
            latIndex = getCompoundType().getMemberIndex(SmosConstants.GRID_POINT_LAT_NAME);
        }

        @Override
        public final int getElementCount() {
            int elementCount = 0;
            for (final SequenceData zone : zones) {
                elementCount += zone.getElementCount();
            }
            return elementCount;
        }

        @Override
        public final CompoundData getCompound(int i) throws IOException {
            for (int z = 0, counts = 0, offset = 0, zonesLength = zones.length; z < zonesLength; z++) {
                counts += zones[z].getElementCount();
                if (i < counts) {
                    return zones[z].getCompound(i - offset);
                }
                offset = counts;
            }
            throw new IOException(MessageFormat.format("Cannot read compound data for index {0}", i));
        }

        @Override
        public final CompoundType getCompoundType() {
            return (CompoundType) zones[0].getType().getElementType();
        }

        @Override
        public double getLon(int i) throws IOException {
            return getCompound(i).getFloat(lonIndex);
        }

        @Override
        public double getLat(int i) throws IOException {
            return getCompound(i).getFloat(latIndex);
        }
    }
}
