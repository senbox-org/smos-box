package org.esa.smos.ee2netcdf;

import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.locationtech.jts.geom.Geometry;

import java.io.File;

public class ExportParameter {

    public static final String SELECTED_PRODUCT = "useSelectedProduct";
    public static final String SOURCE_DIRECTORY = "sourceDirectory";
    public static final String TARGET_DIRECTORY = "targetDirectory";

    public static final String OPEN_FILE_DIALOG = "openFileDialog";

    public static final String GEOMETRY = "geometry";
    public static final String ROI_TYPE = "roiType";
    public static final String NORTH_BOUND = "northBound";
    public static final String SOUTH_BOUND = "southBound";
    public static final String WEST_BOUND = "westBound";
    public static final String EAST_BOUND = "eastBound";

    public static final String CONTACT = "contact";
    public static final String INSTITUTION = "institution";
    public static final String VARIABLES = "variables";
    public static final String COMPRESSION_LEVEL = "compressionLevel";
    public static final String OVERWRITE_TARGET = "overwriteTarget";

    public static final int ROI_TYPE_GEOMETRY = 1;

    /**
     * Valid source product types are
     * <p>
     * MIR_SM_SMUDP2
     * MIR_SM_OSUDP2
     * MIR_SM_SCxF1C
     * MIR_SM_SCxD1C
     * MIR_SM_BWxF1C
     * MIR_SM_BWxD1C
     */
    public static final String PRODUCT_TYPE_REGEX = "MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2";

    @Parameter(alias = SELECTED_PRODUCT, defaultValue = "false",
               description = "Convert the selected product to netCDF. Used in GUI mode only.")
    private boolean useSelectedProduct;

    @Parameter(alias = SOURCE_DIRECTORY,
               description = "The source directory. If specified, all files in the source directory are converted to netCDF.",
               label = "Source directory")
    private File sourceDirectory;

    @Parameter(alias = OPEN_FILE_DIALOG, defaultValue = "false",
               description = "Open a file dialog to select a product to be converted to netCDF.")
    private boolean openFileDialog;

    @Parameter(alias = GEOMETRY, converter = JtsGeometryConverter.class,
               defaultValue = "",
               description = "A region-of-interest specified in geographic coordinates using well-known-text (WKT) format. For example: 'POLYGON((<lon1> <lat1>, <lon2> <lat2>, ..., <lon1> <lat1>))'. An empty text is interpreted as 'all', i.e. the Globe.",
               label = "Region")
    private Geometry geometry;

    @Parameter(alias = ROI_TYPE, defaultValue = "0", valueSet = {"0", "1", "2"})
    private int roiType;

    @Parameter(alias = TARGET_DIRECTORY,
               defaultValue = ".",
               description = "The directory where the target netCDF file is put out.",
               label = "Target directory")
    private File targetDirectory;

    @Parameter(alias = NORTH_BOUND, interval = "[-90.0, 90.0]", defaultValue = "90.0",
               description = "The northern bound of the region-of-interest. Used only if the ROI type selected is a latitude-longitude box.")
    private double northBound;

    @Parameter(alias = EAST_BOUND, interval = "[-180.0, 180.0]", defaultValue = "180.0",
               description = "The eastern bound of the region-of-interest. Used only if the ROI type selected is a latitude-longitude box.")
    private double eastBound;

    @Parameter(alias = SOUTH_BOUND, interval = "[-90.0, 90.0]", defaultValue = "-90.0",
               description = "The southern bound of the region-of-interest. Used only if the ROI type selected is a latitude-longitude box.")
    private double southBound;

    @Parameter(alias = WEST_BOUND, interval = "[-180.0, 180.0]", defaultValue = "-180.0",
               description = "The western bound of the region-of-interest. Used only if the ROI type selected is a latitude-longitude box.")
    private double westBound;

    @Parameter(alias = CONTACT,
               description = "The contact address to be included in the global attributes of the target netCDF file.",
               label = "Contact")
    private String contact;

    @Parameter(alias = INSTITUTION,
               description = "The institution to be included in the global attributes of the target netCDF file.",
               label = "Institution")
    private String institution;

    @Parameter(alias = VARIABLES,
               description = "A comma-separated list of variables to be included in the target netCDF file. Variables have to be denoted by names as defined in the ESA SMOS product specification documents. By default all variables in the source file are included in the target file.",
               label = "Variables")
    private String[] variableNames;

    @Parameter(alias = COMPRESSION_LEVEL, defaultValue = "6",
               valueSet = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"},
               description = "Target file compression level. 0 = no compression, 9 = highest compression.")
    private int compressionLevel;

    @Parameter(alias = OVERWRITE_TARGET, defaultValue = "false",
               description = "Overwrite existing target products.", label = "Overwrite existing target products")
    private boolean overwriteTarget;

    public ExportParameter() {
        variableNames = new String[0];
        compressionLevel = 6;
    }

    public void setUseSelectedProduct(boolean useSelectedProduct) {
        this.useSelectedProduct = useSelectedProduct;
    }

    public boolean isUseSelectedProduct() {
        return useSelectedProduct;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setOpenFileDialog(boolean openFileDialog) {
        this.openFileDialog = openFileDialog;
    }

    public boolean isOpenFileDialog() {
        return openFileDialog;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setRoiType(int roiType) {
        this.roiType = roiType;
    }

    public int getRoiType() {
        return roiType;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public String boundingBoxToWKT() {
        final StringBuilder wktBuilder = new StringBuilder(128);
        wktBuilder.append("POLYGON((");
        appendCoordinate(wktBuilder, westBound, northBound, true);
        appendCoordinate(wktBuilder, eastBound, northBound, true);
        appendCoordinate(wktBuilder, eastBound, southBound, true);
        appendCoordinate(wktBuilder, westBound, southBound, true);
        appendCoordinate(wktBuilder, westBound, northBound, false);
        wktBuilder.append("))");

        return wktBuilder.toString();
    }

    public void setNorthBound(double northBound) {
        this.northBound = northBound;
    }

    public double getNorthBound() {
        return northBound;
    }

    public void setEastBound(double eastBound) {
        this.eastBound = eastBound;
    }

    public double getEastBound() {
        return eastBound;
    }

    public void setSouthBound(double southBound) {
        this.southBound = southBound;
    }

    public double getSouthBound() {
        return southBound;
    }

    public void setWestBound(double westBound) {
        this.westBound = westBound;
    }

    public double getWestBound() {
        return westBound;
    }

    public void setOverwriteTarget(boolean overwriteTarget) {
        this.overwriteTarget = overwriteTarget;
    }

    public boolean isOverwriteTarget() {
        return overwriteTarget;
    }

    private void appendCoordinate(StringBuilder wktBuilder, double x, double y, boolean appendComma) {
        wktBuilder.append(x);
        wktBuilder.append(" ");
        wktBuilder.append(y);
        if (appendComma) {
            wktBuilder.append(",");
        }
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getInstitution() {
        return institution;
    }

    public void setVariableNames(String[] variableNames) {
        this.variableNames = variableNames;
    }

    public String[] getVariableNames() {
        return variableNames;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }
}
