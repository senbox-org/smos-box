package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.SmosConstants;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Area;
import java.io.IOException;

public class ScienceFlagsValueProvider extends AbstractValueProvider {

    private final Area area;
    private final GridPointInfo gridPointInfo;
    private final String variableName;
    private final ArrayCache arrayCache;
    private final BandDescriptor descriptor;
    private final double incidenceAngleScalingFactor;

    private long snapshotId;


    ScienceFlagsValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo, double incidenceAngleScalingFactor) {
        this.gridPointInfo = gridPointInfo;
        this.area = area;
        this.variableName = variableName;
        this.arrayCache = arrayCache;
        this.descriptor = descriptor;
        this.incidenceAngleScalingFactor = incidenceAngleScalingFactor;

        snapshotId = -1;
    }

    @Override
    public Area getArea() {
        return area;
    }

    public long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return gridPointInfo.getGridPointIndex(seqnum);
    }

    @Override
    public byte getByte(int gridPointIndex) {
        if (snapshotId < 0) {
            return (byte) getCombinedFlags(gridPointIndex);
        } else {
            return (byte) getSnapshotValue(gridPointIndex);
        }
    }

    @Override
    public short getShort(int gridPointIndex) {
        if (snapshotId < 0) {
            return (short) getCombinedFlags(gridPointIndex);
        } else {
            return (short) getSnapshotValue(gridPointIndex);
        }
    }

    @Override
    public int getInt(int gridPointIndex) {
        if (snapshotId < 0) {
            return getCombinedFlags(gridPointIndex);
        } else {
            return getSnapshotValue(gridPointIndex);
        }
    }

    @Override
    public float getFloat(int gridPointIndex) {
        if (snapshotId < 0) {
            return getCombinedFlags(gridPointIndex);
        } else {
            return getSnapshotValue(gridPointIndex);
        }
    }

    private int getCombinedFlags(int gridPointIndex) {
        try {
            final Array flagDataArray = arrayCache.get(variableName);
            final Array flagsVector = extractGridPointVector(gridPointIndex, flagDataArray);
            final Index flagsVectorIndex = flagsVector.getIndex();

            final Array incidenceAngleArray = arrayCache.get(SmosConstants.INCIDENCE_ANGLE);
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

                    if (incidenceAngle >= SmosConstants.MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= SmosConstants.MAX_BROWSE_INCIDENCE_ANGLE) {
                        combinedFlags |= flags;

                        if (!hasLower) {
                            hasLower = incidenceAngle <= SmosConstants.CENTER_BROWSE_INCIDENCE_ANGLE;
                        }
                        if (!hasUpper) {
                            hasUpper = incidenceAngle > SmosConstants.CENTER_BROWSE_INCIDENCE_ANGLE;
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
        return (int) descriptor.getFillValue();
    }

    private int getSnapshotValue(int gridPointIndex) {
        try {
            final Array flagDataArray = arrayCache.get(variableName);
            final Array flagsVector = extractGridPointVector(gridPointIndex, flagDataArray);
            final Index flagsVectorIndex = flagsVector.getIndex();

            final Array snapshotIdOfPixelArray = arrayCache.get("Snapshot_ID_of_Pixel");
            final Array snapsotIdVector = extractGridPointVector(gridPointIndex, snapshotIdOfPixelArray);
            final Index snapshotIndex = snapsotIdVector.getIndex();

            final int polarization = descriptor.getPolarization();
            for (int i = 0; i < flagsVector.getSize(); i++) {
                snapshotIndex.set(i);

                final long pixelSnapshotId = snapsotIdVector.getLong(snapshotIndex);
                if (pixelSnapshotId == snapshotId) {
                    flagsVectorIndex.set(i);

                    final int flags = flagsVector.getInt(flagsVectorIndex);
                    if (polarization == 4 || // for flags (they do not depend on polarisation)
                            polarization == (flags & 1) || // for x or y polarisation (dual pol)
                            (polarization & flags & 2) != 0) { // for xy polarisation (full pol, real and imaginary)
                        return flags;
                    }
                }
            }
        } catch (IOException | InvalidRangeException e) {
            // @todo 3 tb/tb handle correctly 2015-10-16
        }
        return (int) descriptor.getFillValue();
    }

    // @todo 3 tb/tb duplicated code - move to common location and write test 2015-10-14
    private Array extractGridPointVector(int gridPointIndex, Array array) throws InvalidRangeException {
        final int[] origin = {gridPointIndex, 0};
        final int[] shape = array.getShape();
        shape[0] = 1;

        return array.section(origin, shape);
    }
}
