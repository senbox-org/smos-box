package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.binning.support.ReducedGaussianGrid;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.dataio.netcdf.util.MetadataUtils;
import org.esa.beam.dataio.smos.*;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.StringUtils;
import org.esa.beam.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class SmosBufrReader extends SmosReader {

    private static final String ATTR_NAME_ADD_OFFSET = "add_offset";
    private static final String ATTR_NAME_SCALE_FACTOR = "scale_factor";
    private static final String ATTR_NAME_MISSING_VALUE = "missing_value";
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
                "Brightness_temperature_real_part",
                "Brightness_temperature_imaginary_part",
                "Pixel_radiometric_accuracy",
                "Incidence_angle",
                "Azimuth_angle",
                "Faraday_rotational_angle",
                "Geometric_rotational_angle",
                "Footprint_axis_1",
                "Footprint_axis_2",
                "Water_fraction",
                "SMOS_information_flag",
                "Polarisation"
        };

        snapshotDataNames = new String[]{
                "Number_of_grid_points",
                "Year",
                "Month",
                "Day",
                "Hour",
                "Minute",
                "Second",
                "Total_electron_count_per_square_metre",
                "Direct_sun_brightness_temperature",
                "Snapshot_accuracy",
                "Radiometric_accuracy_pure_polarisation",
                "Radiometric_accuracy_cross_polarisation",
                "Snapshot_overall_quality"
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

    private NetcdfFile ncfile;
    private HashMap<Integer, SnapshotObservation> snapshotMap;
    private HashMap<Integer, ArrayList<Observation>> gridPointMap;
    private Grid grid;
    private Area area;
    private ScaleFactors scaleFactors;
    private int gridPointMinIndex;
    private int gridPointMaxIndex;
    private LinkedList<Integer> snapshotIdlist;
    private SnapshotInfo snapshotInfo;

    SmosBufrReader(SmosBufrReaderPlugIn smosBufrReaderPlugIn) {
        super(smosBufrReaderPlugIn);
        ncfile = null;
        gridPointMinIndex = -1;
        gridPointMaxIndex = -1;
    }

    @Override
    public GridPointBtDataset getBtData(int gridPointIndex) {
        if (gridPointIndex < 0) {
            return null;
        }

        final ArrayList<Observation> gridPointData = gridPointMap.get(gridPointIndex);
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
                    currentMeasures[k] = scaleFactors.bandScaleFactors[k].scale(observation.data[k]);
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
        final Integer snapshotId = snapshotIdlist.get(snapshotIndex);
        final SnapshotObservation snapshotObservation = snapshotMap.get(snapshotId);
        if (snapshotObservation == null) {
            return new Object[0][];
        }

        final Object[][] snapshotData = new Object[snapshotDataNames.length][2];
        for (int i = 0; i < snapshotDataNames.length; i++) {
            snapshotData[i][0] = snapshotDataNames[i];
            if (i == TEC_INDEX) {
                snapshotData[i][1] = scaleFactors.tec.scale(snapshotObservation.data[i]);
            } else if (i == ACCURACY_INDEX) {
                snapshotData[i][1] = scaleFactors.accuracy.scale(snapshotObservation.data[i]);
            } else if (i == RA_PP_INDEX) {
                snapshotData[i][1] = scaleFactors.ra_pp.scale(snapshotObservation.data[i]);
            } else if (i == RA_CP_INDEX) {
                snapshotData[i][1] = scaleFactors.ra_cp.scale(snapshotObservation.data[i]);
            } else {
                snapshotData[i][1] = snapshotObservation.data[i];
            }
        }

        return snapshotData;
    }

    private SnapshotInfo createSnapshotInfo() {
        final Set<Integer> snapshotIds = snapshotMap.keySet();
        snapshotIdlist = new LinkedList<>(snapshotIds);

        Collections.sort(snapshotIdlist);

        final Set<Long> all = new TreeSet<>();
        final Set<Long> x = new TreeSet<>();
        final Set<Long> y = new TreeSet<>();
        final Set<Long> xy = new TreeSet<>();
        final Map<Long, Integer> snapshotIndexMap = new TreeMap<>();
        final Map<Long, Rectangle2D> snapshotAreaMap = new TreeMap<>();

        final PolarisationModel polarisationModel = getPolarisationModel();
        int index = 0;
        for (final Integer snapshotId : snapshotIdlist) {
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

                if (polarisationModel.is_XY1_Polarised(polarisationMode) || polarisationModel.is_XY2_Polarised(polarisationMode)) {
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
        ncfile = NetcdfFile.open(inputFile.getPath());
        grid = new Grid(new ReducedGaussianGrid(512));

        final Product product = createProduct(inputFile);

        extractMetaData(product);
        extractScaleFactors();
        readObservations();
        calculateArea();
        addBands(product);

        return product;
    }

    private void calculateArea() throws IOException {
        final ArrayList<Point> points = new ArrayList<>();
        final Set<Map.Entry<Integer, ArrayList<Observation>>> entries = gridPointMap.entrySet();
        for (Map.Entry<Integer, ArrayList<Observation>> next : entries) {
            final ArrayList<Observation> value = next.getValue();
            final Observation observation = value.get(0);

            final Point point = new Point(observation.lon, observation.lat);
            points.add(point);
        }

        area = DggUtils.computeArea(new ObservationPointList(points.toArray(new Point[points.size()])));
    }

    private void extractScaleFactors() {
        final Sequence sequence = getObservationSequence();
        scaleFactors = new ScaleFactors();

        scaleFactors.lon = createFactor(sequence, "Longitude_high_accuracy");
        scaleFactors.lat = createFactor(sequence, "Latitude_high_accuracy");
        scaleFactors.incidenceAngle = createFactor(sequence, "Incidence_angle");

        scaleFactors.tec = createFactor(sequence, "Total_electron_count_per_square_metre");
        scaleFactors.accuracy = createFactor(sequence, "Snapshot_accuracy");
        scaleFactors.ra_pp = createFactor(sequence, "Radiometric_accuracy_pure_polarisation");
        scaleFactors.ra_cp = createFactor(sequence, "Radiometric_accuracy_cross_polarisation");

        scaleFactors.bandScaleFactors = new ScaleFactor[rawDataNames.length];
        for (int i = 0; i < rawDataNames.length; i++) {
            final ScaleFactor factor = createFactor(sequence, rawDataNames[i]);
            scaleFactors.bandScaleFactors[i] = factor;
        }
    }

    private ScaleFactor createFactor(Sequence sequence, String variableName) {
        final Variable variable = sequence.findVariable(variableName);

        final double scale = getAttributeValue(variable, ATTR_NAME_SCALE_FACTOR, 1.0);
        final double offset = getAttributeValue(variable, ATTR_NAME_ADD_OFFSET, 0.0);
        final double missingValueDouble = getAttributeValue(variable, ATTR_NAME_MISSING_VALUE, Double.NaN);
        int missingValue = -1;
        if (!Double.isNaN(missingValueDouble)) {
            missingValue = (int) missingValueDouble;
        }

        return new ScaleFactor(scale, offset, missingValue);
    }

    private void readObservations() throws IOException {
        snapshotMap = new HashMap<>();
        gridPointMap = new HashMap<>();
        final Sequence observationSequence = getObservationSequence();
        final StructureDataIterator structureIterator = observationSequence.getStructureIterator();

        gridPointMinIndex = Integer.MAX_VALUE;
        gridPointMaxIndex = Integer.MIN_VALUE;
        while (structureIterator.hasNext()) {
            structureIterator.hasNext();
            final StructureData next = structureIterator.next();

            final Observation observation = new Observation();
            observation.data[AZIMUTH_ANGLE_INDEX] = next.getScalarInt("Azimuth_angle");

            final short bt_imag = next.getScalarShort("Brightness_temperature_imaginary_part");
            observation.data[BT_IMAG_INDEX] = DataType.unsignedShortToInt(bt_imag);

            final short bt_real = next.getScalarShort("Brightness_temperature_real_part");
            observation.data[BT_REAL_INDEX] = DataType.unsignedShortToInt(bt_real);

            observation.data[FARADAY_ANGLE_INDEX] = next.getScalarInt("Faraday_rotational_angle");
            observation.data[FOOTPRINT_AXIS_1_INDEX] = next.getScalarShort("Footprint_axis_1");
            observation.data[FOOTPRINT_AXIS_2_INDEX] = next.getScalarShort("Footprint_axis_2");
            observation.data[GEOMETRIC_ANGLE_INDEX] = next.getScalarInt("Geometric_rotational_angle");
            observation.data[INCIDENCE_ANGLE_INDEX] = next.getScalarInt("Incidence_angle");
            observation.data[RADIOMETRIC_ACCURACY_INDEX] = next.getScalarShort("Pixel_radiometric_accuracy");
            observation.data[INFORMATION_FLAG_INDEX] = next.getScalarShort("SMOS_information_flag");
            observation.data[WATER_FRACTION_INDEX] = next.getScalarShort("Water_fraction");
            observation.data[POLARISATION_INDEX] = next.getScalarByte("Polarisation");

            final int longitude_high_accuracy = next.getScalarInt("Longitude_high_accuracy");
            final float lon = (float) scaleFactors.lon.scale(longitude_high_accuracy);
            observation.lon = lon;

            final int latitude_high_accuracy = next.getScalarInt("Latitude_high_accuracy");
            final float lat = (float) scaleFactors.lat.scale(latitude_high_accuracy);
            observation.lat = lat;

            observation.cellIndex = grid.getCellIndex(lon, lat);
            addObservationToGridPoints(observation);
            traceGridPointIndexMinMax(grid.getCellIndex(lon, lat));

            final int snapshot_id = next.getScalarInt("Snapshot_identifier");
            SnapshotObservation snapshotObservation = snapshotMap.get(snapshot_id);
            if (snapshotObservation == null) {
                snapshotObservation = new SnapshotObservation(new int[snapshotDataNames.length]);
                snapshotObservation.data[0] = next.getScalarShort("Number_of_grid_points");
                snapshotObservation.data[1] = next.getScalarShort("Year");
                snapshotObservation.data[2] = next.getScalarByte("Month");
                snapshotObservation.data[3] = next.getScalarByte("Day");
                snapshotObservation.data[4] = next.getScalarByte("Hour");
                snapshotObservation.data[5] = next.getScalarByte("Minute");
                snapshotObservation.data[6] = next.getScalarByte("Second");
                snapshotObservation.data[7] = next.getScalarByte("Total_electron_count_per_square_metre");
                snapshotObservation.data[8] = next.getScalarInt("Direct_sun_brightness_temperature");
                snapshotObservation.data[9] = next.getScalarShort("Snapshot_accuracy");
                snapshotObservation.data[10] = next.getScalarShort("Radiometric_accuracy_pure_polarisation");
                snapshotObservation.data[11] = next.getScalarShort("Radiometric_accuracy_cross_polarisation");
                final Array snapshot_overall_quality = next.getArray("Snapshot_overall_quality");
                snapshotObservation.data[12] = snapshot_overall_quality.getByte(0);
                snapshotObservation.observations = new ArrayList<>();
                snapshotMap.put(snapshot_id, snapshotObservation);
            }
            snapshotObservation.observations.add(observation);


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
        ArrayList<Observation> gridPointObservations = gridPointMap.get(observation.cellIndex);
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
        // just to make sure the garbage collector is not confused by these multiple referenced maps
        gridPointMap.clear();
        snapshotMap.clear();
        snapshotInfo = null;

        if (ncfile != null) {
            ncfile.close();
            ncfile = null;
        }
    }

    private Product createProduct(File inputFile) {
        final String productName = FileUtils.getFilenameWithoutExtension(inputFile);
        final String productType = "SMOS.MIRAS.NRT_BUFR_Light";
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(new File(ncfile.getLocation()));
        product.setPreferredTileSize(512, 512);

        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));

        return product;
    }

    private File getInputFile() {
        final Object input = getInput();

        if (input instanceof String) {
            return new File((String) input);
        }
        if (input instanceof File) {
            return (File) input;
        }

        throw new IllegalArgumentException(MessageFormat.format("Illegal input: {0}", input));
    }

    private Sequence getObservationSequence() {
        return (Sequence) ncfile.findVariable("obs");
    }

    // @todo 2 tb/tb duplicated code - is copied from old BUFR lightreader tb 2014-09-12
    private void extractMetaData(Product product) {
        final List<Attribute> globalAttributes = ncfile.getGlobalAttributes();
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(MetadataUtils.readAttributeList(globalAttributes, "Global_Attributes"));
        final Sequence sequence = getObservationSequence();
        final List<Variable> variables = sequence.getVariables();
        metadataRoot.addElement(MetadataUtils.readVariableDescriptions(variables, "Variable_Attributes", 100));
    }

    private void addBands(Product product) throws IOException {
        final Sequence sequence = getObservationSequence();
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
        final double addOffset = getAttributeValue(variable, ATTR_NAME_ADD_OFFSET, 0.0);
        if (addOffset != 0.0) {
            band.setScalingOffset(addOffset);
        }
        final double scaleFactor = getAttributeValue(variable, ATTR_NAME_SCALE_FACTOR, 1.0);
        if (scaleFactor != 1.0) {
            band.setScalingFactor(scaleFactor);
        }
        final Attribute missingValue = variable.findAttribute(ATTR_NAME_MISSING_VALUE);
        if (missingValue != null) {
            band.setNoDataValue(missingValue.getNumericValue().doubleValue());
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
            final ScaleFactor scalingFactor = createFactor(getObservationSequence(), descriptor.getMemberName());
            valueProvider = new BufrCellValueProvider(descriptor.getPolarization(), index, scalingFactor);
        } else {
            valueProvider = new FlagCellValueProvider(descriptor.getPolarization(), index);
        }
        band.setSourceImage(createSourceImage(band, valueProvider));
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }

    private static double getAttributeValue(Variable lonVariable, String attributeName, double defaultValue) {
        final Attribute attribute = lonVariable.findAttribute(attributeName);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().doubleValue();
    }

    private MultiLevelImage createSourceImage(final Band band, final CellValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(final Band band, final CellValueProvider valueProvider) {
        final MultiLevelModel multiLevelModel = SmosDgg.getInstance().getMultiLevelImage().getModel();
        return new BufrMultiLevelSource(multiLevelModel, valueProvider, band);
    }

    private class BufrCellValueProvider implements CellValueProvider {

        private final int dataindex;
        private final int polarisation;
        private final ScaleFactor scaleFactor;
        private int snapshotId;

        private BufrCellValueProvider(int polarisation, int dataIndex, ScaleFactor scaleFactor) {
            this.dataindex = dataIndex;
            this.polarisation = polarisation;
            this.scaleFactor = scaleFactor;
            snapshotId = -1;
        }

        @Override
        public Area getArea() {
            return SmosBufrReader.this.area;
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            return SmosBufrReader.this.grid.getCellIndex(lon, lat);
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
            final ArrayList<Observation> cellObservations = gridPointMap.get(cellIndex);
            if (cellObservations != null) {
                int count = 0;
                double sx = 0;
                double sy = 0;
                double sxx = 0;
                double sxy = 0;

                boolean hasLower = false;
                boolean hasUpper = false;
                final ScaleFactor incidenceAngleScaleFactor = scaleFactors.incidenceAngle;

                for (final Observation observation : cellObservations) {
                    final int value = observation.data[dataindex];
                    if (!scaleFactor.isValid(value)) {
                        continue;
                    }
                    if (polarisation == 4 ||
                            (observation.data[POLARISATION_INDEX] & 3) == polarisation ||
                            (polarisation & observation.data[POLARISATION_INDEX] & 2) != 0) {

                        final int incidenceAngleInt = observation.data[INCIDENCE_ANGLE_INDEX];
                        if (incidenceAngleScaleFactor.isValid(incidenceAngleInt)) {
                            final double incidenceAngle = incidenceAngleScaleFactor.scale(incidenceAngleInt);
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
            return SmosBufrReader.this.area;
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            return SmosBufrReader.this.grid.getCellIndex(lon, lat);
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
            final ArrayList<Observation> cellObservations = gridPointMap.get(cellIndex);
            if (cellObservations != null) {
                final ScaleFactor incidenceAngleScaleFactor = scaleFactors.incidenceAngle;

                boolean hasLower = false;
                boolean hasUpper = false;
                int combinedFlags = 0;

                for (final Observation observation : cellObservations) {
                    if (polarisation == 4 ||
                            (observation.data[POLARISATION_INDEX] & 3) == polarisation ||
                            (polarisation & observation.data[POLARISATION_INDEX] & 2) != 0) {

                        final int incidenceAngleInt = observation.data[INCIDENCE_ANGLE_INDEX];

                        if (incidenceAngleScaleFactor.isValid(incidenceAngleInt)) {
                            final double incidenceAngle = incidenceAngleScaleFactor.scale(incidenceAngleInt);
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
