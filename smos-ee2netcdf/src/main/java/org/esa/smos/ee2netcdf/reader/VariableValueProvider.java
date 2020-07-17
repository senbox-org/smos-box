package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import ucar.ma2.Array;

import java.awt.geom.Area;
import java.io.IOException;

class VariableValueProvider extends AbstractValueProvider {

    private final Area area;
    private final GridPointInfo gridPointInfo;
    private final ArrayCache arrayCache;
    private final String variableName;

    public VariableValueProvider(ArrayCache arrayCache, String variableName, Area area, GridPointInfo gridPointInfo) {
        this.area = area;
        this.gridPointInfo = gridPointInfo;
        this.arrayCache = arrayCache;
        this.variableName = variableName;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return gridPointInfo.getGridPointIndex(seqnum);
    }

    @Override
    public byte getByte(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        return array.getByte(gridPointIndex);
    }

    @Override
    public short getShort(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        return array.getShort(gridPointIndex);
    }

    @Override
    public int getInt(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        return array.getInt(gridPointIndex);
    }

    @Override
    public float getFloat(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        return array.getFloat(gridPointIndex);
    }
}
