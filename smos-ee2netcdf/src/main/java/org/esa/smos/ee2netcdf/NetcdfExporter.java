package org.esa.smos.ee2netcdf;


import org.esa.smos.dataio.smos.SmosProductReaderPlugIn;
import org.esa.snap.dataio.netcdf.nc.N4FileWriteable;
import org.esa.snap.dataio.netcdf.nc.NFileWriteable;
import org.esa.snap.framework.dataio.DecodeQualification;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

class NetcdfExporter {

    private final ExportParameter parameter;

    NetcdfExporter(ExportParameter parameter) {
        this.parameter = parameter;
    }

    void initialize() {
        ExporterUtils.assertTargetDirectoryExists(parameter.getTargetDirectory());
    }

    void exportProduct(Product product, Logger logger) {
        final File fileLocation = product.getFileLocation();

        NFileWriteable nFileWriteable = null;
        try {
            logger.info("Converting product: " + fileLocation.getPath() + " ...");
            final FormatExporter exporter = FormatExporterFactory.create(fileLocation.getName());
            exporter.initialize(product, parameter);

            final File targetFile = getTargetFile(fileLocation, parameter.getTargetDirectory());
            if (targetFile.isFile()) {
                if (parameter.isOverwriteTarget()) {
                    if (!targetFile.delete()) {
                        throw new IOException("Unable to delete already existing product: " + targetFile.getAbsolutePath());
                    }
                } else {
                    logger.warning("output file '" + targetFile.getPath() + "' exists. Target file will not be overwritten.");
                    return;
                }
            }

            nFileWriteable = N4FileWriteable.create(targetFile.getPath());

            exporter.prepareGeographicSubset(parameter);
            exporter.addDimensions(nFileWriteable);
            exporter.addVariables(nFileWriteable, parameter);
            exporter.addGlobalAttributes(nFileWriteable, product.getMetadataRoot(), parameter);

            nFileWriteable.create();

            exporter.writeData(nFileWriteable);
            logger.info("Success. Wrote target product: " + targetFile.getPath());
        } catch (Exception e) {
            logger.severe("Failed to convert file: " + fileLocation.getAbsolutePath());
            logger.severe(e.getMessage());
        } finally {
            if (nFileWriteable != null) {
                try {
                    nFileWriteable.close();
                } catch (IOException e) {
                    logger.severe("Failed to close file: " + fileLocation.getAbsolutePath());
                    logger.severe(e.getMessage());
                }
            }
        }
    }

    void exportFile(File file, Logger logger) {
        Product product = null;
        try {
            final SmosProductReaderPlugIn readerPlugIn = new SmosProductReaderPlugIn();
            final DecodeQualification decodeQualification = readerPlugIn.getDecodeQualification(file);
            if (decodeQualification == DecodeQualification.INTENDED) {
                product = readerPlugIn.createReaderInstance().readProductNodes(file, null);
                final String productType = product.getProductType();
                if (productType.matches(ExportParameter.PRODUCT_TYPE_REGEX)) {
                    exportProduct(product, logger);
                } else {
                    logger.info("Unable to convert file: " + file.getAbsolutePath());
                    logger.info("Unsupported product of type: " + productType);
                }
            } else {
                logger.warning("Unable to open file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.severe("Failed to convert file: " + file.getAbsolutePath());
            logger.severe(e.getMessage());
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    private static File getTargetFile(File dblFile, File targetDirectory) {
        return FileUtils.exchangeExtension(new File(targetDirectory, dblFile.getName()), ".nc");
    }
}
