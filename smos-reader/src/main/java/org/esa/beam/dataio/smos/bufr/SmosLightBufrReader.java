package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.binning.support.ReducedGaussianGrid;
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
import ucar.ma2.Array;
import ucar.ma2.DataType;
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
import java.util.*;
import java.util.List;

public class SmosLightBufrReader extends SmosReader {

    private static final HashMap<String, Integer> datasetNameIndexMap;
    private static final String[] rawDataNames;
    private static final String[] snapshotDataNames;

    private static final double CENTER_BROWSE_INCIDENCE_ANGLE = 42.5;
    private static final double MIN_BROWSE_INCIDENCE_ANGLE = 37.5;
    private static final double MAX_BROWSE_INCIDENCE_ANGLE = 52.5;

    // Grid Point Indices
    private static final int BT_REAL_INDEX = 0;
    private static final int BT_IMAG_INDEX = 1;
    private static final int RADIOMETRIC_ACCURACY_INDEX = 2;
    private static final int INCIDENCE_ANGLE_INDEX = 3;
    private static final int AZIMUTH_ANGLE_INDEX = 4;
    private static final int FARADAY_ANGLE_INDEX = 5;
    private static final int GEOMETRIC_ANGLE_INDEX = 6;
    private static final int FOOTPRINT_AXIS_1_INDEX = 7;
    private static final int FOOTPRINT_AXIS_2_INDEX = 8;
    private static final int WATER_FRACTION_INDEX = 9;
    private static final int INFORMATION_FLAG_INDEX = 10;
    private static final int POLARISATION_INDEX = 11;

    // Snapshot Indices
    private static final int TEC_INDEX = 7;
    private static final int ACCURACY_INDEX = 9;
    private static final int RA_PP_INDEX = 10;
    private static final int RA_CP_INDEX = 11;

    static {
        rawDataNames = new String[]{
                SmosBufrFile.BRIGHTNESS_TEMPERATURE_REAL_PART,
                SmosBufrFile.BRIGHTNESS_TEMPERATURE_IMAGINARY_PART,
                SmosBufrFile.PIXEL_RADIOMETRIC_ACCURACY,
                SmosBufrFile.INCIDENCE_ANGLE,
                SmosBufrFile.AZIMUTH_ANGLE,
                SmosBufrFile.FARADAY_ROTATIONAL_ANGLE,
                SmosBufrFile.GEOMETRIC_ROTATIONAL_ANGLE,
                SmosBufrFile.FOOTPRINT_AXIS_1,
                SmosBufrFile.FOOTPRINT_AXIS_2,
                SmosBufrFile.WATER_FRACTION,
                SmosBufrFile.SMOS_INFORMATION_FLAG,
                SmosBufrFile.POLARISATION
        };

        snapshotDataNames = new String[]{
                SmosBufrFile.NUMBER_OF_GRID_POINTS,
                "Year",
                "Month",
                "Day",
                "Hour",
                "Minute",
                "Second",
                SmosBufrFile.TOTAL_ELECTRON_COUNT,
                SmosBufrFile.DIRECT_SUN_BRIGHTNESS_TEMPERATURE,
                SmosBufrFile.SNAPSHOT_ACCURACY,
                SmosBufrFile.RADIOMETRIC_ACCURACY_PP,
                SmosBufrFile.RADIOMETRIC_ACCURACY_CP,
                SmosBufrFile.SNAPSHOT_OVERALL_QUALITY
        };

        datasetNameIndexMap = new HashMap<>();
        datasetNameIndexMap.put(rawDataNames[0], BT_REAL_INDEX);
        datasetNameIndexMap.put(rawDataNames[1], BT_IMAG_INDEX);
        datasetNameIndexMap.put(rawDataNames[2], RADIOMETRIC_ACCURACY_INDEX);
        datasetNameIndexMap.put(rawDataNames[3], INCIDENCE_ANGLE_INDEX);
        datasetNameIndexMap.put(rawDataNames[4], AZIMUTH_ANGLE_INDEX);
        datasetNameIndexMap.put(rawDataNames[5], FARADAY_ANGLE_INDEX);
        datasetNameIndexMap.put(rawDataNames[6], GEOMETRIC_ANGLE_INDEX);
        datasetNameIndexMap.put(rawDataNames[7], FOOTPRINT_AXIS_1_INDEX);
        datasetNameIndexMap.put(rawDataNames[8], FOOTPRINT_AXIS_2_INDEX);
        datasetNameIndexMap.put(rawDataNames[9], WATER_FRACTION_INDEX);
        datasetNameIndexMap.put(rawDataNames[10], INFORMATION_FLAG_INDEX);
        datasetNameIndexMap.put(rawDataNames[11], POLARISATION_INDEX);
    }

