package org.esa.smos.ee2netcdf.reader;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;

class ArrayCache {

    private final NetcdfFile netcdfFile;
    private final HashMap<String, Array> cache;

    ArrayCache(NetcdfFile netcdfFile) {
        this.netcdfFile = netcdfFile;

        cache = new HashMap<>();
    }

    Array get(String variableName) throws IOException {
        Array array = cache.get(variableName);
        if (array == null) {
            final Variable variable = netcdfFile.findVariable(null, variableName);
            array =  variable.read();
            cache.put(variableName, array);
        }

        return array;
    }
}
