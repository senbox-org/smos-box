package org.esa.smos.gui.gridpoint;

import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class GridPointBtDataTableModelTest {

    private GridPointBtDataTableModel model;

    @Before
    public void setUp() {
        model = new GridPointBtDataTableModel();
    }

    @Test
    public void testCreate_defaultValues() {
        assertEquals(0, model.getRowCount());
        assertEquals(0, model.getColumnCount());
        assertNull(model.getValueAt(0, 5));
        assertEquals("", model.getColumnName(3));
        assertEquals(Number.class, model.getColumnClass(3));
    }

    @Test
    public void testDatasetAccess() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();
        model.setGridPointBtDataset(btDataset);

        assertEquals(2, model.getRowCount());
        assertEquals(6, model.getValueAt(0, 2));
        assertEquals(8, model.getValueAt(1, 1));

        assertEquals(String.class, model.getColumnClass(1));
        assertEquals(Double.class, model.getColumnClass(2));
    }

    @Test
    public void testGetValueAt_specialZeroColumnTreatment() {
        assertEquals(5, model.getValueAt(4, 0));
        assertEquals(2, model.getValueAt(1, 0));
    }

    @Test
    public void testGetColumnClass_specialZeroColumnTreatment() {
        assertEquals(Integer.class, model.getColumnClass(0));
    }

    @Test
    public void testColumnAccess() {
        final String[] columnNames = {"one", "two", "three", "four"};
        model.setColumnNames(columnNames);

        assertEquals(5, model.getColumnCount());
        assertEquals("three", model.getColumnName(3));

        assertArrayEquals(columnNames, model.getColumnNames());
    }

    @Test
    public void testGetIndex() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();
        model.setGridPointBtDataset(btDataset);

        assertEquals(3, model.getIndex("Fritz"));
        assertEquals(1, model.getIndex("Heini"));
    }

    @Test
    public void testColumnAccess_specialZeroColumnTreatment() {
        assertEquals("Rec#", model.getColumnName(0));
    }

    private GridPointBtDataset createGridPointBtDataset() {
        final HashMap<String, Integer> memberNamesMap = new HashMap<>();
        memberNamesMap.put("Heini", 1);
        memberNamesMap.put("Veronika", 2);
        memberNamesMap.put("Fritz", 3);
        memberNamesMap.put("Walburga", 4);

        final Class[] classes = {String.class, Double.class};
        final Number[][] data = {{5, 6, 7}, {8, 9, 10}};

        return new GridPointBtDataset(memberNamesMap, classes, data);
    }
}
