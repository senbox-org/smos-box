package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.dataio.smos.*;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.StringUtils;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ralf Quast
 */
public class SmosBufrReader extends SmosReader {

    private BufrSupport bufrSupport;
    private ValueDecoders valueDecoders;
    private int firstSnapshotId;

    private final Map<Integer, IndexArea> snapshotMessageIndexMap;

    public SmosBufrReader(SmosBufrReaderPlugIn smosBufrReaderPlugIn) {
        super(smosBufrReaderPlugIn);

        snapshotMessageIndexMap = new HashMap<>();
        firstSnapshotId = -1;
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
        // TODO - check
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
        // TODO - implement
        return null;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) throws IOException {
        // TODO - implement
        return new Object[0][];
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();
        bufrSupport = new BufrSupport();
        bufrSupport.open(inputFile.getPath());

        final Product product = bufrSupport.createProduct(inputFile, "SMOS.MIRAS.NRT_BUFR");

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
                final int dataType = DataTypeUtils.getRasterDataType(variable);
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

        final Integer index = BufrSupport.getDatasetNameIndexMap().get(descriptor.getMemberName());

        final CellValueProvider valueProvider;
        if (descriptor.getFlagDescriptors() == null) {
            final ValueDecoder scalingFactor = smosBufrFile.getValueDecoder(descriptor.getMemberName());
            valueProvider = new BufrCellValueProvider(descriptor.getPolarization(), index, scalingFactor, firstSnapshotId);
        } else {
            valueProvider = new FlagCellValueProvider(descriptor.getPolarization(), index, firstSnapshotId);
        }
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

    private class BufrCellValueProvider implements CellValueProvider {

        private final int dataindex;
        private final int polarisation;
        private final ValueDecoder valueDecoder;
        private int snapshotId;

        private BufrCellValueProvider(int polarisation, int dataIndex, ValueDecoder valueDecoder, int firstSnapshotId) {
            this.dataindex = dataIndex;
            this.polarisation = polarisation;
            this.valueDecoder = valueDecoder;
            snapshotId = firstSnapshotId;
        }

        @Override
        public Area getArea() {
            final IndexArea indexArea = SmosBufrReader.this.snapshotMessageIndexMap.get(snapshotId);
            return new Area(indexArea.getArea());
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            // @todo 1 tb/tb implement 2014-12-02
            return -1;
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

        private int getData(int cellIndex, int noDataValue) {
            return getSnapshotData(cellIndex, noDataValue);
        }

        private int getSnapshotData(int cellIndex, int noDataValue) {
            // @todo 1 tb/tb implement 2014-12-02
            return 13;

            //return noDataValue;
        }

        @Override
        public int getSnapshotId() {
            return snapshotId;
        }

        @Override
        public void setSnapshotId(int snapshotId) {
            this.snapshotId = snapshotId;
        }
    }

    private class FlagCellValueProvider implements CellValueProvider {

        private final int dataindex;
        private final int polarisation;
        private int snapshotId;

        private FlagCellValueProvider(int polarisation, int dataIndex, int firstSnapshotId) {
            this.dataindex = dataIndex;
            this.polarisation = polarisation;
            snapshotId = firstSnapshotId;
        }

        @Override
        public Area getArea() {
            final IndexArea indexArea = SmosBufrReader.this.snapshotMessageIndexMap.get(snapshotId);
            return new Area(indexArea.getArea());
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            // @todo 1 tb/tb implement 2014-12-02
            return -1;
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

        private int getData(int cellIndex, int noDataValue) {
            return getSnapshotData(cellIndex, noDataValue);
        }

        private int getSnapshotData(int cellIndex, int noDataValue) {
            // @todo 1 tb/tb implement 2014-12-02
            return 13;

            //return noDataValue;
        }

        @Override
        public int getSnapshotId() {
            return snapshotId;
        }

        @Override
        public void setSnapshotId(int snapshotId) {
            this.snapshotId = snapshotId;
        }
    }
}
