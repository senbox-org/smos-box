package org.esa.smos.dataio.smos.bufr;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class SmosLightBufrReaderPlugInTest {

    private SmosLightBufrReaderPlugIn plugin;

    @Before
    public void setUp() {
        plugin = new SmosLightBufrReaderPlugIn();
    }

    @Test
    public void testGetDecodeQualification() {
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin"));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin.bz2"));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin")));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin.bz2")));

        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v620.bin"));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v620.bin.bz2"));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v620.bin")));
        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v620.bin.bz2")));

        assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20140626184247_20110505233004_20110506011209_bufr_v620.bin.bz2")));

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

        assertEquals(2, extensions.length);
        assertEquals(".bin", extensions[0]);
        assertEquals(".bin.bz2", extensions[1]);
    }

    @Test
    public void testGetDescription() {
         assertEquals("SMOS Light-BUFR data products", plugin.getDescription(null));
    }

    @Test
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugin.getProductFileFilter();

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
