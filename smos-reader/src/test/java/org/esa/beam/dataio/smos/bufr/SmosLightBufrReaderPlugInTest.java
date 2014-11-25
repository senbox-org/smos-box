package org.esa.beam.dataio.smos.bufr;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.util.io.BeamFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmosLightBufrReaderPlugInTest {

    private SmosLightBufrReaderPlugIn plugin;

    @Before
    public void setUp() {
        plugin = new SmosLightBufrReaderPlugIn();
    }

    @Test
    public void testGetDecodeQualification() {
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin"));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin")));

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
        assertEquals("SMOS Light-BUFR", formatNames[0]);
    }

    @Test
    public void testGetDefaultFileExtensions() {
        final String[] extensions = plugin.getDefaultFileExtensions();

        assertEquals(1, extensions.length);
        assertEquals(".bin", extensions[0]);
    }

    @Test
    public void testGetDescription() {
         assertEquals("SMOS BUFR light data products", plugin.getDescription(null));
    }

    @Test
    public void testGetProductFileFilter() {
        final BeamFileFilter productFileFilter = plugin.getProductFileFilter();

        assertNotNull(productFileFilter);
        assertEquals(".bin", productFileFilter.getDefaultExtension());
        assertEquals("SMOS Light-BUFR", productFileFilter.getFormatName());
    }

    @Test
    public void testCreateReaderInstance() {
        final ProductReader reader = plugin.createReaderInstance();

        assertNotNull(reader);
        assertTrue(reader instanceof SmosLightBufrReader);
    }
}
