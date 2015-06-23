package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.provider.ValueProvider;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ucar.nc2.Variable;

import java.awt.geom.Area;

public class VariableValueProvider implements ValueProvider {

    public VariableValueProvider(Variable variable) {
    }

    @Override
    public Area getArea() {
        throw new NotImplementedException();
    }

    @Override
    public byte getValue(int seqnum, byte noDataValue) {
        System.out.println(seqnum);
        return 0;
    }

    @Override
    public short getValue(int seqnum, short noDataValue) {
        System.out.println(seqnum);
        return 0;
    }

    @Override
    public int getValue(int seqnum, int noDataValue) {
        System.out.println(seqnum);
        return 0;
    }

    @Override
    public float getValue(int seqnum, float noDataValue) {
        System.out.println(seqnum);
        return 0;
    }
}
