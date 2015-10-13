package org.esa.smos.ee2netcdf.reader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScienceProductSupportTest {

    private ScienceProductSupport support;

    @Before
    public void setUp() {
         // no specific functionality concerning the NetCDF file tb 2015-06-29
        support = new ScienceProductSupport(null, "MIR_SCSD1C");
    }

    @Test
    public void testGetLatitudeBandName() {
        assertEquals("Grid_Point_Latitude", support.getLatitudeBandName());
    }

    @Test
    public void testGetLongitudeBandName() {
        assertEquals("Grid_Point_Longitude", support.getLongitudeBandName());
    }

    @Test
    public void testCanSupplyGridPointBtData() {
        assertTrue(support.canSupplyGridPointBtData());
    }

    @Test
    public void testCanSupplySnapshotData() {
        assertTrue(support.canSupplySnapshotData());
    }

    @Test
    public void testCanSupplyFullPolData() {
        final ScienceProductSupport dualPole = new ScienceProductSupport(null, "MIR_SCSD1C");
        assertFalse(dualPole.canSupplyFullPolData());

        final ScienceProductSupport fullPole = new ScienceProductSupport(null, "MIR_SCSF1C");
        assertTrue(fullPole.canSupplyFullPolData());
    }

    // @todo 2 tb/tb do not forget to add a  test for band-scaling 2015-06-29
}
