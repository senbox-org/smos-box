package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.awt.geom.Area;
import java.io.IOException;

class BrowseValueProvider extends AbstractValueProvider {

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
    public int getGridPointIndex(int seqnum) {
        return gridPointInfo.getGridPointIndex(seqnum);
    }

    @Override
    public byte getByte(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        final Index index = array.getIndex();
        index.set(gridPointIndex, polarization);
        return array.getByte(index);
    }

    @Override
    public short getShort(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        final Index index = array.getIndex();
        index.set(gridPointIndex, polarization);
        return array.getShort(index);
    }

    @Override
    public int getInt(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        final Index index = array.getIndex();
        index.set(gridPointIndex, polarization);
        return array.getInt(index);
    }

    @Override
    public float getFloat(int gridPointIndex) throws IOException {
        final Array array = arrayCache.get(variableName);
        final Index index = array.getIndex();
        index.set(gridPointIndex, polarization);
        return array.getFloat(index);
    }
}
