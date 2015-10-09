package org.esa.smos.ee2netcdf;

import org.esa.snap.core.util.io.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class TestHelper {

    public static Path getResourcePath(String filename) {
        final Path resourceDirectory = getResourceDirectory();
        return resourceDirectory.resolve(filename);
    }

    public static Path getResourceDirectory() {
        String resourceName = "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip";
        try {
            return FileUtils.getPathFromURI(TestHelper.class.getResource(resourceName).toURI()).getParent();
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(String.format("not able to locate resource '%s'", resourceName), e);
        }
    }
}
