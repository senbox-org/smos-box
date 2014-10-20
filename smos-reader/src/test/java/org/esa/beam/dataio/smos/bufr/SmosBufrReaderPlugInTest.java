package org.esa.beam.dataio.smos.bufr;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class SmosBufrReaderPlugInTest {

    private SmosBufrReaderPlugIn plugin;

    @Before
    public void setUp() {
        plugin = new SmosBufrReaderPlugIn();
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
}
