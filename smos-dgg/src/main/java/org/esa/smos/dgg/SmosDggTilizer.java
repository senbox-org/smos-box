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

package org.esa.smos.dgg;

import com.bc.ceres.multilevel.MultiLevelModel;
import com.bc.ceres.multilevel.MultiLevelSource;
import com.bc.ceres.multilevel.support.DefaultMultiLevelModel;
import com.bc.ceres.multilevel.support.DefaultMultiLevelSource;
import org.esa.snap.core.image.TiledFileOpImage;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.CropDescriptor;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SmosDggTilizer {

    public static void main(String[] args) throws IOException {
        new SmosDggTilizer().doIt(new File(args[0]), new File(args[1]));
    }

    private void doIt(File inputLevel0Dir, File outputDir) throws IOException {
        if (!outputDir.exists() && !outputDir.mkdir()) {
            throw new IOException("Failed to create directory: " + outputDir.getAbsolutePath());
        }

        RenderedImage opImage;
        opImage = TiledFileOpImage.create(inputLevel0Dir, null);
        opImage = CropDescriptor.create(opImage, 0.0f, 64.0f, 16384.0f, 8064.0f, null);
        final int dataType = opImage.getSampleModel().getDataType();
        int tileWidth = 512;
        int tileHeight = 504;
        final int levelCount = 7;
        final MultiLevelModel model = new DefaultMultiLevelModel(levelCount, new AffineTransform(),
                opImage.getWidth(),
                opImage.getHeight());
        final MultiLevelSource multiLevelSource = new DefaultMultiLevelSource(opImage, model);

        for (int level = 0; level < levelCount; level++) {
            final PlanarImage image = PlanarImage.wrapRenderedImage(multiLevelSource.getImage(level));

            final int width = image.getWidth();
            final int height = image.getHeight();

            int numXTiles;
            int numYTiles;
            while (true) {
                numXTiles = width / tileWidth;
                numYTiles = height / tileHeight;
                if (numXTiles * tileWidth == width && numYTiles * tileHeight == image.getHeight()) {
                    break;
                }
                if (numXTiles * tileWidth < width) {
                    tileWidth /= 2;
                }
                if (numYTiles * tileHeight < height) {
                    tileHeight /= 2;
                }
            }
            if (numXTiles == 0 || numYTiles == 0) {
                throw new IllegalStateException("numXTiles == 0 || numYTiles == 0");
            }
            if (tileWidth < 512 && tileHeight < 504) {
                tileWidth = width;
                tileHeight = height;
                numXTiles = numYTiles = 1;
            }

            final File outputLevelDir = new File(outputDir, "" + level);
            if (!outputLevelDir.exists() && !outputLevelDir.mkdir()) {
                throw new IOException("Failed to create directory: " + outputLevelDir.getAbsolutePath());
            }

            final File imagePropertiesFile = new File(outputLevelDir, "image.properties");
            System.out.println("Writing " + imagePropertiesFile + "...");
            final PrintWriter printWriter = new PrintWriter(new FileWriter(imagePropertiesFile));
            writeImageProperties(level, dataType, width, height, tileWidth, tileHeight, numXTiles, numYTiles,
                    new PrintWriter(System.out));
            writeImageProperties(level, dataType, width, height, tileWidth, tileHeight, numXTiles, numYTiles,
                    printWriter);
            System.out.flush();
            printWriter.close();

            writeTiles(outputLevelDir, image, tileWidth, tileHeight, numXTiles, numYTiles);
        }
    }

    private void writeTiles(File levelDir, PlanarImage image, int tileWidth, int tileHeight, int numXTiles,
                            int numYTiles) throws IOException {
        for (int tileY = 0; tileY < numYTiles; tileY++) {
            for (int tileX = 0; tileX < numXTiles; tileX++) {
                final int x = tileX * tileWidth;
                final int y = tileY * tileHeight + image.getMinY();
                final Raster raster = image.getData(new Rectangle(x, y, tileWidth, tileHeight));
                int[] data = ((DataBufferInt) raster.getDataBuffer()).getData();
                if (data.length != tileWidth * tileHeight) {
                    data = new int[tileWidth * tileHeight];
                    raster.getDataElements(x, y, tileWidth, tileHeight, data);
                }
                writeData(levelDir, tileX, tileY, data);
            }
        }
    }

    private void writeImageProperties(int level, int dataType, int width, int height, int tileWidth, int tileHeight,
                                      int numXTiles, int numYTiles, PrintWriter printWriter) {
        printWriter.println("level      = " + level);
        printWriter.println("dataType   = " + dataType);
        printWriter.println("width      = " + width);
        printWriter.println("height     = " + height);
        printWriter.println("tileWidth  = " + tileWidth);
        printWriter.println("tileHeight = " + tileHeight);
        printWriter.println("numXTiles  = " + numXTiles);
        printWriter.println("numYTiles  = " + numYTiles);
    }

    private void writeData(File levelDir, int tileX, int tileY, int[] data) throws IOException {
        final String baseName = tileX + "-" + tileY + ".raw";
        final File file = new File(levelDir, baseName + ".zip");
        final ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
        zipOutputStream.putNextEntry(new ZipEntry(baseName));
        final ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(zipOutputStream);
        imageOutputStream.writeInts(data, 0, data.length);
        imageOutputStream.flush();
        zipOutputStream.closeEntry();
        zipOutputStream.close();
    }
}
