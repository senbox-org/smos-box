package org.esa.smos.dataio.smos.bufr;

import org.esa.snap.framework.dataio.DecodeQualification;
import org.esa.snap.framework.dataio.ProductReader;
import org.esa.snap.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Ralf Quast
 */
public class SmosBufrReaderPlugInTest {

    private SmosBufrReaderPlugIn plugin;

    @Before
    public void setUp() {
        plugin = new SmosBufrReaderPlugIn();
    }

    @Test
    public void testGetDecodeQualification() {
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification("miras_20131028_003256_20131028_020943_smos_20947_o_20131028_031005_l1c.bufr"));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("miras_20131028_003256_20131028_020943_smos_20947_o_20131028_031005_l1c.bufr")));

        assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification(new File("firle.fanz")));
        assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification("smos_file.txt"));
    }

    @Test
    public void testGetInputTypes() {
        final Class[] inputTypes = plugin.getInputTypes();

        assertEquals(2, inputTypes.length);
        assertEquals(File.class, inputTypes[0]);
        assertEquals(String.class, inputTypes[1]);
    }

    @Test
    public void testGetFormatNames() {
        final String[] formatNames = plugin.getFormatNames();

        assertEquals(1, formatNames.length);
        assertEquals("SMOS BUFR", formatNames[0]);
    }

    @Test
    public void testGetDefaultFileExtensions() {
        final String[] extensions = plugin.getDefaultFileExtensions();

        assertEquals(1, extensions.length);
        assertEquals(".bufr", extensions[0]);
    }

    @Test
    public void testGetDescription() {
         assertEquals("SMOS BUFR data products", plugin.getDescription(null));
    }

    @Test
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugin.getProductFileFilter();

        assertNotNull(productFileFilter);
        assertEquals(".bufr", productFileFilter.getDefaultExtension());
        assertEquals("SMOS BUFR", productFileFilter.getFormatName());
    }

    @Test
    public void testCreateReaderInstance() {
        final ProductReader reader = plugin.createReaderInstance();

        assertNotNull(reader);
        assertTrue(reader instanceof SmosBufrReader);
    }

}
