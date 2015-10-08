package org.esa.smos.gui.export;

import org.esa.smos.gui.BindingConstants;
import org.esa.snap.framework.gpf.annotations.Parameter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class GridPointExportParameterTest {

    private GridPointExportParameter gridPointExportParameter;

    @Before
    public void setUp() {
        gridPointExportParameter = new GridPointExportParameter();
    }

    @Test
    public void testSetIsUseSelectedProduct() {
        gridPointExportParameter.setUseSelectedProduct(true);
        assertTrue(gridPointExportParameter.isUseSelectedProduct());

        gridPointExportParameter.setUseSelectedProduct(false);
        assertFalse(gridPointExportParameter.isUseSelectedProduct());
    }

    @Test
    public void testUseSelectedProductAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField(BindingConstants.SELECTED_PRODUCT);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SELECTED_PRODUCT, parameter.alias());
    }

    @Test
    public void testSetGetSourceDirectory() {
        final File sourceDirectory = new File("where/ever/my/source");

        gridPointExportParameter.setSourceDirectory(sourceDirectory);
        assertEquals(sourceDirectory.getPath(), gridPointExportParameter.getSourceDirectory().getPath());
    }

    @Test
    public void testSourceDirectoryAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField(BindingConstants.SOURCE_DIRECTORY);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SOURCE_DIRECTORY, parameter.alias());
    }

    @Test
    public void testSetIsOpenFileDialog() {
        gridPointExportParameter.setOpenFileDialog(true);
        assertTrue(gridPointExportParameter.isOpenFileDialog());

        gridPointExportParameter.setOpenFileDialog(false);
        assertFalse(gridPointExportParameter.isOpenFileDialog());
    }

    @Test
    public void testOpenFileDialogAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField(BindingConstants.OPEN_FILE_DIALOG);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.OPEN_FILE_DIALOG, parameter.alias());
    }

    @Test
    public void testSetIsRecursive() {
        gridPointExportParameter.setRecursive(true);
        assertTrue(gridPointExportParameter.isRecursive());

        gridPointExportParameter.setRecursive(false);
        assertFalse(gridPointExportParameter.isRecursive());
    }

    @Test
    public void testRecursiveAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField(GridPointExportDialog.ALIAS_RECURSIVE);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(GridPointExportDialog.ALIAS_RECURSIVE, parameter.alias());
        assertEquals("false", parameter.defaultValue());
    }

    @Test
    public void testSetGetRoiType() {
        gridPointExportParameter.setRoiType(5);
        assertEquals(5, gridPointExportParameter.getRoiType());
    }

    @Test
    public void testRoiTypeAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField(BindingConstants.ROI_TYPE);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.ROI_TYPE, parameter.alias());
        assertEquals("2", parameter.defaultValue());
        assertArrayEquals(new String[]{"0", "1", "2"}, parameter.valueSet());
    }

    @Test
    public void testSetGetNorth() {
        gridPointExportParameter.setNorth(56.22);
        assertEquals(56.22, gridPointExportParameter.getNorth(), 1e-8);

        gridPointExportParameter.setNorth(-19.55);
        assertEquals(-19.55, gridPointExportParameter.getNorth(), 1e-8);
    }

    @Test
    public void testNorthAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField("north");
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.NORTH, parameter.alias());
        assertEquals("90.0", parameter.defaultValue());
        assertEquals("[-90.0, 90.0]", parameter.interval());
    }

    @Test
    public void testSetGetSouth() {
        gridPointExportParameter.setSouth(-22.65);
        assertEquals(-22.65, gridPointExportParameter.getSouth(), 1e-8);

        gridPointExportParameter.setSouth(3.018);
        assertEquals(3.018, gridPointExportParameter.getSouth(), 1e-8);
    }

    @Test
    public void testSouthAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField("south");
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SOUTH, parameter.alias());
        assertEquals("-90.0", parameter.defaultValue());
        assertEquals("[-90.0, 90.0]", parameter.interval());
    }

    @Test
    public void testSetGetEast() {
        gridPointExportParameter.setEast(29.01);
        assertEquals(29.01, gridPointExportParameter.getEast(), 1e-8);

        gridPointExportParameter.setEast(-11.5);
        assertEquals(-11.5, gridPointExportParameter.getEast(), 1e-8);
    }

    @Test
    public void testEastAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField("east");
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.EAST, parameter.alias());
        assertEquals("180.0", parameter.defaultValue());
        assertEquals("[-180.0, 180.0]", parameter.interval());
    }

    @Test
    public void testSetGetWest() {
        gridPointExportParameter.setWest(30.02);
        assertEquals(30.02, gridPointExportParameter.getWest(), 1e-8);

        gridPointExportParameter.setWest(-12.6);
        assertEquals(-12.6, gridPointExportParameter.getWest(), 1e-8);
    }

    @Test
    public void testWestAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField("west");
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.WEST, parameter.alias());
        assertEquals("-180.0", parameter.defaultValue());
        assertEquals("[-180.0, 180.0]", parameter.interval());
    }

    @Test
    public void testSetGetTargetFile() {
        final File targetFile = new File("target/file");

        gridPointExportParameter.setTargetFile(targetFile);
        assertEquals(targetFile.getPath(), gridPointExportParameter.getTargetFile().getPath());
    }

    @Test
    public void testTargetFileAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField("targetFile");
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(GridPointExportDialog.ALIAS_TARGET_FILE, parameter.alias());
        assertTrue(parameter.notNull());
        assertTrue(parameter.notEmpty());
    }

    @Test
    public void testSetGetExportFormat() {
        gridPointExportParameter.setExportFormat("wurst");
        assertEquals("wurst", gridPointExportParameter.getExportFormat());

        gridPointExportParameter.setExportFormat("xls");
        assertEquals("xls", gridPointExportParameter.getExportFormat());
    }

    @Test
    public void testExportFormatAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = GridPointExportParameter.class.getDeclaredField(GridPointExportDialog.ALIAS_EXPORT_FORMAT);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(GridPointExportDialog.ALIAS_EXPORT_FORMAT, parameter.alias());
        assertEquals(GridPointExportDialog.NAME_CSV, parameter.defaultValue());
        assertArrayEquals(new String[]{GridPointExportDialog.NAME_CSV, GridPointExportDialog.NAME_EEF}, parameter.valueSet());
    }

    @Test
    public void testGetClone() {
        final File sourceDirectory = new File("source/dir");
        final File targetFile = new File("target/file");

        gridPointExportParameter.setUseSelectedProduct(true);
        gridPointExportParameter.setSourceDirectory(sourceDirectory);
        gridPointExportParameter.setOpenFileDialog(true);
        gridPointExportParameter.setRecursive(true);
        gridPointExportParameter.setRoiType(6);
        gridPointExportParameter.setNorth(7.1);
        gridPointExportParameter.setSouth(8.2);
        gridPointExportParameter.setEast(9.3);
        gridPointExportParameter.setWest(10.4);
        gridPointExportParameter.setTargetFile(targetFile);
        gridPointExportParameter.setExportFormat("word");

        final GridPointExportParameter clone = gridPointExportParameter.getClone();
        assertNotNull(clone);
        assertNotSame(clone, gridPointExportParameter);

        assertEquals(gridPointExportParameter.isUseSelectedProduct(), clone.isUseSelectedProduct());
        assertEquals(sourceDirectory.getPath(), clone.getSourceDirectory().getPath());
        assertEquals(gridPointExportParameter.isOpenFileDialog(), clone.isOpenFileDialog());
        assertEquals(gridPointExportParameter.isRecursive(), clone.isRecursive());
        assertEquals(gridPointExportParameter.getRoiType(), clone.getRoiType());
        assertEquals(gridPointExportParameter.getNorth(), clone.getNorth(), 1e-8);
        assertEquals(gridPointExportParameter.getSouth(), clone.getSouth(), 1e-8);
        assertEquals(gridPointExportParameter.getEast(), clone.getEast(), 1e-8);
        assertEquals(gridPointExportParameter.getWest(), clone.getWest(), 1e-8);
        assertEquals(targetFile.getPath(), clone.getTargetFile().getPath());
        assertEquals(gridPointExportParameter.getExportFormat(), clone.getExportFormat());
    }
}