    private HashMap<Integer, SnapshotObservation> snapshotMap;
    private HashMap<Integer, List<Observation>> gridPointMap;
    private Grid grid;
    private Area area;
    private ValueDecoders valueDecoders;
    private int gridPointMinIndex;
    private int gridPointMaxIndex;
    private LinkedList<Integer> snapshotIdList;
    private SnapshotInfo snapshotInfo;
    private BufrSupport bufrSupport;

    SmosLightBufrReader(SmosLightBufrReaderPlugIn smosLightBufrReaderPlugIn) {
        super(smosLightBufrReaderPlugIn);
        bufrSupport = null;
        gridPointMinIndex = -1;
        gridPointMaxIndex = -1;
    }

    @Override
    public GridPointBtDataset getBtData(int gridPointIndex) {
        if (gridPointIndex < 0) {
            return null;
        }

        final List<Observation> gridPointData = gridPointMap.get(gridPointIndex);
        if (gridPointData == null) {
            return null;
        }

        final int numData = datasetNameIndexMap.size();

        final Class[] classes = new Class[numData];
        for (int i = 0; i < classes.length; i++) {
            if (i == INFORMATION_FLAG_INDEX || i == POLARISATION_INDEX) {
                classes[i] = Integer.class;
            } else {
                classes[i] = Double.class;
            }
        }
        final int numMeasurements = gridPointData.size();
        final Number[][] data = new Number[numMeasurements][numData];

        for (int i = 0; i < numMeasurements; i++) {
            final Number[] currentMeasures = new Number[numData];

            final Observation observation = gridPointData.get(i);
            for (int k = 0; k < numData; k++) {
                if (classes[k] == Integer.class) {
                    currentMeasures[k] = observation.data[k];
                } else {
                    currentMeasures[k] = valueDecoders.dataDecoders[k].decode(observation.data[k]);
                }
            }

            data[i] = currentMeasures;
        }

        final GridPointBtDataset btDataset = new GridPointBtDataset(datasetNameIndexMap, classes, data);
        btDataset.setFlagBandIndex(INFORMATION_FLAG_INDEX);
        btDataset.setIncidenceAngleBandIndex(INCIDENCE_ANGLE_INDEX);
        btDataset.setRadiometricAccuracyBandIndex(RADIOMETRIC_ACCURACY_INDEX);
        btDataset.setBTValueRealBandIndex(BT_REAL_INDEX);
        btDataset.setBTValueImaginaryBandIndex(BT_IMAG_INDEX);
        btDataset.setPolarisationFlagBandIndex(POLARISATION_INDEX);
        return btDataset;
    }

    @Override
    public boolean canSupplyGridPointBtData() {
        return true;
    }

    @Override
    public boolean canSupplyFullPolData() {
        return true;
    }

    @Override
    public int getGridPointIndex(int gridPointId) {
        if (gridPointId >= gridPointMinIndex && gridPointId <= gridPointMaxIndex) {
            if (gridPointMap.containsKey(gridPointId)) {
                return gridPointId;
            }
        }
        return -1;
    }

