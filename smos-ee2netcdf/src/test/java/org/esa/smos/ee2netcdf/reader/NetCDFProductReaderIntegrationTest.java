package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.AcceptanceTestRunner;
import org.esa.smos.ee2netcdf.NetcdfExportOp;
import org.esa.snap.framework.dataio.ProductIO;
import org.esa.snap.framework.datamodel.MetadataAttribute;
import org.esa.snap.framework.datamodel.MetadataElement;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.gpf.GPF;
import org.esa.snap.util.io.FileUtils;
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

            assertGlobalMetadataFields(ncProduct);
            assertSmosMetaDataFields(product, ncProduct);


        } finally {
            if (product != null) {
                product.dispose();
            }
            if (ncProduct != null) {
                ncProduct.dispose();
            }
        }
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
        for(int i = 0; i < sourceElement.getNumAttributes(); i++ ) {
            final MetadataAttribute attribute = sourceElement.getAttributeAt(i);
            final MetadataAttribute ncAttribute = ncSourceElement.getAttribute(attribute.getName());
            assertNotNull(ncAttribute);
            assertEquals(attribute.getData().getElemString(), ncAttribute.getData().getElemString());
        }
    }

    private void assertGlobalMetadataFields(Product ncProduct) {
        final MetadataElement metadataRoot = ncProduct.getMetadataRoot();
        final MetadataElement global_attributes = metadataRoot.getElement("Global_Attributes");
        assertNotNull(global_attributes);

        final MetadataAttribute creation_date = global_attributes.getAttribute("creation_date");
        assertNotNull(creation_date);
        final String creation_date_value = creation_date.getData().getElemString();
        assertTrue(creation_date_value.contains("UTC="));

        final MetadataAttribute total_number_of_grid_points = global_attributes.getAttribute("total_number_of_grid_points");
        assertNotNull(total_number_of_grid_points);
        assertEquals("44273", total_number_of_grid_points.getData().getElemString());
    }
}