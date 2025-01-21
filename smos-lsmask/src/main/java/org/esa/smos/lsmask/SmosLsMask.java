/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.smos.lsmask;


import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.multilevel.MultiLevelImage;
import com.bc.ceres.multilevel.MultiLevelSource;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import org.esa.snap.core.image.TiledFileMultiLevelSource;
import org.esa.snap.core.util.ResourceInstaller;
import org.esa.snap.core.util.SystemUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

public class SmosLsMask {

    private static final String SMOS_LS_MASK_DIR_PROPERTY_NAME = "org.esa.smos.lsMaskDir";

    private final MultiLevelImage multiLevelImage;

    public static SmosLsMask getInstance() {
        return Holder.INSTANCE;
    }

    public MultiLevelImage getMultiLevelImage() {
        return multiLevelImage;
    }

    // todo (mp-29.09.15) - installation of auxiliary data should be done in an activator (org.esa.snap.runtime.Activator)
    private SmosLsMask() {
        try {
            Path dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getLsMaskAuxdataPath();
            }
            installLsMaskFiles(dirPath);

            final MultiLevelSource multiLevelSource = TiledFileMultiLevelSource.create(dirPath);
            multiLevelImage = new DefaultMultiLevelImage(multiLevelSource);
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot create SMOS Land/Sea Mask multi-level image: {0}", e.getMessage()), e);
        }
    }

    private static Path getPathFromModule() throws URISyntaxException, IOException {
        return ResourceInstaller.findModuleCodeBasePath(SmosLsMask.class);
    }

    private void installLsMaskFiles(Path auxdataPath) throws IOException, URISyntaxException {
        Path resolve = getPathFromModule().resolve("org/esa/smos/lsmask");
        new ResourceInstaller(resolve, auxdataPath).install(".*(zip|properties)", ProgressMonitor.NULL);
    }

    private static Path getLsMaskAuxdataPath() {
        return SystemUtils.getAuxDataPath().resolve("smos-lsmask/mask-tiles");
    }

    private static Path getDirPathFromProperty() throws IOException {
        final String dirPath = System.getProperty(SMOS_LS_MASK_DIR_PROPERTY_NAME);

        if (dirPath != null) {
            final Path dir = Paths.get(dirPath);
            if (!Files.isReadable(dir)) {
                throw new IOException(MessageFormat.format(
                        "Cannot read directory ''{0}''. System property ''{0}'' must point to a readable directory.",
                        dir, SMOS_LS_MASK_DIR_PROPERTY_NAME));
            }
            return dir;
        }
        return null;
    }


    // Initialization on demand holder idiom

    private static class Holder {

        private static final SmosLsMask INSTANCE = new SmosLsMask();
    }
}
