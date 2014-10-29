package org.esa.beam.smos.gui;


import org.esa.beam.framework.ui.RegionBoundsInputUI;

public class BindingConstants {

    public static final String SELECTED_PRODUCT = "useSelectedProduct";
    public static final String SOURCE_DIRECTORY = "sourceDirectory";
    public static final String TARGET_DIRECTORY = "targetDirectory";
    public static final String OVERWRITE_TARGET = "overwriteTarget";

    public static final String OPEN_FILE_DIALOG = "openFileDialog";
    public static final String GEOMETRY_NODE = "region";
    public static final String GEOMETRY = "geometry";
    public static final String ROI_TYPE = "roiType";

    public static final String NORTH = RegionBoundsInputUI.PROPERTY_NORTH_BOUND;
    public static final String SOUTH = RegionBoundsInputUI.PROPERTY_SOUTH_BOUND;
    public static final String EAST = RegionBoundsInputUI.PROPERTY_EAST_BOUND;
    public static final String WEST = RegionBoundsInputUI.PROPERTY_WEST_BOUND;

    public static final int ROI_TYPE_ALL = 0;
    public static final int ROI_TYPE_REGION = 1;
    public static final int ROI_TYPE_BOUNDING_BOX = 2;

    public static final String USE_SELECTED_PRODUCT_BUTTON_NAME = "Use selected SMOS product";

    public static final String INSTITUTION = "institution";
    public static final String CONTACT = "contact";
    public static final String VARIABLES = "variables";

    public static final String COMPRESSION_LEVEL = "compressionLevel";
}
