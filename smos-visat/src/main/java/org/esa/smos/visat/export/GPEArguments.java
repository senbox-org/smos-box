package org.esa.smos.visat.export;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class GPEArguments {

    private Shape roi;
    File targetFile;
    File[] sourceFiles;

    public GPEArguments(String[] args, ErrorHandler errorHandler) {
        try {
            parse(args);
        } catch (IllegalArgumentException e) {
            errorHandler.error(e);
        }
        if (roi == null) {
            roi = createBoxedArea(-180, 180, -90, 90);
        }
    }

    public Shape getRoi() {
        return roi;
    }

    private void parse(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments specified.");
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-box")) {
                if (args.length < i + 6) {
                    throw new IllegalArgumentException("Not enough arguments specified.");
                }
                if (roi != null) {
                    throw new IllegalArgumentException("A ROI may either be defined by '-box' or '-point'.");
                }
                final double minLon = Double.valueOf(args[i + 1]);
                ensureRange("minLon", minLon, -180, 180);
                final double maxLon = Double.valueOf(args[i + 2]);
                ensureRange("maxLon", maxLon, -180, 180);
                if (maxLon <= minLon) {
                    throw new IllegalArgumentException("The value of 'maxLon' must exceed the value of 'minLon'.");
                }
                final double minLat = Double.valueOf(args[i + 3]);
                ensureRange("minLat", minLat, -90, 90);
                final double maxLat = Double.valueOf(args[i + 4]);
                ensureRange("maxLat", maxLat, -90, 90);
                if (maxLat <= minLat) {
                    throw new IllegalArgumentException("The value of 'maxLat' must exceed the value of 'minLat'.");
                }
                roi = createBoxedArea(minLon, maxLon, minLat, maxLat);
                i += 4;
            } else if (args[i].equals("-point")) {
                if (args.length < i + 4) {
                    throw new IllegalArgumentException("Not enough arguments specified.");
                }
                if (roi != null) {
                    throw new IllegalArgumentException("A ROI may either be defined by '-box' or '-point'.");
                }
                final double lon = Double.valueOf(args[i + 1]);
                ensureRange("lon", lon, -180, 180);
                final double lat = Double.valueOf(args[i + 2]);
                ensureRange("lat", lat, -90, 90);
                roi = createPointShape(lon, lat);
                i += 2;
            } else if (args[i].equals("-o")) {
                if (args.length < i + 3) {
                    throw new IllegalArgumentException("Not enough arguments specified.");
                }
                targetFile = new File(args[i + 1]);
                i += 1;
            } else if (args[i].startsWith("-")) {
                throw new IllegalArgumentException("Illegal option.");
            } else {
                final List<File> sourceFileList = new ArrayList<>();
                for (int j = i; j < args.length; j++) {
                    final File sourceFile = new File(args[j]);
                    if (sourceFile.isDirectory()) {
                        sourceFileList.add(sourceFile);
                    }
                }
                sourceFiles = sourceFileList.toArray(new File[sourceFileList.size()]);
                i = args.length - 1;
            }
        }
    }

    private static void ensureRange(String name, double value, double min, double max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The value of ''{0}'' = ''{1}'' is out of range [''{2}'', ''{3}''].", name, value, min, max));
        }
    }

    static Area createBoxedArea(double lon1, double lon2, double lat1, double lat2) {
        return new Area(new Rectangle2D.Double(lon1, lat1, lon2 - lon1, lat2 - lat1));
    }

    static Shape createPointShape(double lon, double lat) {
        return new Rectangle2D.Double(lon, lat, 0, 0);
    }
}

