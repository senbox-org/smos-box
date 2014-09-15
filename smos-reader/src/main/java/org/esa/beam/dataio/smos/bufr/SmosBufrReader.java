package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.binning.support.ReducedGaussianGrid;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.dataio.netcdf.util.MetadataUtils;
import org.esa.beam.dataio.smos.Grid;
import org.esa.beam.dataio.smos.ProductHelper;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.util.io.FileUtils;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmosBufrReader extends AbstractProductReader {

    private static final String ATTR_NAME_ADD_OFFSET = "add_offset";
    private static final String ATTR_NAME_SCALE_FACTOR = "scale_factor";
    private static final String ATTR_NAME_MISSING_VALUE = "missing_value";

    private NetcdfFile ncfile;
    private ArrayList<Observation> observations;
    private HashMap<Integer, ArrayList<Observation>> snapshotMap;
    private HashMap<Integer, ArrayList<Observation>> gridPointMap;
    private Grid grid;

    SmosBufrReader(SmosBufrReaderPlugin smosBufrReaderPlugin) {
        super(smosBufrReaderPlugin);
        ncfile = null;
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();
        ncfile = NetcdfFile.open(inputFile.getPath());
        grid = new Grid(new ReducedGaussianGrid(512));

        final Product product = createProduct(inputFile);

        extractMetaData(product);
        readObservations();
        addBands(product);

        return product;
    }

    private void readObservations() throws IOException {
        observations = new ArrayList<>();
        snapshotMap = new HashMap<>();
        gridPointMap = new HashMap<>();
        final Sequence observationSequence = getObservationSequence();
        final StructureDataIterator structureIterator = observationSequence.getStructureIterator();
        while (structureIterator.hasNext()) {
            structureIterator.hasNext();
            final StructureData next = structureIterator.next();

            final Observation observation = new Observation();
            observation.data[4] = next.getScalarInt("Azimuth_angle");
            observation.data[1] = next.getScalarShort("Brightness_temperature_imaginary_part");
            observation.data[0] = next.getScalarShort("Brightness_temperature_real_part");
            observation.data[5] = next.getScalarInt("Faraday_rotational_angle");
            observation.data[7] = next.getScalarShort("Footprint_axis_1");
            observation.data[8]= next.getScalarShort("Footprint_axis_2");
            observation.data[6] = next.getScalarInt("Geometric_rotational_angle");
            observation.data[3] = next.getScalarInt("Incidence_angle");
            observation.data[2] = next.getScalarShort("Pixel_radiometric_accuracy");
            observation.data[10] = next.getScalarShort("SMOS_information_flag");
            observation.data[9] = next.getScalarShort("Water_fraction");
            observation.data[11]= next.getScalarByte("Polarisation");

            final int snapshot_id = next.getScalarInt("Snapshot_identifier");
            addObservationToSnapshots(observation, snapshot_id);

//            final int gridpoint_id = next.getScalarInt("Grid_point_identifier");
//            final short number_of_grid_points = next.getScalarShort("Number_of_grid_points");

            // @todo 1 tb/tb scale factors from netcdf variables
            final int longitude_high_accuracy = next.getScalarInt("Longitude_high_accuracy");
            final float lon = longitude_high_accuracy * 1.0e-5f - 180.f;
            observation.lon = lon;

            final int latitude_high_accuracy = next.getScalarInt("Latitude_high_accuracy");
            final float lat = latitude_high_accuracy * 1.0e-5f - 90.f;
            observation.lat = lat;

            final int grid_point_index = grid.getCellIndex(lon, lat);
            addObservationToGridPoints(observation, grid_point_index);
//            System.out.println("lon/lat = " + lon + " " + lat);
//            System.out.println("snap = " + snapshot_id + ", grid = " + gridpoint_id + ", num = " + number_of_grid_points);

            observations.add(observation);
        }
    }

    private void addObservationToGridPoints(Observation observation, int grid_point_index) {
        ArrayList<Observation> gridPointObservations = gridPointMap.get(grid_point_index);
        if (gridPointObservations == null) {
            gridPointObservations = new ArrayList<>();
            gridPointMap.put(grid_point_index, gridPointObservations);
        }
        gridPointObservations.add(observation);
    }

    private void addObservationToSnapshots(Observation observation, int snapshot_id) {
        ArrayList<Observation> snapshotObservations = snapshotMap.get(snapshot_id);
        if (snapshotObservations == null) {
            snapshotObservations = new ArrayList<>();
            snapshotMap.put(snapshot_id, snapshotObservations);
        }
        snapshotObservations.add(observation);
    }

    @Override
    public void readBandRasterData(Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY, Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

    }

    @Override
    public void close() throws IOException {
        // just to make sure the garbage collector is not confused by this multiple referenced maps
        gridPointMap.clear();
        snapshotMap.clear();
        observations.clear();

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
        for (final BandDescriptor d : descriptors.asList()) {
            final Variable v = sequence.findVariable(d.getMemberName());
            if (v.getDataType().isEnum()) {
                final int dataType = ProductData.TYPE_UINT8;
                addBand(product, v, dataType, d);
            } else {
                final int dataType = DataTypeUtils.getRasterDataType(v);
                if (dataType != -1) {
                    addBand(product, v, dataType, d);
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
        if (!descriptor.getValidPixelExpression().isEmpty()) {
            band.setValidPixelExpression(descriptor.getValidPixelExpression());
        }
        if (!descriptor.getDescription().isEmpty()) {
            band.setDescription(descriptor.getDescription());
        }
        if (descriptor.getFlagDescriptors() != null) {
            ProductHelper.addFlagsAndMasks(product, band, descriptor.getFlagCodingName(), descriptor.getFlagDescriptors());
        }

//        final CellValueProvider valueProvider = createCellValueProvider(variable, descriptor.getPolarization());
//        band.setSourceImage(createSourceImage(band, valueProvider));
//        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }

    private static double getAttributeValue(Variable lonVariable, String attributeName, double defaultValue) {
        final Attribute attribute = lonVariable.findAttribute(attributeName);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().doubleValue();
    }


    // @todo 1 tb/tb implement class that is configured by DDDB tb 2014-09-12
    private class Observation {

        private Observation() {
            data = new int[12];
        }

        float lon;
        float lat;
        int[] data;

        // 0: bt_real
        // 1: bt_imag
        // 2: pixel_rad_acc;
        // 3: incidence_angle;
        // 4: azimuth_angle;
        // 5: faraday_rot_angle;
        // 6: geometric_rot_angle;
        // 7: footprint_axis_1;
        // 8: footprint_axis_2;
        // 9: water_fraction;
        // 10: smos_info_flag;
        // 11: polarisation;
    }
}
