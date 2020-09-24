package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.AcceptanceTestRunner;
import org.esa.smos.ee2netcdf.NetcdfExportOp;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.util.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.util.DiskCache;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AcceptanceTestRunner.class)
public class NetCDFProductReaderIntegrationTest {

    private static NetcdfExportOp.Spi spi;
    private final File targetDirectory;

    public NetCDFProductReaderIntegrationTest() {
        targetDirectory = new File("test_out");
    }

    @BeforeClass
    public static void setUpClass() {
        spi = new NetcdfExportOp.Spi();
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(spi);

    }

    @Before
    public void setUp() {
        if (!targetDirectory.mkdirs()) {
            fail("Unable to create test directory");
        }

        // need to move NetCDF cache dir to a directory that gets deleted  tb 2014-07-04
        DiskCache.setRootDirectory(targetDirectory.getAbsolutePath());
        DiskCache.setCachePolicy(true);
    }

    @After
    public void tearDown() {
        if (targetDirectory.isDirectory()) {
            if (!FileUtils.deleteTree(targetDirectory)) {
                fail("Unable to delete test directory");
            }
        }
    }

    @AfterClass
    public static void tearDownClass() {
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(spi);
    }

    @Test
    public void testConvertAndReImportSMUDP2() throws IOException {
        final URL resource = NetcdfProductReaderPluginTest.class.getResource("../SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.zip");
        assertNotNull(resource);

        Product product = null;
        Product ncProduct = null;
        try {
            product = ProductIO.readProduct(resource.getFile());
            assertNotNull(product);

            final HashMap<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("targetDirectory", targetDirectory);

            GPF.createProduct(NetcdfExportOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File ncFile = new File(targetDirectory, "SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.nc");
            assertTrue(ncFile.isFile());

            ncProduct = ProductIO.readProduct(ncFile);
            assertNotNull(ncProduct);

            final ProductReader productReader = ncProduct.getProductReader();
            assertNotNull(productReader);
            assertTrue(productReader instanceof NetcdfProductReader);

            assertGlobalMetadataFields(ncProduct, 44273);
            assertSmosMetaDataFields(product, ncProduct);

            assertEquals(product.getNumBands(), ncProduct.getNumBands());

            compareBand(product, ncProduct, "AFP", 2296, 7640);
            compareBand(product, ncProduct, "Dielect_Const_Non_MD_RE", 15869, 1594);
            compareBand(product, ncProduct, "N_RFI_X", 16167, 909);
            compareBand(product, ncProduct, "Surface_Temperature", 4205, 7141);
            compareBand(product, ncProduct, "Roughness_Param", 3154, 7625);
            compareBand(product, ncProduct, "Dielect_Const_MD_RE_DQX", 2345, 7523);

        } finally {
            if (product != null) {
                product.dispose();
            }
            if (ncProduct != null) {
                ncProduct.dispose();
            }
        }
    }

    @Test
    public void testConvertAndReImportOSUDP2() throws IOException {
        final URL resource = NetcdfProductReaderPluginTest.class.getResource("../SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");
        assertNotNull(resource);

        Product product = null;
        Product ncProduct = null;
        try {
            product = ProductIO.readProduct(resource.getFile());
            assertNotNull(product);

            final HashMap<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("targetDirectory", targetDirectory);

            GPF.createProduct(NetcdfExportOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File ncFile = new File(targetDirectory, "SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc");
            assertTrue(ncFile.isFile());

            ncProduct = ProductIO.readProduct(ncFile);
            assertNotNull(ncProduct);

            final ProductReader productReader = ncProduct.getProductReader();
            assertNotNull(productReader);
            assertTrue(productReader instanceof NetcdfProductReader);

            assertGlobalMetadataFields(ncProduct, 98564);
            assertSmosMetaDataFields(product, ncProduct);

            assertEquals(product.getNumBands(), ncProduct.getNumBands());

            compareBand(product, ncProduct, "SSS1", 11998, 5323);
            compareBand(product, ncProduct, "Sigma_SSS2", 9599, 597);
            compareBand(product, ncProduct, "Acard", 12884, 6675);
            compareBand(product, ncProduct, "Sigma_WS", 11802, 4315);
            compareBand(product, ncProduct, "TBH", 11504, 3307);
            compareBand(product, ncProduct, "Sigma_TBV", 9697, 597);

        } finally {
            if (product != null) {
                product.dispose();
            }
            if (ncProduct != null) {
                ncProduct.dispose();
            }
        }
    }

    @Test
    public void testConvertAndReImportBWLF1C() throws IOException {
        final URL resource = NetcdfProductReaderPluginTest.class.getResource("../SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        assertNotNull(resource);

        Product product = null;
        Product ncProduct = null;
        try {
            product = ProductIO.readProduct(resource.getFile());
            assertNotNull(product);

            final HashMap<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("targetDirectory", targetDirectory);

            GPF.createProduct(NetcdfExportOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File ncFile = new File(targetDirectory, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc");
            assertTrue(ncFile.isFile());

            ncProduct = ProductIO.readProduct(ncFile);
            assertNotNull(ncProduct);

            final ProductReader productReader = ncProduct.getProductReader();
            assertNotNull(productReader);
            assertTrue(productReader instanceof NetcdfProductReader);

            assertGlobalMetadataFields(ncProduct, 84045);
            assertSmosMetaDataFields(product, ncProduct);

            assertEquals(product.getNumBands(), ncProduct.getNumBands());

            compareBand(product, ncProduct, "BT_Value_X", 6505, 7687);
            compareBand(product, ncProduct, "BT_Value_XY_Imag", 8754, 7314);
            compareBand(product, ncProduct, "Pixel_Radiometric_Accuracy_XY", 9908, 6158);
            compareBand(product, ncProduct, "Azimuth_Angle_XY", 10240, 4845);
            compareBand(product, ncProduct, "Footprint_Axis1_XY", 10506, 2946);
            compareBand(product, ncProduct, "Footprint_Axis2_XY", 11037, 1706);

        } finally {
            if (product != null) {
                product.dispose();
            }
            if (ncProduct != null) {
                ncProduct.dispose();
            }
        }
    }

    @Test
    public void testConvertAndReImportBWLD1C() throws IOException {
        final URL resource = NetcdfProductReaderPluginTest.class.getResource("../SM_OPER_MIR_BWLD1C_20100208T040959_20100208T050400_324_001_1.zip");
        assertNotNull(resource);

        Product product = null;
        Product ncProduct = null;
        try {
            product = ProductIO.readProduct(resource.getFile());
            assertNotNull(product);

            final HashMap<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("targetDirectory", targetDirectory);

            GPF.createProduct(NetcdfExportOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File ncFile = new File(targetDirectory, "SM_OPER_MIR_BWLD1C_20100208T040959_20100208T050400_324_001_1.nc");
            assertTrue(ncFile.isFile());

            ncProduct = ProductIO.readProduct(ncFile);
            assertNotNull(ncProduct);

            final ProductReader productReader = ncProduct.getProductReader();
            assertNotNull(productReader);
            assertTrue(productReader instanceof NetcdfProductReader);

            assertGlobalMetadataFields(ncProduct, 384);
            assertSmosMetaDataFields(product, ncProduct);

            assertEquals(product.getNumBands(), ncProduct.getNumBands());

            compareBand(product, ncProduct, "BT_Value_X", 8304, 1998);
            compareBand(product, ncProduct, "Pixel_Radiometric_Accuracy_X", 8215, 1942);
            compareBand(product, ncProduct, "Azimuth_Angle_X", 8335, 1947);
            compareBand(product, ncProduct, "Footprint_Axis1_X", 8350, 2099);
            compareBand(product, ncProduct, "Footprint_Axis2_Y", 8302, 2100);
            compareBand(product, ncProduct, "Flags_Y", 8282, 2007);

        } finally {
            if (product != null) {
                product.dispose();
            }
            if (ncProduct != null) {
                ncProduct.dispose();
            }
        }
    }

    @Test
    public void testConvertAndReImportSCLF1C() throws IOException {
        final URL resource = NetcdfProductReaderPluginTest.class.getResource("../SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.zip");
        assertNotNull(resource);

        Product product = null;
        Product ncProduct = null;
        try {
            product = ProductIO.readProduct(resource.getFile());
            assertNotNull(product);

            final HashMap<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("targetDirectory", targetDirectory);

            GPF.createProduct(NetcdfExportOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File ncFile = new File(targetDirectory, "SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.nc");
            assertTrue(ncFile.isFile());

            ncProduct = ProductIO.readProduct(ncFile);
            assertNotNull(ncProduct);

            final ProductReader productReader = ncProduct.getProductReader();
            assertNotNull(productReader);
            assertTrue(productReader instanceof NetcdfProductReader);

            assertGlobalMetadataFields(ncProduct, 42);
            assertSmosMetaDataFields(product, ncProduct);

            assertEquals(product.getNumBands(), ncProduct.getNumBands());
            compareBand(product, ncProduct, "BT_Value_X", 8020, 7464);
            compareBand(product, ncProduct, "BT_Value_XY_Imag", 8754, 7314);
            compareBand(product, ncProduct, "Pixel_Radiometric_Accuracy_XY", 9908, 6158);
            compareBand(product, ncProduct, "Azimuth_Angle_XY", 10240, 4845);
            compareBand(product, ncProduct, "Footprint_Axis1_XY", 10506, 2946);
            compareBand(product, ncProduct, "Footprint_Axis2_XY", 11037, 1706);

        } finally {
            if (product != null) {
                product.dispose();
            }
            if (ncProduct != null) {
                ncProduct.dispose();
            }
        }
    }

    private void compareBand(Product product, Product ncProduct, String bandName, int pixelX, int pixelY) throws IOException {
        final Band afpBand = product.getBand(bandName);
        assertNotNull(afpBand);
        final Band afpNcBand = ncProduct.getBand(bandName);
        assertNotNull(afpNcBand);

        assertEquals(afpBand.getDescription(), afpNcBand.getDescription());
        assertEquals(afpBand.getUnit(), afpNcBand.getUnit());

        final double[] afp = new double[1];
        afpBand.readPixels(pixelX, pixelY, 1, 1, afp);

        final double[] afpNc = new double[1];
        afpNcBand.readPixels(pixelX, pixelY, 1, 1, afpNc);

        assertEquals(afp[0], afpNc[0], 1e-8);
    }

    private void assertSmosMetaDataFields(Product product, Product ncProduct) {
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement ncMetadataRoot = ncProduct.getMetadataRoot();

        MetadataElement sourceElement = metadataRoot.getElement("Fixed_Header").getElement("Source");
        MetadataElement ncSourceElement = ncMetadataRoot.getElement("Fixed_Header").getElement("Source");
        assertSameAttributes(sourceElement, ncSourceElement);

        sourceElement = metadataRoot.getElement("Variable_Header").getElement("Specific_Product_Header").getElement("Main_Info");
        ncSourceElement = ncMetadataRoot.getElement("Variable_Header").getElement("Specific_Product_Header").getElement("Main_Info");
        assertSameAttributes(sourceElement, ncSourceElement);
    }

    private void assertSameAttributes(MetadataElement sourceElement, MetadataElement ncSourceElement) {
        for (int i = 0; i < sourceElement.getNumAttributes(); i++) {
            final MetadataAttribute attribute = sourceElement.getAttributeAt(i);
            final MetadataAttribute ncAttribute = ncSourceElement.getAttribute(attribute.getName());
            assertNotNull(ncAttribute);
            assertEquals(attribute.getData().getElemString(), ncAttribute.getData().getElemString());
        }
    }

    private void assertGlobalMetadataFields(Product ncProduct, int gridPointCount) {
        final MetadataElement metadataRoot = ncProduct.getMetadataRoot();
        final MetadataElement global_attributes = metadataRoot.getElement("Global_Attributes");
        assertNotNull(global_attributes);

        final MetadataAttribute creation_date = global_attributes.getAttribute("creation_date");
        assertNotNull(creation_date);
        final String creation_date_value = creation_date.getData().getElemString();
        assertTrue(creation_date_value.contains("UTC="));

        final MetadataAttribute total_number_of_grid_points = global_attributes.getAttribute("total_number_of_grid_points");
        assertNotNull(total_number_of_grid_points);
        assertEquals(Integer.toString(gridPointCount), total_number_of_grid_points.getData().getElemString());
    }
}
