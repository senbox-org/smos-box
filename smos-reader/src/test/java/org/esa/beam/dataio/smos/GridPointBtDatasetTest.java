package org.esa.beam.dataio.smos;


import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class GridPointBtDatasetTest {

    @Test
    public void testGetColumnIndex() {
        final GridPointBtDataset gridPointBtDataset = createGridPointBtDataset();

        assertEquals(1, gridPointBtDataset.getColumnIndex("Willy"));
        assertEquals(3, gridPointBtDataset.getColumnIndex("Hermann"));

        assertEquals(-1, gridPointBtDataset.getColumnIndex("Adele"));
    }

    @Test
    public void testGetClasses() {
        final GridPointBtDataset gridPointBtDataset = createGridPointBtDataset();

        final Class[] columnClasses = gridPointBtDataset.getColumnClasses();
        assertEquals(2, columnClasses.length);
        assertEquals(Double.class, columnClasses[1]);
    }

    @Test
    public void testGetData() {
        final GridPointBtDataset gridPointBtDataset = createGridPointBtDataset();

        final Number[][] data = gridPointBtDataset.getData();
        assertEquals(2, data.length);
        assertEquals(1, data[0][0]);
        assertEquals(4, data[1][1]);
    }

    private GridPointBtDataset createGridPointBtDataset() {
        final HashMap<String, Integer> memberNamesMap = new HashMap<>();
        memberNamesMap.put("Willy", 1);
        memberNamesMap.put("Charlotte", 2);
        memberNamesMap.put("Hermann", 3);
        memberNamesMap.put("Astrid", 4);

        final Class[] classes = {String.class, Double.class};
        final Number[][] data = {{1, 2}, {3, 4}};

        return new GridPointBtDataset(memberNamesMap, classes, data);
    }

    @Test
    public void testSetGetFlagBandIndex() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();

        btDataset.setFlagBandIndex(28);
        assertEquals(28, btDataset.getFlagBandIndex());
    }

    @Test
    public void testGetFlagBandIndex_defaultValue() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();

        assertEquals(-1, btDataset.getFlagBandIndex());
    }

    @Test
    public void testSetGetIncidenceAngleBandIndex() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();

        btDataset.setIncidenceAngleBandIndex(29);
        assertEquals(29, btDataset.getIncidenceAngleBandIndex());
    }

    @Test
    public void testGetIncidenceAngleBandIndex_defaultValue() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();

        assertEquals(-1, btDataset.getIncidenceAngleBandIndex());
    }

    @Test
    public void testSetGetRadiometricAccuracyBandIndex() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();

        btDataset.setRadiometricAccuracyBandIndex(30);
        assertEquals(30, btDataset.getRadiometricAccuracyBandIndex());
    }

    @Test
    public void testGetRadiometricAccuracyBandIndex_defaultValue() {
        final GridPointBtDataset btDataset = createGridPointBtDataset();

        assertEquals(-1, btDataset.getRadiometricAccuracyBandIndex());
    }
}
