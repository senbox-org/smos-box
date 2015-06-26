package org.esa.smos.ee2netcdf.reader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class L2ProductTypeSupportTest {

    private L2ProductSupport support;

    @Before
    public void setUp() {
        support = new L2ProductSupport();
    }

    @Test
    public void testGetLatitudeBandName() {
        assertEquals("Latitude", support.getLatitudeBandName());
    }

    @Test
    public void testGetLongitudeBandName() {
        assertEquals("Longitude", support.getLongitudeBandName());
    }
}
