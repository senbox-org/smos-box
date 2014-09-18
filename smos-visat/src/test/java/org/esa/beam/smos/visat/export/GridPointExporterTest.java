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

        final String usage = outputStream.toString();
        assertEquals("SMOS-Box Grid Point Export command line tool, version 3.0\n" +
                "\n" +
                "usage : export-grid-points [ROI] [-o targetFile] [sourceProduct ...]\n" +
                "\n" +
                "ROI\n" +
                "    [-box minLon maxLon minLat maxLat] | [-point lon lat]\n" +
                "    a region-of-interest either defined by a latitude-longitude box\n" +
                "    or the coordinates of a DGG grid point\n" +
                "\n" +
                "Note that each source product must be specified by the path name of\n" +
                "the directory which contains the SMOS '.HDR' and '.DBL' files.\n" +
                "\n" +
                "targetFile\n" +
                "    If the target file is a directory, the grid point data are exported\n" +
                "      into that directory, the data is stored in EE formatted files.\n" +
                "    If the target file is a normal file, the grid point data are stored\n" +
                "      to this file as CSV table.\n" +
                "    If no target file is specified, the grid point data are printed to\n" +
                "      the console (in CSV format).\n", usage) ;
    }
}
