package org.esa.smos.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.smos.dataio.smos.CellValueProvider;
import org.esa.smos.dataio.smos.DggUtils;
import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.PolarisationModel;
import org.esa.smos.dataio.smos.ProductHelper;
import org.esa.smos.dataio.smos.SmosReader;
import org.esa.smos.dataio.smos.SnapshotInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dgg.SmosDgg;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.logging.BeamLogManager;
import ucar.ma2.Array;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * @author Ralf Quast
 */
public class SmosBufrReader extends SmosReader {

    private BufrSupport bufrSupport;
    private ValueDecoders valueDecoders;
    private int firstSnapshotId;
    private SnapshotInfo snapshotInfo;

    private final Map<Integer, IndexArea> snapshotMessageIndexMap;

    public SmosBufrReader(SmosBufrReaderPlugIn smosBufrReaderPlugIn) {
        super(smosBufrReaderPlugIn);

        snapshotMessageIndexMap = new HashMap<>();
        firstSnapshotId = -1;

        snapshotInfo = null;
    }

    @Override
    public final boolean canSupplyGridPointBtData() {
        return false;
    }

    @Override
    public final boolean canSupplyFullPolData() {
        return true;
    }

    @Override
    public final GridPointBtDataset getBtData(int gridPointIndex) throws IOException {
        return null;
    }

    @Override
    public final int getGridPointIndex(int gridPointId) {
        return -1;
    }

