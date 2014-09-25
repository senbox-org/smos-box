package org.esa.beam.smos.visat.export;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class GridPointExporterTest {

    @Test
    public void testPrintUsageTo() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        GridPointExporter.printUsageTo(new PrintStream(outputStream));

        final String lineSep = System.lineSeparator();
        final String usage = outputStream.toString();
        assertEquals("SMOS-Box Grid Point Export command line tool, version 3.0" + lineSep +
                lineSep +
                "usage : export-grid-points [ROI] [-o targetFile] [sourceProduct ...]" + lineSep +
                lineSep +
                "ROI" + lineSep +
                "    [-box minLon maxLon minLat maxLat] | [-point lon lat]" + lineSep +
                "    a region-of-interest either defined by a latitude-longitude box" + lineSep +
                "    or the coordinates of a DGG grid point" +
                lineSep + lineSep +
                "Note that each source product must be specified by the path name of" + lineSep +
                "the directory which contains the SMOS '.HDR' and '.DBL' files." + lineSep +
                lineSep +
                "targetFile" + lineSep +
                "    If the target file is a directory, the grid point data are exported" + lineSep +
                "      into that directory, the data is stored in EE formatted files." + lineSep +
                "    If the target file is a normal file, the grid point data are stored" + lineSep +
                "      to this file as CSV table." + lineSep +
                "    If no target file is specified, the grid point data are printed to" + lineSep +
                "      the console (in CSV format)." + lineSep, usage);
    }
}
