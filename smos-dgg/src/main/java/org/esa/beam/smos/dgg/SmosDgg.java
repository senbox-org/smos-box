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

package org.esa.beam.smos.dgg;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.glevel.TiledFileMultiLevelSource;
import org.esa.beam.util.ResourceInstaller;
import org.esa.beam.util.SystemUtils;

import javax.media.jai.PlanarImage;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 * Provides a {@link com.bc.ceres.glevel.MultiLevelImage multi level image} of
 * the SMOS Discrete global grid.
 *
 * @author Marco Peters
 * @author Ralf Quast
 * @version $Revision: $ $Date: $
 * @since SMOS-Box 1.0
 */
public class SmosDgg {

    private static final String SMOS_DGG_DIR_PROPERTY_NAME = "org.esa.beam.smos.dggDir";

    private static final int A = 1000000;
    private static final int B = 262144;
    private static final int C = B + 1;
    private static final int D = A - B;
    private static final int MAX_SEQNUM = 2621442;
    private static final int MAX_ZONE_ID = 10;

    public static final int MIN_GRID_POINT_ID = 1;
    public static final int MAX_GRID_POINT_ID = 9262145;

    private volatile MultiLevelImage dggMultiLevelImage;


    public static SmosDgg getInstance() {
        return Holder.instance;
    }

    public static int gridPointIdToSeqnum(int gridPointId) {
        return gridPointId < A ? gridPointId : gridPointId - D * ((gridPointId - 1) / A) + 1;
    }

    static int gridPointIdToSeqnumInZone(int gridPointId) {
        return gridPointId % A;
    }

    static int gridPointIdToZoneId(int gridPointId) {
        return gridPointId / A + 1;
    }

    public static int seqnumToGridPointId(int seqnum) {
        return seqnum <= C ? seqnum : seqnum == MAX_SEQNUM ? MAX_GRID_POINT_ID : seqnum - 1 + ((seqnum - 2) / B) * D;
    }

    public static int seqnumToSeqnumInZone(int seqnum) {
        return seqnum <= C ? seqnum : seqnum == MAX_SEQNUM ? C : (seqnum - 2) % B + 1;
    }

    public static int seqnumToZoneId(int seqnum) {
        return seqnum <= C ? 1 : seqnum == MAX_SEQNUM ? MAX_ZONE_ID : (seqnum - 2) / B + 1;
    }

    public AffineTransform getImageToMapTransform() {
        return getMultiLevelImage().getModel().getImageToModelTransform(0);
    }

    public MultiLevelImage getMultiLevelImage() {
        return dggMultiLevelImage;
    }

    public int getSeqnum(double lon, double lat) {
        final Point2D p = new Point2D.Double(lon, lat);
        try {
            getImageToMapTransform().inverseTransform(p, p);
        } catch (NoninvertibleTransformException ignored) {
            // cannot happen
        }
        final int pixelX = (int) p.getX();
        final int pixelY = (int) p.getY();
        final PlanarImage image = getMultiLevelImage();
        final int tileX = image.XToTileX(pixelX);
        final int tileY = image.YToTileY(pixelY);
        final Raster data = image.getTile(tileX, tileY);
        if (data == null) {
            return -1;
        }
        return data.getSample(pixelX, pixelY, 0);
    }

    // todo (mp-23.03.15) - installation of auxiliary data should be done in an activator (still needs to be developed)
    private SmosDgg() {
        try {
            Path dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getDggAuxdataPath();
            }
            installDggFiles(dirPath);

            final MultiLevelSource dggMultiLevelSource = TiledFileMultiLevelSource.create(dirPath);
            dggMultiLevelImage = new DefaultMultiLevelImage(dggMultiLevelSource);
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot create SMOS DDG multi-level image: {0}", e.getMessage()), e);
        }
    }

    private void installDggFiles(Path dggAuxdataPath) throws IOException, URISyntaxException {
        new ResourceInstaller(getPathFromModule(), "org/esa/beam/smos/dgg", dggAuxdataPath).install(".*(zip|properties)", ProgressMonitor.NULL);
    }

    private static Path getDggAuxdataPath() {
        return SystemUtils.getApplicationDataDir().toPath().resolve("smos-dgg/grid-tiles");
    }

    private static Path getPathFromModule() throws URISyntaxException, IOException {
          return SystemUtils.getPathFromURI(SmosDgg.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private static Path getDirPathFromProperty() throws IOException {
        final String dirPath = System.getProperty(SMOS_DGG_DIR_PROPERTY_NAME);

        if (dirPath != null) {
            final Path dir = Paths.get(dirPath);
            if (!Files.isReadable(dir)) {
                throw new IOException(MessageFormat.format(
                        "Cannot read directory ''{0}''. System property ''{0}'' must point to a readable directory.",
                        dir, SMOS_DGG_DIR_PROPERTY_NAME));
            }
            return dir;
        }
        return null;
    }

    // Initialization on demand holder idiom

    private static class Holder {

        private static final SmosDgg instance = new SmosDgg();
    }
}
