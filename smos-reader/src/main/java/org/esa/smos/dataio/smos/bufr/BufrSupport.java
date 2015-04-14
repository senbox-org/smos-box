package org.esa.smos.dataio.smos.bufr;

import org.esa.smos.dataio.smos.ProductHelper;
import org.esa.snap.dataio.netcdf.util.MetadataUtils;
import org.esa.snap.framework.datamodel.MetadataElement;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.util.io.FileUtils;
import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

class BufrSupport {

    // Grid Point Indices
    static final int BT_REAL_INDEX = 0;
    static final int BT_IMAG_INDEX = 1;
    static final int RADIOMETRIC_ACCURACY_INDEX = 2;
    static final int INCIDENCE_ANGLE_INDEX = 3;
    static final int AZIMUTH_ANGLE_INDEX = 4;
    static final int FARADAY_ANGLE_INDEX = 5;
    static final int GEOMETRIC_ANGLE_INDEX = 6;
    static final int FOOTPRINT_AXIS_1_INDEX = 7;
    static final int FOOTPRINT_AXIS_2_INDEX = 8;
    static final int WATER_FRACTION_INDEX = 9;
    static final int INFORMATION_FLAG_INDEX = 10;
    static final int POLARISATION_INDEX = 11;

    // Snapshot Indices
    static final int TEC_INDEX = 7;
    static final int ACCURACY_INDEX = 9;
    static final int RA_PP_INDEX = 10;
    static final int RA_CP_INDEX = 11;

    private static final HashMap<String, Integer> datasetNameIndexMap;
    static final String[] RAW_DATA_NAMES;
    static final String[] SNAPSHOT_DATA_NAMES;

    static {
        RAW_DATA_NAMES = new String[]{
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

        SNAPSHOT_DATA_NAMES = new String[]{
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
        datasetNameIndexMap.put(RAW_DATA_NAMES[0], BT_REAL_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[1], BT_IMAG_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[2], RADIOMETRIC_ACCURACY_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[3], INCIDENCE_ANGLE_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[4], AZIMUTH_ANGLE_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[5], FARADAY_ANGLE_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[6], GEOMETRIC_ANGLE_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[7], FOOTPRINT_AXIS_1_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[8], FOOTPRINT_AXIS_2_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[9], WATER_FRACTION_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[10], INFORMATION_FLAG_INDEX);
        datasetNameIndexMap.put(RAW_DATA_NAMES[11], POLARISATION_INDEX);
    }


    private SmosBufrFile smosBufrFile;


    void open(String location) throws IOException {
        smosBufrFile = SmosBufrFile.open(location);
    }

    void close() throws IOException {
        if (smosBufrFile != null) {
            smosBufrFile.close();
            smosBufrFile = null;
        }
    }

    SmosBufrFile getSmosBufrFile() {
        return smosBufrFile;
    }

    int getMessageCount() {
        return smosBufrFile.getMessageCount();
    }

    StructureDataIterator getStructureIterator(int index) throws IOException {
        return smosBufrFile.getStructureIterator(index);
    }

    Product createProduct(File inputFile, String productType) {
        final String productName = FileUtils.getFilenameWithoutExtension(inputFile);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(new File(smosBufrFile.getLocation()));
        product.setPreferredTileSize(512, 512);

        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));

        return product;
    }

    void extractMetaData(Product product) {
        final List<Attribute> globalAttributes = smosBufrFile.getGlobalAttributes();
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(MetadataUtils.readAttributeList(globalAttributes, "Global_Attributes"));
        final Sequence sequence = smosBufrFile.getObservationStructure();
        final List<Variable> variables = sequence.getVariables();
        metadataRoot.addElement(MetadataUtils.readVariableDescriptions(variables, "Variable_Attributes", 100));
    }

    ValueDecoders extractValueDecoders() {
        final ValueDecoders valueDecoders = new ValueDecoders();

        valueDecoders.lonDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.LONGITUDE_HIGH_ACCURACY);
        valueDecoders.latDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.LATITUDE_HIGH_ACCURACY);
        valueDecoders.incidenceAngleDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.INCIDENCE_ANGLE);
        valueDecoders.tecDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.TOTAL_ELECTRON_COUNT);
        valueDecoders.snapshotAccuracyDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.SNAPSHOT_ACCURACY);
        valueDecoders.raPpDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.RADIOMETRIC_ACCURACY_PP);
        valueDecoders.raCpDecoder = smosBufrFile.getValueDecoder(SmosBufrFile.RADIOMETRIC_ACCURACY_CP);

        valueDecoders.dataDecoders = new ValueDecoder[RAW_DATA_NAMES.length];
        for (int i = 0; i < RAW_DATA_NAMES.length; i++) {
            final ValueDecoder factor = smosBufrFile.getValueDecoder(RAW_DATA_NAMES[i]);
            valueDecoders.dataDecoders[i] = factor;
        }

        return valueDecoders;
    }

    int getNumDatasets() {
        return datasetNameIndexMap.size();
    }

    static HashMap<String, Integer> getDatasetNameIndexMap() {
        return datasetNameIndexMap;
    }

    static boolean isIntegerBandIndex(int index) {
        return index == INFORMATION_FLAG_INDEX || index == POLARISATION_INDEX;
    }
}
