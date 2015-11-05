package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.provider.ValueProvider;

import java.awt.geom.Area;
import java.util.HashMap;

class FaradayValueProvider implements ValueProvider {

    private final Area area;

    public FaradayValueProvider(Area area, HashMap<String, ValueProvider> valueProviderMap) {
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
