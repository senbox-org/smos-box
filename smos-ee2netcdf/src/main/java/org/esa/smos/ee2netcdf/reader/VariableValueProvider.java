package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import ucar.ma2.Array;

import java.awt.geom.Area;
import java.io.IOException;

class VariableValueProvider implements ValueProvider {

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
    public byte getValue(int seqnum, byte noDataValue) {
        int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        try {
            final Array array = arrayCache.get(variableName);
            return array.getByte(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public short getValue(int seqnum, short noDataValue) {
        int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        try {
            final Array array = arrayCache.get(variableName);
            return array.getShort(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public int getValue(int seqnum, int noDataValue) {
        int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        try {
            final Array array = arrayCache.get(variableName);
            return array.getInt(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public float getValue(int seqnum, float noDataValue) {
        int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        try {
            final Array array = arrayCache.get(variableName);
            return array.getFloat(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }
}
