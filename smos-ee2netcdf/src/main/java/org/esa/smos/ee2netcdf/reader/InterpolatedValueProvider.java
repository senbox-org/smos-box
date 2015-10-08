package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Area;
import java.io.IOException;

class InterpolatedValueProvider implements ValueProvider {

    private final Area area;
    private final GridPointInfo gridPointInfo;
    private final String variableName;
    private final ArrayCache arrayCache;
    private final int polarization;

    public InterpolatedValueProvider(ArrayCache arrayCache, String variableName, int polarization, Area area, GridPointInfo gridPointInfo) {
        this.area = area;
        this.gridPointInfo = gridPointInfo;
        this.variableName = variableName;
        this.arrayCache = arrayCache;
        this.polarization = polarization;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public byte getValue(int seqnum, byte noDataValue) {
        final int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        return 0;
    }

    @Override
    public short getValue(int seqnum, short noDataValue) {
        final int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        return 0;
    }

    @Override
    public int getValue(int seqnum, int noDataValue) {
        final int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }
        return 0;
    }

    @Override
    public float getValue(int seqnum, float noDataValue) {
        final int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        return getInterpolatedValue(gridPointIndex, noDataValue);
    }

    private float getInterpolatedValue(int gridPointIndex, float noDataValue) {
        try {
            final Array gpArray = arrayCache.get(variableName);
            final Array gpDataVector = extractGridPointVector(gridPointIndex, gpArray);

            final Array flagsArray = arrayCache.get("Flags");
            final Array flagsVector = extractGridPointVector(gridPointIndex, flagsArray);
            for (int i = 0; i < gpDataVector.getSize(); i++) {
                final float value = gpDataVector.getFloat(i);
                if (Math.abs(value - noDataValue) < 1e-8) {
                    break;
                }
                final int flags = flagsVector.getInt(i);
                if (polarization == 4 || polarization == (flags & 3) || (polarization & flags & 2) != 0) {

                }
            }


            return 0;
        } catch (IOException e) {
            return noDataValue;
        } catch (InvalidRangeException e) {
            return noDataValue;
        }
    }

    private Array extractGridPointVector(int gridPointIndex, Array array) throws InvalidRangeException {
        final int[] origin = array.getShape();
        origin[0] = gridPointIndex;
        origin[1] = 0;

        final int[] shape = array.getShape();
        shape[0] = 1;

        return array.section(origin, shape);
    }


}
