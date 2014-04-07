package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.dataio.smos.util.DateTimeUtils;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.util.DiskCache;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class GridPointFormatExporterTest {

    private File targetDirectory;
    private GridPointFormatExporter gridPointFormatExporter;

    @Before
    public void setUp() {
        targetDirectory = new File("test_out");

        // need to move NetCDF cache dir to a directory that gets deleted  tb 2014-04-05
        DiskCache.setRootDirectory(targetDirectory.getAbsolutePath());
        DiskCache.setCachePolicy(true);

        gridPointFormatExporter = new GridPointFormatExporter();
    }

    @After
    public void tearDown() {
        if (targetDirectory.isDirectory()) {
            if (!FileUtils.deleteTree(targetDirectory)) {
                fail("Unable to delete test directory");
            }
        }
    }

    @Test
    public void testExportBWLF1C() throws IOException, ParseException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        final File outputFile = new File(targetDirectory, "BWLF1C.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);
            assertGlobalAttribute("Conventions", "CF-1.6", targetFile);
            assertGlobalAttribute("title", "TBD", targetFile);
            assertGlobalAttribute("institution", "TBD", targetFile);
            assertGlobalAttribute("contact", "TBD", targetFile);
            assertCreationDateWithinLast5Minutes(targetFile);
            assertGlobalAttribute("total_number_of_grid_points", "84045", targetFile);

            assertDimension("grid_point_count", 84045, targetFile);
            assertDimension("bt_data_count", 255, targetFile);

        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testExportSCLF1C() throws IOException, ParseException {
        final File file = TestHelper.getResourceFile("SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.zip");
        final File outputFile = new File(targetDirectory, "SCLF1C.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);
            assertGlobalAttribute("Conventions", "CF-1.6", targetFile);
            assertGlobalAttribute("title", "TBD", targetFile);
            assertGlobalAttribute("institution", "TBD", targetFile);
            assertGlobalAttribute("contact", "TBD", targetFile);
            assertCreationDateWithinLast5Minutes(targetFile);
            assertGlobalAttribute("total_number_of_grid_points", "42", targetFile);

            assertDimension("grid_point_count", 42, targetFile);
            assertDimension("bt_data_count", 300, targetFile);

        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testExportOSUDP2() throws IOException, ParseException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");
        final File outputFile = new File(targetDirectory, "OSUDP2.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);

            assertGlobalAttribute("Conventions", "CF-1.6", targetFile);
            assertGlobalAttribute("title", "TBD", targetFile);
            assertGlobalAttribute("institution", "TBD", targetFile);
            assertGlobalAttribute("contact", "TBD", targetFile);
            assertCreationDateWithinLast5Minutes(targetFile);
            assertGlobalAttribute("total_number_of_grid_points", "98564", targetFile);

            assertDimension("grid_point_count", 98564, targetFile);
            assertNoDimension("bt_data_count", targetFile);
        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    private static void assertCreationDateWithinLast5Minutes(NetcdfFile targetFile) throws ParseException {
        final List<Attribute> globalAttributes = targetFile.getGlobalAttributes();

        for(final Attribute globalAttribute: globalAttributes) {
            if (globalAttribute.getFullName().equals("creation_date")) {
                final Date dateFromFile = DateTimeUtils.fromFixedHeaderFormat(globalAttribute.getStringValue());
                final Date now = new Date();
                assertTrue((now.getTime() - dateFromFile.getTime()< 300000));
            }

        }
    }

    // @todo 1 tb/tb test for L1C files
    // Dimensions
    // - radiometric_accuracy_count = 2
    // - bt_data_count = 300
    // - snapshot_count = 4231
    // - grid_point_count = unlimited

    private static void assertGlobalAttribute(String attributeName, String attributeValue, NetcdfFile targetFile) {
        final List<Attribute> globalAttributes = targetFile.getGlobalAttributes();
        for(final Attribute globalAttribute: globalAttributes) {
            if (globalAttribute.getFullName().equals(attributeName)) {
                assertEquals(attributeValue, globalAttribute.getStringValue());
                return;
            }
        }
        fail("Global attribute: '" + attributeName + "' not present");
    }

    private static void assertDimension(String dimensionName, int dimensionLength, NetcdfFile targetFile) {
        final List<Dimension> dimensions = targetFile.getDimensions();
        for (final Dimension dimension : dimensions) {
            if (dimension.getFullName().equals(dimensionName)) {
                assertEquals(dimensionLength, dimension.getLength());
                return;
            }
        }
        fail("file does not contain dimension: " + dimensionName);
    }

    private static void assertNoDimension(String dimensionName, NetcdfFile targetFile) {
        final List<Dimension> dimensions = targetFile.getDimensions();

        for (final Dimension dimension: dimensions) {
            if (dimension.getFullName().equals(dimensionName)) {
                fail("Product contains dimension: '" + dimensionName + "' but shouldn't");
                return;
            }
        }
    }
}