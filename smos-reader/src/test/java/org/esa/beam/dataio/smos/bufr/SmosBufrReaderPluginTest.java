package org.esa.beam.dataio.smos.bufr;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class SmosBufrReaderPluginTest {

    private static final String BUFR_ENABLED_PROPERTY_NAME = "beam.smos.readLightBUFR";
    private SmosBufrReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new SmosBufrReaderPlugin();
    }

    @Test
    public void testGetDecodeQualification_bufrSupportDisabled() {
        final String oldValue = System.getProperty(BUFR_ENABLED_PROPERTY_NAME);
        System.clearProperty(BUFR_ENABLED_PROPERTY_NAME);

        try {
            assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin"));
            assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin")));

            assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification(new File("firle.fanz")));
            assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification("smos_file.txt"));
        } finally {
            if (StringUtils.isNotNullAndNotEmpty(oldValue)) {
                System.setProperty(BUFR_ENABLED_PROPERTY_NAME, oldValue);
            }
        }
    }

    @Test
    public void testGetDecodeQualification_bufrSupportEnabled() {
        final String oldValue = System.getProperty(BUFR_ENABLED_PROPERTY_NAME);
        System.setProperty(BUFR_ENABLED_PROPERTY_NAME, "true");

        try {
            assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin"));
            assertEquals(DecodeQualification.INTENDED, plugin.getDecodeQualification(new File("W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin")));

            assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification(new File("firle.fanz")));
            assertEquals(DecodeQualification.UNABLE, plugin.getDecodeQualification("smos_file.txt"));
        } finally {
            if (StringUtils.isNotNullAndNotEmpty(oldValue)) {
                System.setProperty(BUFR_ENABLED_PROPERTY_NAME, oldValue);
            }
        }
    }

    @Test
    public void testGetInputTypes() {
        final Class[] inputTypes = plugin.getInputTypes();

        assertEquals(2, inputTypes.length);
        assertEquals(File.class, inputTypes[0]);
        assertEquals(String.class, inputTypes[1]);
    }
}
