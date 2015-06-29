package org.esa.smos.ee2netcdf.reader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrowseProductSupportTest {

    private BrowseProductSupport support;

    @Before
    public void setUp() {
        // no specific functionality concerning the NetCDF file tb 2015-06-29
        support = new BrowseProductSupport(null);
    }

    @Test
    public void testGetLatitudeBandName() {
        assertEquals("Grid_Point_Latitude", support.getLatitudeBandName());
    }

    @Test
    public void testGetLongitudeBandName() {
        assertEquals("Grid_Point_Longitude", support.getLongitudeBandName());
    }

    // @todo 2 tb/tb do not forget to add a  test for band-scaling 2015-06-29
}
