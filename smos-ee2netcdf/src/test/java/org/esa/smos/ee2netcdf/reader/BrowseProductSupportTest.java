package org.esa.smos.ee2netcdf.reader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrowseProductSupportTest {

    private BrowseProductSupport support;

    @Before
    public void setUp() {
        support = new BrowseProductSupport();
    }

    @Test
    public void testGetLatitudeBandName() {
        assertEquals("Grid_Point_Latitude", support.getLatitudeBandName());
    }

    @Test
    public void testGetLongitudeBandName() {
        assertEquals("Grid_Point_Longitude", support.getLongitudeBandName());
    }
}
