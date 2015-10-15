package org.esa.smos.ee2netcdf.ui;

import org.esa.smos.ee2netcdf.TestHelper;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NetcdfExportDialogTest {

    @Test
    public void testGetTargetFiles_singleProduct() throws IOException {
        final Path resourceFile = TestHelper.getResourcePath("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        String someNotExistingDir = "/some/not/existing/dir";
        final Path targetDir = Paths.get(someNotExistingDir);


        final List<Path> targetFiles = NetcdfExportDialog.getTargetFiles(resourceFile, targetDir);
        assertNotNull(targetFiles);
        assertEquals(1, targetFiles.size());
        assertEquals(getAbsolutePath(someNotExistingDir, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc"), targetFiles.get(0));
    }

    @Test
    public void testGetTargetFiles_directory() throws IOException {
        final Path resourceDirectory = TestHelper.getResourceDirectory();
        String someNotExistingDir = "/some/not/existing/dir";
        final Path targetDir = Paths.get(someNotExistingDir);

        final List<Path> targetFiles = NetcdfExportDialog.getTargetFiles(resourceDirectory, "*.zip", targetDir);
        assertNotNull(targetFiles);
        assertEquals(5, targetFiles.size());
        assertEquals(getAbsolutePath(someNotExistingDir, "SM_OPER_MIR_BWLD1C_20100208T040959_20100208T050400_324_001_1.nc"), targetFiles.get(0));
        assertEquals(getAbsolutePath(someNotExistingDir, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc"), targetFiles.get(1));
        assertEquals(getAbsolutePath(someNotExistingDir, "SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc"), targetFiles.get(2));
        assertEquals(getAbsolutePath(someNotExistingDir, "SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.nc"), targetFiles.get(3));
        assertEquals(getAbsolutePath(someNotExistingDir, "SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.nc"), targetFiles.get(4));
    }

    private Path getAbsolutePath(String someNotExistingDir, String s) {
        return Paths.get(someNotExistingDir, s).toAbsolutePath();
    }

    @Test
    public void testGetExistingFiles_noneExists() {
        final ArrayList<Path> targetFiles = new ArrayList<>();
        targetFiles.add(Paths.get("/fantasy/location/target/file"));
        targetFiles.add(Paths.get("/not/existing/file"));

        final List<Path> existingFiles = NetcdfExportDialog.getExistingPaths(targetFiles);
        assertNotNull(existingFiles);
        assertEquals(0, existingFiles.size());
    }

    @Test
    public void testGetExistingFiles_oneExists() {
        final ArrayList<Path> targetPaths = new ArrayList<>();
        targetPaths.add(Paths.get("/fantasy/location/target/file"));
        targetPaths.add(TestHelper.getResourcePath("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip"));

        final List<Path> existingFiles = NetcdfExportDialog.getExistingPaths(targetPaths);
        assertNotNull(existingFiles);
        assertEquals(1, existingFiles.size());
    }

    @Test
    public void testGetExistingFiles_twoExists() {
        final ArrayList<Path> targetFiles = new ArrayList<>();
        targetFiles.add(TestHelper.getResourcePath("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip"));
        targetFiles.add(Paths.get("/fantasy/location/target/file"));
        targetFiles.add(TestHelper.getResourcePath("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip"));

        final List<Path> existingFiles = NetcdfExportDialog.getExistingPaths(targetFiles);
        assertNotNull(existingFiles);
        assertEquals(2, existingFiles.size());
    }

    @Test
    public void testListToString() {
        final Path file_1 = TestHelper.getResourcePath("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        final Path file_2 = TestHelper.getResourcePath("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");

        final ArrayList<Path> targetFiles = new ArrayList<>();
        targetFiles.add(file_1);
        targetFiles.add(file_2);

        assertEquals(file_1.toAbsolutePath() + "\n" + file_2.toAbsolutePath() + "\n", NetcdfExportDialog.listToString(
                targetFiles));
    }

    @Test
    public void testListToString_ellipseAfterTenFiles() {

        final ArrayList<Path> targetFiles = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            targetFiles.add(Paths.get("blabla_" + i));
        }

        assertTrue(NetcdfExportDialog.listToString(targetFiles).contains("..."));
    }

}

