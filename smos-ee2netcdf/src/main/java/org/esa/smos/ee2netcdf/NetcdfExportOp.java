package org.esa.smos.ee2netcdf;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProducts;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.esa.snap.core.util.io.FileUtils;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeSet;

@OperatorMetadata(
        alias = NetcdfExportOp.ALIAS,
        version = "5.8.0",
        authors = "Tom Block",
        copyright = "(c) 2014-2020 by Brockmann Consult",
        description = "Exports SMOS Earth Explorer products to NetCDF format.",
        autoWriteDisabled = true)
public class NetcdfExportOp extends Operator {

    public static final String ALIAS = "SmosNetcdfExport";

    @SourceProducts(type = ExportParameter.PRODUCT_TYPE_REGEX,
            description = "The source products to be converted. If not given, the parameter 'sourceProductPaths' must be provided.")
    protected Product[] sourceProducts;

    @Parameter(description = "Comma-separated list of file paths specifying the source products.\n" +
            "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
            "'*' (matches any character sequence in path names) and\n" +
            "'?' (matches any single character).")
    protected String[] sourceProductPaths;

    @Parameter(description = "The target directory for the converted data. If not existing, directory will be created.",
            defaultValue = ".",
            notEmpty = true,
            notNull = true)
    protected File targetDirectory;

    @Parameter(defaultValue = "false",
            description = "Set true to overwrite already existing target files.")
    protected boolean overwriteTarget;

    @Parameter(description = "Target geographical region as a geometry in well-known text format (WKT). The output product will be tailored according to the region.",
            converter = JtsGeometryConverter.class)
    protected Geometry geometry;

    @Parameter(description = "Set institution field for file metadata. If left empty, no institution metadata is written to output file.")
    private String institution;

    @Parameter(description = "Set contact field for file metadata. If left empty, no contact information is written to output file.")
    private String contact;

    @Parameter(defaultValue = "",
            description = "A comma-separated list of variables to be included in the target netCDF file. Variables have to be denoted by names as defined in the ESA SMOS product specification documents. If left empty, all variables in the source file are included in the target file.")
    private String variableNames;

    @Parameter(defaultValue = "6",
            valueSet = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"},
            description = "Output file compression level. 0 = no compression, 9 = highest compression.")
    private int compressionLevel;

    public static Path getOutputFile(Path dblFile, Path targetDirectory) {
        Path outFile = targetDirectory.resolve(dblFile.getFileName().toString());
        return Paths.get(FileUtils.exchangeExtension(outFile.toAbsolutePath().toString(), ".nc"));
    }

    @Override
    public void initialize() throws OperatorException {
        final ExportParameter exportParameter = new ExportParameter();
        exportParameter.setTargetDirectory(targetDirectory);
        exportParameter.setInstitution(institution);
        exportParameter.setContact(contact);
        exportParameter.setOverwriteTarget(overwriteTarget);
        exportParameter.setCompressionLevel(compressionLevel);
        exportParameter.setGeometry(geometry);
        if (StringUtils.isNotNullAndNotEmpty(variableNames)) {
            final String[] variableNames = StringUtils.csvToArray(this.variableNames);
            final ArrayList<String> variableNamesList = new ArrayList<>(variableNames.length);
            for (String variableName : variableNames) {
                variableNamesList.add(variableName.trim());
            }
            exportParameter.setVariableNames(variableNamesList.toArray(new String[variableNamesList.size()]));
        }

        setDummyTargetProduct();

        final NetcdfExporter exporter = new NetcdfExporter(exportParameter);
        exporter.initialize();

        if (sourceProducts != null) {
            for (Product sourceProduct : sourceProducts) {
                exporter.exportProduct(sourceProduct, getLogger());
            }
        }

        if (sourceProductPaths != null) {
            final TreeSet<File> sourceFileSet = ExporterUtils.createInputFileSet(sourceProductPaths);

            for (final File sourceFile : sourceFileSet) {
                exporter.exportFile(sourceFile, getLogger());
            }
        }
    }

    protected void setDummyTargetProduct() {
        final Product product = new Product("dummy", "dummy", 2, 2);
        product.addBand("dummy", ProductData.TYPE_INT8);
        setTargetProduct(product);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(NetcdfExportOp.class);
        }
    }
}
