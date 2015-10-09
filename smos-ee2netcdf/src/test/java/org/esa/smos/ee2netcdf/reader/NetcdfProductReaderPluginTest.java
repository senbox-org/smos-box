package org.esa.smos.ee2netcdf.reader;


import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class NetcdfProductReaderPluginTest {

    private NetcdfProductReaderPlugin plugIn;

    @Before
    public void setUp() {
        plugIn = new NetcdfProductReaderPlugin();
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

    @Test
    public void testGetDecodeQualification_wrongExtension() {
        final File undecodeableFile = new File("ignore_the_name.txt");

        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(undecodeableFile));
    }

    @Test
    public void testGetDecodeQualification_correctFilePattern() {
        final File bwlf1cFie = new File("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(bwlf1cFie));

        final File smudp2Fie = new File("SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.nc");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(smudp2Fie));
    }

    @Test
    public void testGetDecodeQualification_correctFileWrongName() {
        final URL resource = NetcdfProductReaderPluginTest.class.getResource("../TEST_SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.nc");
        assertNotNull(resource);

        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(resource.getFile()));
    }

    @Test
    public void testCreateReaderInstance() {
        final ProductReader readerInstance = plugIn.createReaderInstance();

        assertNotNull(readerInstance);
        assertTrue(readerInstance instanceof NetcdfProductReader);
    }
}
