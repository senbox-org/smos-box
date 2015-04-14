package org.esa.smos.dataio.smos.dddb;

import java.io.File;

public class TestHelper {

    public static File getResourceFile(String filename) {
        final File resourceDirectory = getResourceDirectory();
        return new File(resourceDirectory, filename);
    }

    public static File getResourceDirectory() {
        File resourceDir = new File("./smos-box/smos-reader/src/test/resources/org/esa/snap/dataio/smos/dddb/");
        if (!resourceDir.isDirectory()) {
            resourceDir = new File("./src/test/resources/org/esa/snap/dataio/smos/dddb/");
        }
        return resourceDir;
    }
}
