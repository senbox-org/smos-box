package org.esa.smos.ee2netcdf;


import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.dataio.netcdf.nc.NFileWriteable;

import java.io.IOException;

interface FormatExporter {
    void initialize(Product product, ExportParameter exportParameter) throws IOException;

    /**
     * Prepares the exporter for geographic subsetting.
     * @param exportParameter the parameter
     * @return -1 if no geographic subsetting is performed, else the number of grid cells covered by the geometry
     *
     * @throws IOException
     */
    int prepareGeographicSubset(ExportParameter exportParameter) throws IOException;

    void addGlobalAttributes(NFileWriteable nFileWriteable, MetadataElement metadataRoot, ExportParameter exportParameter) throws IOException;

    void addDimensions(NFileWriteable nFileWriteable) throws IOException;

    void addVariables(NFileWriteable nFileWriteable, ExportParameter exportParameter) throws IOException;

    void writeData(NFileWriteable nFileWriteable) throws IOException;
}
