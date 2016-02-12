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
package org.esa.smos.gui.export;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.util.SystemUtils;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command line tool for grid point export.
 *
 * @author Ralf Quast
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
public class GridPointExporter {

    private static Logger logger = SystemUtils.LOG;

    public static void main(String[] args) throws IOException {

        final GPEArguments arguments = new GPEArguments(args, new ErrorHandler() {
            @Override
            public void warning(final Throwable t) {
            }

            @Override
            public void error(final Throwable t) {
                System.err.println(t.getMessage());
                printUsageTo(System.out);
                System.exit(1);
            }
        });

        execute(arguments, new ErrorHandler() {
            @Override
            public void warning(final Throwable t) {
                logger.warning(t.getMessage());
            }

            @Override
            public void error(final Throwable t) {
                System.err.println(t.getMessage());
                logger.severe(t.getMessage());
                final StackTraceElement[] stackTraceElements = t.getStackTrace();
                for (final StackTraceElement stackTraceElement : stackTraceElements) {
                    logger.severe(stackTraceElement.toString());
                }
                System.exit(1);
            }
        });
    }

    private static void execute(GPEArguments arguments, ErrorHandler errorHandler) {
        final List<Exception> problemList = new ArrayList<>();
        logger.info(MessageFormat.format("targetFile = {0}", arguments.targetFile));
        final Shape roi = arguments.getRoi();
        logger.info(MessageFormat.format("ROI = {0}", roi.getBounds2D()));

        GridPointFilterStream filterStream = null;
        try {
            filterStream = createGridPointFilterStream(arguments);
            final GridPointFilterStreamHandler streamHandler =
                    new GridPointFilterStreamHandler(filterStream, new RegionFilter(roi));
            for (final File sourceFile : arguments.sourceFiles) {
                try {
                    logger.info(MessageFormat.format("Exporting source file ''{0}''.", sourceFile.getPath()));
                    streamHandler.processDirectory(sourceFile, false, ProgressMonitor.NULL, problemList);
                } catch (Exception e) {
                    errorHandler.warning(e);
                }
            }
        } catch (Exception e) {
            errorHandler.error(e);
        } finally {
            if (!problemList.isEmpty()) {
                for (Exception problem : problemList) {
                    errorHandler.warning(problem);
                }
            }
            if (filterStream != null) {
                try {
                    filterStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static GridPointFilterStream createGridPointFilterStream(GPEArguments arguments) throws IOException {
        if (arguments.targetFile != null) {
            if (arguments.targetFile.isDirectory()) {
                return new EEExportStream(arguments.targetFile);
            } else {
                return new CsvExportStream(new PrintWriter(arguments.targetFile), ";");
            }
        }
        return new CsvExportStream(new PrintWriter(System.out), ";");
    }

    static void printUsageTo(PrintStream outputStream) {
        outputStream.println("SMOS-Box Grid Point Export command line tool, version 3.0");
        outputStream.println();
        outputStream.println("usage : export-grid-points [ROI] [-o targetFile] [sourceProduct ...]");
        outputStream.println();
        outputStream.println("ROI");
        outputStream.println("    [-box minLon maxLon minLat maxLat] | [-point lon lat]");
        outputStream.println("    a region-of-interest either defined by a latitude-longitude box");
        outputStream.println("    or the coordinates of a DGG grid point");
        outputStream.println();
        outputStream.println("Note that each source product must be specified by the path name of");
        outputStream.println("the directory which contains the SMOS '.HDR' and '.DBL' files.");
        outputStream.println();
        outputStream.println("targetFile");
        outputStream.println("    If the target file is a directory, the grid point data are exported");
        outputStream.println("      into that directory, the data is stored in EE formatted files.");
        outputStream.println("    If the target file is a normal file, the grid point data are stored");
        outputStream.println("      to this file as CSV table.");
        outputStream.println("    If no target file is specified, the grid point data are printed to");
        outputStream.println("      the console (in CSV format).");
    }
}
