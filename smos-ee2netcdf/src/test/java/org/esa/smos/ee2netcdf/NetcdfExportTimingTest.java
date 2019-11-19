package org.esa.smos.ee2netcdf;

import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ucar.nc2.util.DiskCache;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NetcdfExportTimingTest {

    private final File targetDirectory;

    public NetcdfExportTimingTest() {
        targetDirectory = new File("timing_out");
    }

    @Before
    public void setUp() {
        if (!targetDirectory.mkdirs()) {
            fail("Unable to create test directory");
        }

        // need to move NetCDF cache dir to a directory that gets deleted  tb 2014-07-04
        DiskCache.setRootDirectory(targetDirectory.getAbsolutePath());
        DiskCache.setCachePolicy(true);
    }

    @After
    public void tearDown() {
        if (targetDirectory.isDirectory()) {
            if (!FileUtils.deleteTree(targetDirectory)) {
                fail("Unable to delete test directory");
            }
        }
    }

    @Test
    @Ignore("not for running on the test server, local testing only")
    public void testExportWithTiming() {

        final File file = new File("/usr/local/data/reader_acceptance_tests/sensors_platforms/SMOS/MIR_SMUDP2/SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.zip");

        final HashMap<String, Object> parameterMap = createDefaultParameterMap();
        parameterMap.put("sourceProductPaths", file.getAbsolutePath());
        parameterMap.put("compressionLevel", 0);

        final long start = System.currentTimeMillis();

        GPF.createProduct(NetcdfExportOp.ALIAS,
                parameterMap);

        final long stop = System.currentTimeMillis();

        System.out.println("time (secs) = " + (stop - start) / 1000.0);

        final File outputFile = new File(targetDirectory, "SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.nc");
        assertTrue(outputFile.isFile());
        System.out.println("size (MB)  = " + outputFile.length() / (1024.0 * 1024.0));
    }

    private HashMap<String, Object> createDefaultParameterMap() {
        final HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("targetDirectory", targetDirectory);
        return parameterMap;
    }
}