    @Override
    public int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel) {
        return grid.getCellIndex(levelPixelX, levelPixelY, currentLevel);
    }

    @Override
    public String[] getRawDataTableNames() {
        return rawDataNames;
    }

    @Override
    public FlagDescriptor[] getBtFlagDescriptors() {
        final Family<FlagDescriptor> flagDescriptors = Dddb.getInstance().getFlagDescriptors("BUFR_flags");
        final List<FlagDescriptor> flagDescriptorsList = flagDescriptors.asList();

        return flagDescriptorsList.toArray(new FlagDescriptor[flagDescriptorsList.size()]);
    }

    @Override
    public PolarisationModel getPolarisationModel() {
        return new BufrPolarisationModel();
    }

    @Override
    public boolean canSupplySnapshotData() {
        return true;
    }

    @Override
    public boolean hasSnapshotInfo() {
        return true;
    }

    @Override
    public SnapshotInfo getSnapshotInfo() {
        if (snapshotInfo == null) {
            snapshotInfo = createSnapshotInfo();
        }
        return snapshotInfo;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) {
        final Integer snapshotId = snapshotIdList.get(snapshotIndex);
        final SnapshotObservation snapshotObservation = snapshotMap.get(snapshotId);
        if (snapshotObservation == null) {
            return new Object[0][];
        }

        final Object[][] snapshotData = new Object[snapshotDataNames.length][2];
        for (int i = 0; i < snapshotDataNames.length; i++) {
            snapshotData[i][0] = snapshotDataNames[i];
            if (i == TEC_INDEX) {
                snapshotData[i][1] = valueDecoders.tecDecoder.decode(snapshotObservation.data[i]);
            } else if (i == ACCURACY_INDEX) {
                snapshotData[i][1] = valueDecoders.snapshotAccuracyDecoder.decode(snapshotObservation.data[i]);
            } else if (i == RA_PP_INDEX) {
                snapshotData[i][1] = valueDecoders.raPpDecoder.decode(snapshotObservation.data[i]);
            } else if (i == RA_CP_INDEX) {
                snapshotData[i][1] = valueDecoders.raCpDecoder.decode(snapshotObservation.data[i]);
            } else {
                snapshotData[i][1] = snapshotObservation.data[i];
            }
        }

        return snapshotData;
    }

    private SnapshotInfo createSnapshotInfo() {
        final Set<Integer> snapshotIds = snapshotMap.keySet();
        snapshotIdList = new LinkedList<>(snapshotIds);

        Collections.sort(snapshotIdList);

        final Set<Long> all = new TreeSet<>();
        final Set<Long> x = new TreeSet<>();
        final Set<Long> y = new TreeSet<>();
        final Set<Long> xy = new TreeSet<>();
        final Map<Long, Integer> snapshotIndexMap = new TreeMap<>();
        final Map<Long, Rectangle2D> snapshotAreaMap = new TreeMap<>();

        final PolarisationModel polarisationModel = getPolarisationModel();
        int index = 0;
        for (final Integer snapshotId : snapshotIdList) {
            final long snapshotIdLong = (long) snapshotId;
            all.add(snapshotIdLong);
            snapshotIndexMap.put(snapshotIdLong, index);
            ++index;

            boolean hasXPolData = false;
            boolean hasYPolData = false;
            boolean hasXYPolData = false;
            final SnapshotObservation snapshotObservation = snapshotMap.get(snapshotId);
            Rectangle2D snapshotRect = null;
            for (final Observation observation : snapshotObservation.observations) {
                final Rectangle2D gridRect = grid.getGridRect(observation.lon, observation.lat);
                if (snapshotRect == null) {
                    snapshotRect = gridRect;
                } else {
                    snapshotRect.add(gridRect);
                }

                final int polarisationMode = observation.data[POLARISATION_INDEX];
                if (polarisationModel.is_X_Polarised(polarisationMode)) {
                    hasXPolData = true;
                }
                if (polarisationModel.is_Y_Polarised(polarisationMode)) {
                    hasYPolData = true;
                }

                if (polarisationModel.is_XY1_Polarised(polarisationMode) || polarisationModel.is_XY2_Polarised(
                        polarisationMode)) {
                    hasXYPolData = true;
                }
            }
            if (hasXPolData) {
                x.add(snapshotIdLong);
            }
            if (hasYPolData) {
                y.add(snapshotIdLong);
            }
            if (hasXYPolData) {
                xy.add(snapshotIdLong);
            }
            snapshotAreaMap.put(snapshotIdLong, snapshotRect);
        }

        return new SnapshotInfo(snapshotIndexMap, all, x, y, xy, snapshotAreaMap);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();

        bufrSupport = new BufrSupport();
        bufrSupport.open(inputFile.getPath());

        grid = new Grid(new ReducedGaussianGrid(512));

        final Product product = bufrSupport.createProduct(inputFile, "SMOS.MIRAS.NRT_BUFR_Light");

        bufrSupport.extractMetaData(product);
        extractScaleFactors();
        readObservations();
        calculateArea();
        addBands(product);

        return product;
    }

    private void calculateArea() throws IOException {
        final List<Point> points = new ArrayList<>();
        final Set<Map.Entry<Integer, List<Observation>>> entries = gridPointMap.entrySet();
        for (Map.Entry<Integer, List<Observation>> next : entries) {
            final List<Observation> value = next.getValue();
            final Observation observation = value.get(0);

            final Point point = new Point(observation.lon, observation.lat);
            points.add(point);
        }

        area = DggUtils.computeArea(new ObservationPointList(points.toArray(new Point[points.size()])));
    }

    private void extractScaleFactors() {
        valueDecoders = new ValueDecoders();

        final SmosBufrFile smosBufrFile = bufrSupport.getSmosBufrFile();
        valueDecoders.lonDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.LONGITUDE_HIGH_ACCURACY);
        valueDecoders.latDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.LATITUDE_HIGH_ACCURACY);
        valueDecoders.incidenceAngleDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.INCIDENCE_ANGLE);
        valueDecoders.tecDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.TOTAL_ELECTRON_COUNT);
        valueDecoders.snapshotAccuracyDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.SNAPSHOT_ACCURACY);
        valueDecoders.raPpDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.RADIOMETRIC_ACCURACY_PP);
        valueDecoders.raCpDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.RADIOMETRIC_ACCURACY_CP);

        valueDecoders.dataDecoders = new ValueDecoder[rawDataNames.length];
        for (int i = 0; i < rawDataNames.length; i++) {
            final ValueDecoder factor = smosBufrFile.getValueDecoder(rawDataNames[i]);
            valueDecoders.dataDecoders[i] = factor;
        }
    }

    private void readObservations() throws IOException {
        snapshotMap = new HashMap<>();
        gridPointMap = new HashMap<>();

        gridPointMinIndex = Integer.MAX_VALUE;
        gridPointMaxIndex = Integer.MIN_VALUE;

        final SmosBufrFile smosBufrFile = bufrSupport.getSmosBufrFile();
        for (int i = 0, messageCount = smosBufrFile.getMessageCount(); i < messageCount; i++) {
            final StructureDataIterator observationIterator = smosBufrFile.getStructureIterator(i);

            while (observationIterator.hasNext()) {
                final StructureData observationData = observationIterator.next();
                final Observation observation = new Observation();

                observation.data[AZIMUTH_ANGLE_INDEX] = observationData.getScalarInt(SmosBufrFile.AZIMUTH_ANGLE);

                final short btReal = observationData.getScalarShort(SmosBufrFile.BRIGHTNESS_TEMPERATURE_REAL_PART);
                observation.data[BT_REAL_INDEX] = DataType.unsignedShortToInt(btReal);

                final short btImag = observationData.getScalarShort(SmosBufrFile.BRIGHTNESS_TEMPERATURE_IMAGINARY_PART);
                observation.data[BT_IMAG_INDEX] = DataType.unsignedShortToInt(btImag);

                observation.data[FARADAY_ANGLE_INDEX] = observationData.getScalarInt(
                        SmosBufrFile.FARADAY_ROTATIONAL_ANGLE);
                observation.data[FOOTPRINT_AXIS_1_INDEX] = observationData.getScalarShort(SmosBufrFile.FOOTPRINT_AXIS_1);
                observation.data[FOOTPRINT_AXIS_2_INDEX] = observationData.getScalarShort(SmosBufrFile.FOOTPRINT_AXIS_2);
                observation.data[GEOMETRIC_ANGLE_INDEX] = observationData.getScalarInt(
                        SmosBufrFile.GEOMETRIC_ROTATIONAL_ANGLE);
                observation.data[INCIDENCE_ANGLE_INDEX] = observationData.getScalarInt(SmosBufrFile.INCIDENCE_ANGLE);
                observation.data[RADIOMETRIC_ACCURACY_INDEX] = observationData.getScalarShort(
                        SmosBufrFile.PIXEL_RADIOMETRIC_ACCURACY);
                observation.data[INFORMATION_FLAG_INDEX] = observationData.getScalarShort(
                        SmosBufrFile.SMOS_INFORMATION_FLAG);
                observation.data[WATER_FRACTION_INDEX] = observationData.getScalarShort(SmosBufrFile.WATER_FRACTION);
                observation.data[POLARISATION_INDEX] = observationData.getScalarByte(SmosBufrFile.POLARISATION);

                final int highAccuracyLon = observationData.getScalarInt(SmosBufrFile.LONGITUDE_HIGH_ACCURACY);
                final float lon = (float) valueDecoders.lonDecoder.decode(highAccuracyLon);
                observation.lon = lon;

                final int highAccuracyLat = observationData.getScalarInt(SmosBufrFile.LATITUDE_HIGH_ACCURACY);
                final float lat = (float) valueDecoders.latDecoder.decode(highAccuracyLat);
                observation.lat = lat;

                observation.cellIndex = grid.getCellIndex(lon, lat);
                addObservationToGridPoints(observation);
                traceGridPointIndexMinMax(grid.getCellIndex(lon, lat));

                final int snapshot_id = observationData.getScalarInt(SmosBufrFile.SNAPSHOT_IDENTIFIER);
                SnapshotObservation snapshotObservation = snapshotMap.get(snapshot_id);

                if (snapshotObservation == null) {
                    snapshotObservation = new SnapshotObservation(new int[snapshotDataNames.length]);
                    snapshotObservation.data[0] = observationData.getScalarShort(SmosBufrFile.NUMBER_OF_GRID_POINTS);
                    snapshotObservation.data[1] = observationData.getScalarShort("Year");
                    snapshotObservation.data[2] = observationData.getScalarByte("Month");
                    snapshotObservation.data[3] = observationData.getScalarByte("Day");
                    snapshotObservation.data[4] = observationData.getScalarByte("Hour");
                    snapshotObservation.data[5] = observationData.getScalarByte("Minute");
                    snapshotObservation.data[6] = observationData.getScalarByte("Second");
                    snapshotObservation.data[7] = observationData.getScalarByte(SmosBufrFile.TOTAL_ELECTRON_COUNT);
                    snapshotObservation.data[8] = observationData.getScalarInt(
                            SmosBufrFile.DIRECT_SUN_BRIGHTNESS_TEMPERATURE);
                    snapshotObservation.data[9] = observationData.getScalarShort(SmosBufrFile.SNAPSHOT_ACCURACY);
                    snapshotObservation.data[10] = observationData.getScalarShort(
                            SmosBufrFile.RADIOMETRIC_ACCURACY_PP);
                    snapshotObservation.data[11] = observationData.getScalarShort(SmosBufrFile.RADIOMETRIC_ACCURACY_CP);
                    final Array snapshot_overall_quality = observationData.getArray(
                            SmosBufrFile.SNAPSHOT_OVERALL_QUALITY);
                    snapshotObservation.data[12] = snapshot_overall_quality.getByte(0);
                    snapshotObservation.observations = new ArrayList<>();
                    snapshotMap.put(snapshot_id, snapshotObservation);
                }

                snapshotObservation.observations.add(observation);
            }
        }
    }

    private void traceGridPointIndexMinMax(int grid_point_index) {
        if (grid_point_index < gridPointMinIndex) {
            gridPointMinIndex = grid_point_index;
        }
        if (grid_point_index > gridPointMaxIndex) {
            gridPointMaxIndex = grid_point_index;
        }
    }

    private void addObservationToGridPoints(Observation observation) {
        List<Observation> gridPointObservations = gridPointMap.get(observation.cellIndex);
        if (gridPointObservations == null) {
            gridPointObservations = new ArrayList<>();
            gridPointMap.put(observation.cellIndex, gridPointObservations);
        }
        gridPointObservations.add(observation);
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {
        synchronized (this) {
            final RenderedImage image = destBand.getSourceImage();
            final Raster data = image.getData(new Rectangle(destOffsetX, destOffsetY, destWidth, destHeight));

            data.getDataElements(destOffsetX, destOffsetY, destWidth, destHeight, destBuffer.getElems());
        }
    }

    @Override
    public void close() throws IOException {
        super.close();

        // just to make sure the garbage collector is not confused by these multiple referenced maps
        gridPointMap.clear();
        snapshotMap.clear();
        snapshotInfo = null;


        if (bufrSupport != null) {
            bufrSupport.close();
            bufrSupport = null;
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

        final Integer index = datasetNameIndexMap.get(descriptor.getMemberName());

        final CellValueProvider valueProvider;
        if (descriptor.getFlagDescriptors() == null) {
            final ValueDecoder scalingFactor = smosBufrFile.getValueDecoder(descriptor.getMemberName());
            valueProvider = new BufrCellValueProvider(descriptor.getPolarization(), index, scalingFactor);
        } else {
            valueProvider = new FlagCellValueProvider(descriptor.getPolarization(), index);
        }
        band.setSourceImage(createSourceImage(band, valueProvider));
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }

    private MultiLevelImage createSourceImage(final Band band, final CellValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(final Band band, final CellValueProvider valueProvider) {
        final MultiLevelModel multiLevelModel = SmosDgg.getInstance().getMultiLevelImage().getModel();
        return new LightBufrMultiLevelSource(multiLevelModel, valueProvider, band);
    }

    private class BufrCellValueProvider implements CellValueProvider {

        private final int dataindex;
        private final int polarisation;
        private final ValueDecoder valueDecoder;
        private int snapshotId;

        private BufrCellValueProvider(int polarisation, int dataIndex, ValueDecoder valueDecoder) {
            this.dataindex = dataIndex;
            this.polarisation = polarisation;
            this.valueDecoder = valueDecoder;
            snapshotId = -1;
        }

        @Override
        public Area getArea() {
            return SmosLightBufrReader.this.area;
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            return SmosLightBufrReader.this.grid.getCellIndex(lon, lat);
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
            if (snapshotId < 0) {
                return getBrowseViewData(cellIndex, noDataValue);
            } else {
                return getSnapshotData(cellIndex, noDataValue);
            }
        }

        private int getSnapshotData(int cellIndex, int noDataValue) {
            final SnapshotObservation snapshotObservation = snapshotMap.get(snapshotId);
            if (snapshotObservation != null) {
                for (final Observation observation : snapshotObservation.observations) {
                    if (observation.cellIndex == cellIndex) {
                        if ((observation.data[POLARISATION_INDEX] & polarisation) == polarisation) {
                            return observation.data[dataindex];
                        }
                    }
                }
            }

            return noDataValue;
        }

        private int getBrowseViewData(int cellIndex, int noDataValue) {
            final List<Observation> cellObservations = gridPointMap.get(cellIndex);
            if (cellObservations != null) {
                int count = 0;
                double sx = 0;
                double sy = 0;
                double sxx = 0;
                double sxy = 0;

                boolean hasLower = false;
                boolean hasUpper = false;
                final ValueDecoder incidenceAngleValueDecoder = valueDecoders.incidenceAngleDecoder;

                for (final Observation observation : cellObservations) {
                    final int value = observation.data[dataindex];
                    if (!valueDecoder.isValid(value)) {
                        continue;
                    }
                    if (polarisation == 4 ||
                            (observation.data[POLARISATION_INDEX] & 3) == polarisation ||
                            (polarisation & observation.data[POLARISATION_INDEX] & 2) != 0) {

                        final int incidenceAngleInt = observation.data[INCIDENCE_ANGLE_INDEX];
                        if (incidenceAngleValueDecoder.isValid(incidenceAngleInt)) {
                            final double incidenceAngle = incidenceAngleValueDecoder.decode(incidenceAngleInt);
                            if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                                sx += incidenceAngle;
                                sy += value;
                                sxx += incidenceAngle * incidenceAngle;
                                sxy += incidenceAngle * value;
                                count++;

                                if (!hasLower) {
                                    hasLower = incidenceAngle <= CENTER_BROWSE_INCIDENCE_ANGLE;
                                }
                                if (!hasUpper) {
                                    hasUpper = incidenceAngle > CENTER_BROWSE_INCIDENCE_ANGLE;
                                }
                            }
                        }
                    }
                }
                if (hasLower && hasUpper) {
                    final double a = (count * sxy - sx * sy) / (count * sxx - sx * sx);
                    final double b = (sy - a * sx) / count;
                    return (int) (a * CENTER_BROWSE_INCIDENCE_ANGLE + b);
                }
            }
            return noDataValue;
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

        private FlagCellValueProvider(int polarisation, int dataIndex) {
            this.dataindex = dataIndex;
            this.polarisation = polarisation;
            snapshotId = -1;
        }

        @Override
        public Area getArea() {
            return SmosLightBufrReader.this.area;
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            return SmosLightBufrReader.this.grid.getCellIndex(lon, lat);
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
            if (snapshotId < 0) {
                return getBrowseViewData(cellIndex, noDataValue);
            } else {
                return getSnapshotData(cellIndex, noDataValue);
            }
        }

        private int getBrowseViewData(int cellIndex, int noDataValue) {
            final List<Observation> cellObservations = gridPointMap.get(cellIndex);
            if (cellObservations != null) {
                final ValueDecoder incidenceAngleValueDecoder = valueDecoders.incidenceAngleDecoder;

                boolean hasLower = false;
                boolean hasUpper = false;
                int combinedFlags = 0;

                for (final Observation observation : cellObservations) {
                    if (polarisation == 4 ||
                            (observation.data[POLARISATION_INDEX] & 3) == polarisation ||
                            (polarisation & observation.data[POLARISATION_INDEX] & 2) != 0) {

                        final int incidenceAngleInt = observation.data[INCIDENCE_ANGLE_INDEX];

                        if (incidenceAngleValueDecoder.isValid(incidenceAngleInt)) {
                            final double incidenceAngle = incidenceAngleValueDecoder.decode(incidenceAngleInt);
                            if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                                combinedFlags |= observation.data[dataindex];

                                if (!hasLower) {
                                    hasLower = incidenceAngle <= CENTER_BROWSE_INCIDENCE_ANGLE;
                                }
                                if (!hasUpper) {
                                    hasUpper = incidenceAngle > CENTER_BROWSE_INCIDENCE_ANGLE;
                                }
                            }
                        }
                    }
                }

                if (hasLower && hasUpper) {
                    return combinedFlags;
                }
            }

            return noDataValue;
        }

        private int getSnapshotData(int cellIndex, int noDataValue) {
            final SnapshotObservation snapshotObservation = snapshotMap.get(snapshotId);
            if (snapshotObservation != null) {
                for (final Observation observation : snapshotObservation.observations) {
                    if (observation.cellIndex == cellIndex) {
                        return observation.data[dataindex];
                    }
                }
            }

            return noDataValue;
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

    private static class ObservationPointList implements PointList {

        private final Point[] points;

        public ObservationPointList(Point[] points) {
            this.points = points;
        }

        @Override
        public int getElementCount() {
            return points.length;
        }

        @Override
        public double getLon(int i) throws IOException {
            return points[i].getLon();
        }

        @Override
        public double getLat(int i) throws IOException {
            return points[i].getLat();
        }
    }

    private static final class Point {

        private final double lon;
        private final double lat;

        public Point(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }
    }
}
