package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.netcdf.util.MetadataUtils;
import org.esa.beam.dataio.smos.ProductHelper;
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

    private NetcdfFile ncfile;
    private ArrayList<Observation> observations;
    private HashMap<Integer, ArrayList<Observation>> snapshotMap;

    SmosBufrReader(SmosBufrReaderPlugin smosBufrReaderPlugin) {
        super(smosBufrReaderPlugin);
        ncfile = null;
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();
        ncfile = NetcdfFile.open(inputFile.getPath());

        final Product product = createProduct(inputFile);

        extractMetaData(product);

        observations = new ArrayList<>();
        snapshotMap = new HashMap<>();
        final Sequence observationSequence = getObservationSequence();
        final StructureDataIterator structureIterator = observationSequence.getStructureIterator();
        while (structureIterator.hasNext()) {
            structureIterator.hasNext();
            final StructureData next = structureIterator.next();

            final Observation observation = new Observation();
            observation.azimuth_angle = next.getScalarInt("Azimuth_angle");
            observation.bt_imag = next.getScalarShort("Brightness_temperature_imaginary_part");
            observation.bt_real = next.getScalarShort("Brightness_temperature_real_part");
            observation.faraday_rot_angle = next.getScalarInt("Faraday_rotational_angle");
            observation.footprint_axis_1 = next.getScalarShort("Footprint_axis_1");
            observation.footprint_axis_2 = next.getScalarShort("Footprint_axis_2");
            observation.geometric_rot_angle = next.getScalarInt("Geometric_rotational_angle");
            observation.incidence_angle = next.getScalarInt("Incidence_angle");
            observation.pixel_rad_acc = next.getScalarShort("Pixel_radiometric_accuracy");
            observation.smos_info_flag = next.getScalarShort("SMOS_information_flag");
            observation.water_fraction = next.getScalarShort("Water_fraction");

            final int snapshot_id = next.getScalarInt("Snapshot_identifier");
            ArrayList<Observation> snapshotObservations = snapshotMap.get(snapshot_id);
            if (snapshotObservations == null) {
                snapshotObservations = new ArrayList<>();
                snapshotMap.put(snapshot_id, snapshotObservations);
            }
            snapshotObservations.add(observation);

//            final int gridpoint_id = next.getScalarInt("Grid_point_identifier");
//            final short number_of_grid_points = next.getScalarShort("Number_of_grid_points");

            final int longitude_high_accuracy = next.getScalarInt("Longitude_high_accuracy");
            final float lon = longitude_high_accuracy * 1.0e-5f - 180.f;

            final int latitude_high_accuracy = next.getScalarInt("Latitude_high_accuracy");
            final float lat = latitude_high_accuracy * 1.0e-5f - 90.f;
//            System.out.println("lon/lat = " + lon + " " + lat);
            //System.out.println("snap = " + snapshot_id + ", grid = " + gridpoint_id + ", num = " + number_of_grid_points);


            observations.add(observation);
        }

        return product;
    }

    @Override
    public void readBandRasterData(Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY, Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

    }

    @Override
    public void close() throws IOException {
        if (ncfile != null) {
            ncfile.close();
            ncfile = null;
        }
    }

    private Product createProduct(File inputFile) {
        final String productName = FileUtils.getFilenameWithoutExtension(inputFile);
        final String productType = "W_ES-ESA-ESAC,SMOS,N256";
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

    private void extractMetaData(Product product) {
        final List<Attribute> globalAttributes = ncfile.getGlobalAttributes();
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(MetadataUtils.readAttributeList(globalAttributes, "Global_Attributes"));
        final Sequence sequence = getObservationSequence();
        final List<Variable> variables = sequence.getVariables();
        metadataRoot.addElement(MetadataUtils.readVariableDescriptions(variables, "Variable_Attributes", 100));
    }

    private class Observation {
        short bt_real;
        short bt_imag;
        short pixel_rad_acc;
        int incidence_angle;
        int azimuth_angle;
        int faraday_rot_angle;
        int geometric_rot_angle;
        short footprint_axis_1;
        short footprint_axis_2;
        short water_fraction;
        short smos_info_flag;
    }
}
