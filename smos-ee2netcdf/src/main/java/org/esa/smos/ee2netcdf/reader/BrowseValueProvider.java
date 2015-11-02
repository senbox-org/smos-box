package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.awt.geom.Area;
import java.io.IOException;

class BrowseValueProvider implements ValueProvider {

    private final Area area;
    private final GridPointInfo gridPointInfo;
    private final int polarization;
    private final ArrayCache arrayCache;
    private final String variableName;

    BrowseValueProvider(ArrayCache arrayCache, String variableName, int polarization, Area area, GridPointInfo gridPointInfo) {
        this.area = area;
        this.gridPointInfo = gridPointInfo;
        this.polarization = polarization;
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
            final Index index = array.getIndex();
            index.set(gridPointIndex, polarization);
            return array.getByte(index);
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
            final Index index = array.getIndex();
            index.set(gridPointIndex, polarization);
            return array.getShort(index);
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
            final Index index = array.getIndex();
            index.set(gridPointIndex, polarization);
            return array.getInt(index);
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
            final Index index = array.getIndex();
            index.set(gridPointIndex, polarization);
            return array.getFloat(index);
        } catch (IOException e) {
            return noDataValue;
        }
    }
}