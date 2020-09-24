package org.esa.smos.dataio.smos.dddb;

import java.io.File;
import java.net.URL;

public class TestHelper {

    static File getResourceFile(String filename) {
        final URL resource = TestHelper.class.getResource(filename);
        return new File(resource.getFile());
    }
}
