package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.provider.ValueProvider;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ucar.nc2.Variable;

import java.awt.geom.Area;

public class VariableValueProvider implements ValueProvider {

    private final Area area;

    public VariableValueProvider(Variable variable, Area area) {
        this.area = area;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public byte getValue(int seqnum, byte noDataValue) {
        return 0;
    }

    @Override
    public short getValue(int seqnum, short noDataValue) {
        return 0;
    }

    @Override
    public int getValue(int seqnum, int noDataValue) {
        return 0;
    }

    @Override
    public float getValue(int seqnum, float noDataValue) {
        return 0;
    }
}
