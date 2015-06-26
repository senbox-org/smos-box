package org.esa.smos.ee2netcdf.reader;

class ProductTypeSupportFactory {

    static ProductTypeSupport get(String typeString) {
        if (typeString == null) {
            throw new IllegalArgumentException("Invalid product type: null");
        }

        if (isL2Type(typeString)) {
            return new L2ProductSupport();
        } else if (isBrowseType(typeString)) {
            return new BrowseProductSupport();
        }else if (isScienceType(typeString)) {
            return new ScienceProductSupport();
        }

        throw new IllegalArgumentException("Invalid product type: '" + typeString + "'");
    }

    private static boolean isBrowseType(String typeString) {
        return typeString.matches("MIR_BW[L|N|S][D|F]1C");
    }

    private static boolean isScienceType(String typeString) {
        return typeString.matches("MIR_SC[L|N|S][D|F]1C");
    }

    private static boolean isL2Type(String typeString) {
        return "MIR_SMUDP2".equalsIgnoreCase(typeString) || "MIR_OSUDP2".equalsIgnoreCase(typeString);
    }
}
