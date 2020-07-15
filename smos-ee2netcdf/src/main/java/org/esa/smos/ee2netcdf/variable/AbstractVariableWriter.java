package org.esa.smos.ee2netcdf.variable;

import org.esa.snap.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

abstract class AbstractVariableWriter implements VariableWriter {

    // package access for testing only tb 2020-07-15
    Array array;
    protected NVariable variable;

    @Override
    public void close() throws IOException {
        try {
            variable.writeFully(array);
        } catch (Exception e) {
            System.out.println("variable = " + variable.getName());
            throw new IOException(e);
        }
    }
}
