package org.esa.smos.ee2netcdf.reader;

import ucar.nc2.NetcdfFile;

import java.io.IOException;

class ProductTypeSupportFactory {

    static ProductTypeSupport get(String typeString, NetcdfFile netcdfFile) throws IOException {
        if (typeString == null) {
            throw new IllegalArgumentException("Invalid product type: null");
        }

        if (isL2Type(typeString)) {
            return new L2ProductSupport(netcdfFile);
        } else if (isBrowseType(typeString)) {
            return new BrowseProductSupport(netcdfFile);
        } else if (isScienceType(typeString)) {
            return new ScienceProductSupport(netcdfFile, typeString);
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
