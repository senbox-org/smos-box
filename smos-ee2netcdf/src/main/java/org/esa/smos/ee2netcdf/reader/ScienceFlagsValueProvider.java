package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Area;
import java.io.IOException;

class ScienceFlagsValueProvider implements ValueProvider {

    // @todo 3 tb/(tb duplicated - move to common location 2015-10-14
    private static final double MIN_BROWSE_INCIDENCE_ANGLE = 37.5;
    private static final double MAX_BROWSE_INCIDENCE_ANGLE = 52.5;
    private static final double CENTER_BROWSE_INCIDENCE_ANGLE = 42.5;

    private final Area area;
    private final GridPointInfo gridPointInfo;
    private final String variableName;
    private final ArrayCache arrayCache;
    private final BandDescriptor descriptor;
    private final double incidenceAngleScalingFactor;


    public ScienceFlagsValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo, double incidenceAngleScalingFactor) {
        this.gridPointInfo = gridPointInfo;
        this.area = area;
        this.variableName = variableName;
        this.arrayCache = arrayCache;
        this.descriptor = descriptor;
        this.incidenceAngleScalingFactor = incidenceAngleScalingFactor;
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

        return (byte) getCombinedFlags(gridPointIndex, noDataValue);
    }

    @Override
    public short getValue(int seqnum, short noDataValue) {
        final int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        return (short) getCombinedFlags(gridPointIndex, noDataValue);
    }

    @Override
    public int getValue(int seqnum, int noDataValue) {
        final int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        return getCombinedFlags(gridPointIndex, noDataValue);
    }

    @Override
    public float getValue(int seqnum, float noDataValue) {
        final int gridPointIndex = gridPointInfo.getGridPointIndex(seqnum);
        if (gridPointIndex < 0) {
            return noDataValue;
        }

        return getCombinedFlags(gridPointIndex, (int) noDataValue);
    }

    private int getCombinedFlags(int gridPointIndex, int noDataValue) {
        try {
            final Array flagDataArray = arrayCache.get(variableName);
            final Array flagsVector = extractGridPointVector(gridPointIndex, flagDataArray);
            final Index flagsVectorIndex = flagsVector.getIndex();

            final Array incidenceAngleArray = arrayCache.get("Incidence_Angle");
            final Array incidenceAngleVector = extractGridPointVector(gridPointIndex, incidenceAngleArray);
            final Index angleVectorIndex = incidenceAngleVector.getIndex();

            int combinedFlags = 0;

            boolean hasLower = false;
            boolean hasUpper = false;

            final int polarization = descriptor.getPolarization();
            for (int i = 0; i < flagsVector.getSize(); ++i) {
                flagsVectorIndex.set(i);
                angleVectorIndex.set(i);
                final int flags = flagsVector.getInt(flagsVectorIndex);

                if (polarization == 4 || polarization == (flags & 3) || (polarization & flags & 2) != 0) {
                    final double incidenceAngle = incidenceAngleScalingFactor * incidenceAngleVector.getInt(angleVectorIndex);

                    if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                        combinedFlags |= flags;

                        if (!hasLower) {
                            hasLower = incidenceAngle <= CENTER_BROWSE_INCIDENCE_ANGLE;
                        }
                        if (!hasUpper) {
                            hasUpper = incidenceAngle > CENTER_BROWSE_INCIDENCE_ANGLE;
                        }
                    }
                }
            }
            if (hasLower && hasUpper) {
                return combinedFlags;
            }
        } catch (InvalidRangeException | IOException e) {
            // @todo 3 tb/tb handle correctly 2015-10-14
        }
        return noDataValue;
    }


    // @todo 3 tb/tb duplicated code - move to common location and write test 2015-10-14
    private Array extractGridPointVector(int gridPointIndex, Array array) throws InvalidRangeException {
        final int[] origin = {gridPointIndex, 0};
        final int[] shape = array.getShape();
        shape[0] = 1;

        return array.section(origin, shape);
    }
}
