package org.esa.smos.ee2netcdf.reader;


import org.esa.snap.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NetcdfReaderPluginTest {

    private NetcdfReaderPlugin plugIn;

    @Before
    public void setUp() {
        plugIn = new NetcdfReaderPlugin();
    }

    @Test
    public void testGetInputTypes() {
        final Class[] inputTypes = plugIn.getInputTypes();
        assertEquals(2, inputTypes.length);
        assertEquals(File.class, inputTypes[0]);
        assertEquals(String.class, inputTypes[1]);
    }

    @Test
    public void testGetDefaultFileExtensions() {
        final String[] extensions = plugIn.getDefaultFileExtensions();

        assertEquals(1, extensions.length);
        assertEquals(".nc", extensions[0]);
    }

    @Test
    public void testGetDescription() {
        final String description = plugIn.getDescription(null);
        assertEquals("SMOS Data Products in NetCDF Format", description);
    }

    @Test
    public void testGetFormatNames() {
        final String[] formatNames = plugIn.getFormatNames();

        assertEquals(1, formatNames.length);
        assertEquals("SMOS-NC", formatNames[0]);
    }

    @Test
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugIn.getProductFileFilter();
        assertArrayEquals(plugIn.getDefaultFileExtensions(), productFileFilter.getExtensions());
        assertEquals(plugIn.getFormatNames()[0], productFileFilter.getFormatName());

        assertEquals("SMOS Data Products in NetCDF Format (*.nc)", productFileFilter.getDescription());
    }
}