    @Override
    public int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel) {
        final MultiLevelImage levelImage = SmosDgg.getInstance().getMultiLevelImage();
        final RenderedImage image = levelImage.getImage(currentLevel);
        final Raster data = image.getData(new Rectangle(levelPixelX, levelPixelY, 1, 1));
        return data.getSample(levelPixelX, levelPixelY, 0);
    }

    @Override
    public final String[] getRawDataTableNames() {
        return null;
    }

    @Override
    public final FlagDescriptor[] getBtFlagDescriptors() {
        return null;
    }

    @Override
    public final PolarisationModel getPolarisationModel() {
        return new BufrPolarisationModel();
    }

    @Override
    public final boolean canSupplySnapshotData() {
        return true;
    }

    @Override
    public final boolean hasSnapshotInfo() {
        return true;
    }

    @Override
    public SnapshotInfo getSnapshotInfo() {
        if (snapshotInfo == null) {
            createSnapshotInfo();
        }
        return snapshotInfo;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) throws IOException {
        synchronized (this) {
            final StructureDataIterator observationIterator = bufrSupport.getStructureIterator(snapshotIndex);
            final StructureData snapshotStructure = observationIterator.next();
            if (snapshotStructure == null) {
                return new Object[0][];
            }

            final Object[][] snapshotData = new Object[BufrSupport.SNAPSHOT_DATA_NAMES.length][2];
            for (int i = 0; i < BufrSupport.SNAPSHOT_DATA_NAMES.length; i++) {
                final String variableName = BufrSupport.SNAPSHOT_DATA_NAMES[i];
                snapshotData[i][0] = variableName;
                if (variableName.equals(SmosBufrFile.SNAPSHOT_OVERALL_QUALITY)) {
                    final Array snapshot_overall_quality = snapshotStructure.getArray(SmosBufrFile.SNAPSHOT_OVERALL_QUALITY);
                    snapshotData[i][1] = snapshot_overall_quality.getByte(0);
                    continue;
                }

                final ValueAccessor valueAccessor = ValueAccessors.get(variableName);
                valueAccessor.read(snapshotStructure);
                final int rawValue = valueAccessor.getRawValue();
                if (i == BufrSupport.TEC_INDEX) {
                    snapshotData[i][1] = valueDecoders.tecDecoder.decode(rawValue);
                } else if (i == BufrSupport.ACCURACY_INDEX) {
                    snapshotData[i][1] = valueDecoders.snapshotAccuracyDecoder.decode(rawValue);
                } else if (i == BufrSupport.RA_PP_INDEX) {
                    snapshotData[i][1] = valueDecoders.raPpDecoder.decode(rawValue);
                } else if (i == BufrSupport.RA_CP_INDEX) {
                    snapshotData[i][1] = valueDecoders.raCpDecoder.decode(rawValue);
                } else {
                    snapshotData[i][1] = rawValue;
                }
            }
            return snapshotData;
        }
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();
        bufrSupport = new BufrSupport();
        bufrSupport.open(inputFile.getPath());

        final Product product = ProductHelper.createProduct(inputFile, "SMOS.MIRAS.NRT_BUFR");

        bufrSupport.extractMetaData(product);
        valueDecoders = bufrSupport.extractValueDecoders();

        scanFile();
        addBands(product);

        return product;
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (bufrSupport != null) {
            bufrSupport.close();
            bufrSupport = null;
        }
    }

    @Override
    protected final void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                                int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                                int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                                ProgressMonitor pm) throws IOException {
        synchronized (this) {
            final RenderedImage image = destBand.getSourceImage();
            final Raster data = image.getData(new Rectangle(destOffsetX, destOffsetY, destWidth, destHeight));

            data.getDataElements(destOffsetX, destOffsetY, destWidth, destHeight, destBuffer.getElems());
        }
    }

    private void scanFile() throws IOException {
        synchronized (this) {
            for (int i = 0, messageCount = bufrSupport.getMessageCount(); i < messageCount; i++) {
                final IndexArea current = new IndexArea(i);
                int snapshotId = -1;

                final StructureDataIterator observationIterator = bufrSupport.getStructureIterator(i);
                boolean firstIteration = true;
                Rectangle2D snapshotArea = null;
                while (observationIterator.hasNext()) {
                    final StructureData structureData = observationIterator.next();

                    if (firstIteration) {
                        snapshotId = structureData.getScalarInt(SmosBufrFile.SNAPSHOT_IDENTIFIER);
                        firstIteration = false;
                        if (firstSnapshotId == -1) {
                            firstSnapshotId = snapshotId;
                        }
                    }

                    final int highAccuracyLon = structureData.getScalarInt(SmosBufrFile.LONGITUDE_HIGH_ACCURACY);
                    final float lon = (float) valueDecoders.lonDecoder.decode(highAccuracyLon);

                    final int highAccuracyLat = structureData.getScalarInt(SmosBufrFile.LATITUDE_HIGH_ACCURACY);
                    final float lat = (float) valueDecoders.latDecoder.decode(highAccuracyLat);

                    if (snapshotArea == null) {
                        snapshotArea = DggUtils.createGridPointRectangle(lon, lat);
                    } else {
                        final Rectangle2D gridPointRectangle = DggUtils.createGridPointRectangle(lon, lat);
                        snapshotArea.add(gridPointRectangle);
                    }
                }

                current.setArea(snapshotArea);
                snapshotMessageIndexMap.put(snapshotId, current);
            }
        }
    }

    private void addBands(Product product) throws IOException {
        final SmosBufrFile smosBufrFile = bufrSupport.getSmosBufrFile();
        final Sequence sequence = smosBufrFile.getObservationStructure();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors("BUFR");
        for (final BandDescriptor descriptor : descriptors.asList()) {
            final Variable variable = sequence.findVariable(descriptor.getMemberName());
            if (variable.getDataType().isEnum()) {
                final int dataType = ProductData.TYPE_UINT8;
                addBand(product, variable, dataType, descriptor);
            } else {
                final int dataType = BufrSupport.getBufrDataType(variable);
                if (dataType != -1) {
                    addBand(product, variable, dataType, descriptor);
                }
            }
        }
    }


    private void addBand(Product product, Variable variable, int dataType, BandDescriptor descriptor) throws
            IOException {
        if (!descriptor.isVisible()) {
            return;
        }

        final Band band = product.addBand(descriptor.getBandName(), dataType);
        final Attribute units = variable.findAttribute("units");
        if (units != null) {
            band.setUnit(units.getStringValue());
        }
        final SmosBufrFile smosBufrFile = bufrSupport.getSmosBufrFile();
        final ValueDecoder valueDecoder = smosBufrFile.getValueDecoder(variable.getShortName());
        final double offset = valueDecoder.getOffset();
        if (offset != 0.0) {
            band.setScalingOffset(offset);
        }
        final double scaleFactor = valueDecoder.getScaleFactor();
        if (scaleFactor != 1.0) {
            band.setScalingFactor(scaleFactor);
        }
        final Number missingValue = valueDecoder.getMissingValue();
        if (missingValue != null) {
            band.setNoDataValue(missingValue.doubleValue());
            band.setNoDataValueUsed(true);
        }
        final String validPixelExpression = descriptor.getValidPixelExpression();
        if (StringUtils.isNotNullAndNotEmpty(validPixelExpression)) {
            band.setValidPixelExpression(validPixelExpression);
        }
        if (!descriptor.getDescription().isEmpty()) {
            band.setDescription(descriptor.getDescription());
        }
        if (descriptor.getFlagDescriptors() != null) {
            ProductHelper.addFlagsAndMasks(product, band, descriptor.getFlagCodingName(),
                                           descriptor.getFlagDescriptors());
        }

        final String memberName = descriptor.getMemberName();

        final ValueAccessor valueAccessor = ValueAccessors.get(memberName);
        final CellValueProvider valueProvider = new BufrCellValueProvider(valueAccessor, firstSnapshotId, descriptor.getPolarization());
        band.setSourceImage(createSourceImage(band, valueProvider));
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }

    // @todo 2 tb/tb this method wants to be cleaned up and unified with the light reader 2014-12-02
    private MultiLevelImage createSourceImage(final Band band, final CellValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    // @todo 2 tb/tb this method wants to be cleaned up and unified with the light reader 2014-12-02
    private MultiLevelSource createMultiLevelSource(final Band band, final CellValueProvider valueProvider) {
        final MultiLevelModel multiLevelModel = SmosDgg.getInstance().getMultiLevelImage().getModel();
        return new LightBufrMultiLevelSource(multiLevelModel, valueProvider, band);
    }

    private void createSnapshotInfo() {
        try {
            final Set<Long> all = new TreeSet<>();
            final Set<Long> x = new TreeSet<>();
            final Set<Long> y = new TreeSet<>();
            final Set<Long> xy = new TreeSet<>();
            final Map<Long, Integer> snapshotIndexMap = new TreeMap<>();
            final Map<Long, Rectangle2D> snapshotAreaMap = new TreeMap<>();

            final PolarisationModel polarisationModel = getPolarisationModel();

            synchronized (this) {
                for (int i = 0, messageCount = bufrSupport.getMessageCount(); i < messageCount; i++) {
                    final StructureDataIterator observationIterator = bufrSupport.getStructureIterator(i);
                    while (observationIterator.hasNext()) {
                        final StructureData snapshotData = observationIterator.next();

                        final long longSnapshotId = snapshotData.getScalarInt(SmosBufrFile.SNAPSHOT_IDENTIFIER);
                        final byte snapshotPolarisation = snapshotData.getScalarByte(SmosBufrFile.POLARISATION);

                        all.add(longSnapshotId);

                        if (polarisationModel.is_X_Polarised(snapshotPolarisation)) {
                            x.add(longSnapshotId);
                        }
                        if (polarisationModel.is_Y_Polarised(snapshotPolarisation)) {
                            y.add(longSnapshotId);
                        }
                        if (polarisationModel.is_XY1_Polarised(snapshotPolarisation) || polarisationModel.is_XY2_Polarised(snapshotPolarisation)) {
                            xy.add(longSnapshotId);
                        }
                    }
                }
            }
            final Set<Integer> snapshotIds = snapshotMessageIndexMap.keySet();
            for (final int snapShotId : snapshotIds) {
                final IndexArea indexArea = snapshotMessageIndexMap.get(snapShotId);
                final Long longSnapshotId = (long) snapShotId;
                snapshotIndexMap.put(longSnapshotId, indexArea.getMessageIndex());
                snapshotAreaMap.put(longSnapshotId, indexArea.getArea());
            }

            snapshotInfo = new SnapshotInfo(snapshotIndexMap, all, x, y, xy, snapshotAreaMap);
        } catch (IOException e) {
            final Logger systemLogger = BeamLogManager.getSystemLogger();
            systemLogger.warning("Failed to read snpshot data: " + e.getMessage());
            snapshotInfo = null;
        }
    }

    private class BufrCellValueProvider implements CellValueProvider {

        private final ValueAccessor valueAccessor;
        private final int polarisation;
        private int snapshotId;
        private int snapshotMessageIndex;
        private Area area;
        private HashMap<Integer, Integer> dataMap;
        private boolean dataLoaded;

        private BufrCellValueProvider(ValueAccessor valueAccessor, int firstSnapshotId, int polarisation) {
            this.polarisation = polarisation;
            this.valueAccessor = valueAccessor;
            snapshotId = firstSnapshotId;
            findSnapshotAreaAndIndex();
        }

        @Override
        public Area getArea() {
            return area;
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            final int seqnum = SmosDgg.getInstance().getSeqnum(lon, lat);
            return SmosDgg.seqnumToGridPointId(seqnum);
        }

        @Override
        public byte getValue(long cellIndex, byte noDataValue) {
            return (byte) getData((int) cellIndex, noDataValue);
        }

        @Override
        public int getValue(long cellIndex, int noDataValue) {
            return getData((int) cellIndex, noDataValue);
        }

        @Override
        public short getValue(long cellIndex, short noDataValue) {
            return (short) getData((int) cellIndex, noDataValue);
        }

        @Override
        public float getValue(long cellIndex, float noDataValue) {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public int getSnapshotId() {
            return snapshotId;
        }

        @Override
        public void setSnapshotId(int snapshotId) {
            this.snapshotId = snapshotId;
            findSnapshotAreaAndIndex();
            dataLoaded = false;
        }

        private void findSnapshotAreaAndIndex() {
            final IndexArea indexArea = SmosBufrReader.this.snapshotMessageIndexMap.get(snapshotId);
            area = new Area(indexArea.getArea());
            snapshotMessageIndex = indexArea.getMessageIndex();
        }

        private void readSnapshotData() {
            try {
                synchronized (SmosBufrReader.this) {
                    dataMap = new HashMap<>();
                    final StructureDataIterator observationIterator = bufrSupport.getStructureIterator(snapshotMessageIndex);
                    while (observationIterator.hasNext()) {
                        final StructureData snapshotData = observationIterator.next();

                        final byte snapshotPolarisation = snapshotData.getScalarByte(SmosBufrFile.POLARISATION);
                        if (polarisation == 4 ||
                                (snapshotPolarisation & 3) == polarisation ||
                                (polarisation & snapshotPolarisation & 2) != 0) {

                            valueAccessor.read(snapshotData);

                            dataMap.put(valueAccessor.getGridPointId(), valueAccessor.getRawValue());
                        }
                    }
                }
                dataLoaded = true;
            } catch (IOException e) {
                final Logger systemLogger = BeamLogManager.getSystemLogger();
                systemLogger.warning("Failed to read snpshot data: " + e.getMessage());
            }
        }

        private int getData(int cellIndex, int noDataValue) {
            return getSnapshotData(cellIndex, noDataValue);
        }

        private int getSnapshotData(int cellIndex, int noDataValue) {
            if (!dataLoaded) {
                readSnapshotData();
            }

            final Integer rawValue = dataMap.get(cellIndex);
            if (rawValue == null) {
                return noDataValue;
            }

            return rawValue;
        }
    }
}
