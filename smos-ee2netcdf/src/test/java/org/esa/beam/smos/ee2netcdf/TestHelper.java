package org.esa.beam.smos.ee2netcdf;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {

    public static File getResourceFile(String filename) {
        final Path resourceDirectory = getResourceDirectory();
        return resourceDirectory.resolve(filename).toFile();
    }

    public static Path getResourceDirectory() {
        String resourceName = "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip";
        try {
            return Paths.get(TestHelper.class.getResource(resourceName).toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(String.format("not able to locate resource '%s'", resourceName));
        }
    }
}
