package org.esa.smos.ee2netcdf.ui;

import com.bc.ceres.binding.ConversionException;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.smos.ee2netcdf.ExportParameter;
import org.esa.smos.gui.BindingConstants;
import org.esa.snap.util.converters.JtsGeometryConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.*;

public class NetcdfExportSwingWorkerTest {

    private ExportParameter exportParameter;

    @Before
    public void setUp() throws Exception {
        exportParameter = new ExportParameter();
    }

    @Test
    public void testCreateMap_sourceDirectory() {
        final File expectedSourceDir = new File("/home/tom");
        exportParameter.setSourceDirectory(expectedSourceDir);

        final HashMap<String, Object> parameterMap = NetcdfExportSwingWorker.createParameterMap(exportParameter);
        final String sourceDirectory = (String) parameterMap.get("sourceProductPaths");
        final String absolutePath = expectedSourceDir.getAbsolutePath();
        assertEquals(absolutePath + File.separator + "*.zip," +
                     absolutePath + File.separator + "*.dbl," +
                     absolutePath + File.separator + "*" + File.separator + "*.dbl",
                     sourceDirectory);
    }

    @Test
    public void testCreateMap_sourceDirectory_NotAddedWhenSingleProductSelected() {
        final File expectedSourceDir = new File("/home/tom");
        exportParameter.setSourceDirectory(expectedSourceDir);
        exportParameter.setUseSelectedProduct(true);

        final HashMap<String, Object> parameterMap = NetcdfExportSwingWorker.createParameterMap(exportParameter);
        assertFalse(parameterMap.containsKey("sourceProductPaths"));
    }

    @Test
    public void testCreateMap_targetDirectory() {
        final File expectedTargetDir = new File("/out/put");
        exportParameter.setTargetDirectory(expectedTargetDir);

        final HashMap<String, Object> parameterMap = NetcdfExportSwingWorker.createParameterMap(exportParameter);
        final File targetDirectory = (File) parameterMap.get("targetDirectory");
        assertEquals(expectedTargetDir.getAbsolutePath(), targetDirectory.getAbsolutePath());
    }

    @Test
    public void testCreateMap_area() {
        exportParameter.setNorthBound(22.9);
        exportParameter.setEastBound(100.6);
        exportParameter.setSouthBound(11.8);
        exportParameter.setWestBound(98.06);
        exportParameter.setRoiType(BindingConstants.ROI_TYPE_BOUNDING_BOX);

        final HashMap<String, Object> parameterMap = NetcdfExportSwingWorker.createParameterMap(exportParameter);
        assertEquals("POLYGON((98.06 22.9,100.6 22.9,100.6 11.8,98.06 11.8,98.06 22.9))",
                     parameterMap.get(BindingConstants.GEOMETRY));
    }

    @Test
    public void testCreateMap_wholeProduct() {
        exportParameter.setRoiType(BindingConstants.ROI_TYPE_ALL);

        final HashMap<String, Object> parameterMap = NetcdfExportSwingWorker.createParameterMap(exportParameter);
        assertNull(parameterMap.get("region"));
    }

    @Test
    public void testCreateMap_overwriteTarget() {
        exportParameter.setOverwriteTarget(true);

        HashMap<String, Object> parameterMap = NetcdfExportSwingWorker.createParameterMap(exportParameter);
        assertEquals("true", parameterMap.get("overwriteTarget"));

        exportParameter.setOverwriteTarget(false);

        parameterMap = NetcdfExportSwingWorker.createParameterMap(exportParameter);
        assertEquals("false", parameterMap.get("overwriteTarget"));
    }

    @Test
    public void testCreateSourcePathWildcards() {
        final File inputDir = new File("data");
        final String pathWildcards = NetcdfExportSwingWorker.createSourcePathWildcards(inputDir);

        final String absolutePath = inputDir.getAbsolutePath();
        assertEquals(absolutePath + File.separator + "*.zip," +
                     absolutePath + File.separator + "*.dbl," +
                     absolutePath + File.separator + "*" + File.separator + "*.dbl",
                     pathWildcards);
    }

    @Test
    public void testAddSelectedGeometry_polygon() throws ConversionException {
        final JtsGeometryConverter converter = new JtsGeometryConverter();
        final Geometry polygon = converter.parse("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        final HashMap<String, Object> parameterMap = new HashMap<>();

        NetcdfExportSwingWorker.addSelectedProductGeometry(polygon, parameterMap);

        final String region = (String) parameterMap.get(BindingConstants.GEOMETRY);
        assertEquals("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))", region);
    }

    @Test
    public void testAddSelectedGeometry_point() throws ConversionException {
        final JtsGeometryConverter converter = new JtsGeometryConverter();
        final Geometry polygon = converter.parse("POINT(4 6))");
        final HashMap<String, Object> parameterMap = new HashMap<>();

        NetcdfExportSwingWorker.addSelectedProductGeometry(polygon, parameterMap);

        assertFalse(parameterMap.containsKey(BindingConstants.GEOMETRY));
    }
}
