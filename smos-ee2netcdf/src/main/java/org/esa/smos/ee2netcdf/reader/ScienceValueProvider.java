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

public class ScienceValueProvider extends AbstractValueProvider {

    private final Area area;
    private final GridPointInfo gridPointInfo;
    private final String variableName;
    private final ArrayCache arrayCache;
    private final BandDescriptor descriptor;
    private final double incidenceAngleScalingFactor;
    private long snapshotId;

    public ScienceValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo, double incidenceAngleScalingFactor) {
        this.area = area;
        this.gridPointInfo = gridPointInfo;
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
        return gridPointInfo.getGridPointIndex(seqnum); // used by faraday providers tb 2015-11-06
    }

    @Override
    public byte getByte(int gridPointIndex) throws IOException {
        if (snapshotId == -1) {
            return (byte) getInterpolatedValue(gridPointIndex);
        } else {
            return (byte) getSnapshotValue(gridPointIndex);
        }
    }

    @Override
    public short getShort(int gridPointIndex) throws IOException {
        if (snapshotId == -1) {
            return (short) getInterpolatedValue(gridPointIndex);
        } else {
            return (short) getSnapshotValue(gridPointIndex);
        }
    }

    @Override
    public int getInt(int gridPointIndex) throws IOException {
        if (snapshotId == -1) {
            return (int) getInterpolatedValue(gridPointIndex);
        } else {
            return (int) getSnapshotValue(gridPointIndex);
        }
    }

    @Override
    public float getFloat(int gridPointIndex) throws IOException {
        if (snapshotId == -1) {
            return getInterpolatedValue(gridPointIndex);
        } else {
            return getSnapshotValue(gridPointIndex);
        }
    }

    private float getInterpolatedValue(int gridPointIndex) {
        final float fillValue = (float) descriptor.getFillValue();

        try {
            final Array flagsArray = arrayCache.get("Flags");
            final Array incidenceAngleArray = arrayCache.get(SmosConstants.INCIDENCE_ANGLE);
            if (flagsArray == null || incidenceAngleArray == null) {
                return fillValue;
            }

            final Array gpArray = arrayCache.get(variableName);
            final Array gpDataVector = extractGridPointVector(gridPointIndex, gpArray);
            final Index dataIndex = gpDataVector.getIndex();

            final Array flagsVector = extractGridPointVector(gridPointIndex, flagsArray);
            final Index flagsIndex = flagsVector.getIndex();

            final Array incidenceAngleVector = extractGridPointVector(gridPointIndex, incidenceAngleArray);
            final Index angleVectorIndex = incidenceAngleVector.getIndex();

            int count = 0;
            double sx = 0;
            double sy = 0;
            double sxx = 0;
            double sxy = 0;

            boolean hasLower = false;
            boolean hasUpper = false;

            final int polarization = descriptor.getPolarization();

            for (int i = 0; i < gpDataVector.getSize(); i++) {
                dataIndex.set(i);
                flagsIndex.set(i);
                angleVectorIndex.set(i);

                final float value = gpDataVector.getFloat(dataIndex);
                if (Math.abs(value - fillValue) < 1e-8) {
                    continue;
                }

                final int flags = flagsVector.getInt(flagsIndex);
                if (polarization == 4 || polarization == (flags & 3) || (polarization & flags & 2) != 0) {
                    final double incidenceAngle = incidenceAngleScalingFactor * incidenceAngleVector.getInt(angleVectorIndex);

                    if (incidenceAngle >= SmosConstants.MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= SmosConstants.MAX_BROWSE_INCIDENCE_ANGLE) {
                        sx += incidenceAngle;
                        sy += value;
                        sxx += incidenceAngle * incidenceAngle;
                        sxy += incidenceAngle * value;
                        count++;

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
                final double a = (count * sxy - sx * sy) / (count * sxx - sx * sx);
                final double b = (sy - a * sx) / count;
                return (float) (a * SmosConstants.CENTER_BROWSE_INCIDENCE_ANGLE + b);
            }
        } catch (IOException e) {
            return fillValue;
        } catch (InvalidRangeException e) {
            return fillValue;
        }
        return fillValue;
    }

    private float getSnapshotValue(int gridPointIndex) {
        final float fillValue = (float) descriptor.getFillValue();
        try {
            final Array snapshotIdOfPixelArray = arrayCache.get("Snapshot_ID_of_Pixel");
            final Array flagsArray = arrayCache.get("Flags");
            if (snapshotIdOfPixelArray == null || flagsArray == null) {
                return fillValue;
            }

            final Array gpArray = arrayCache.get(variableName);
            final Array gpDataVector = extractGridPointVector(gridPointIndex, gpArray);
            final Index dataIndex = gpDataVector.getIndex();


            final Array snapsotIdVector = extractGridPointVector(gridPointIndex, snapshotIdOfPixelArray);
            final Index snapshotIndex = snapsotIdVector.getIndex();


            final Array flagsVector = extractGridPointVector(gridPointIndex, flagsArray);
            final Index flagsIndex = flagsVector.getIndex();

            final int polarization = descriptor.getPolarization();
            for (int i = 0; i < gpDataVector.getSize(); i++) {
                snapshotIndex.set(i);

                final long pixelSnapshotId = snapsotIdVector.getLong(snapshotIndex);
                if (pixelSnapshotId == snapshotId) {
                    flagsIndex.set(i);

                    final int flags = flagsVector.getInt(flagsIndex);
                    if (polarization == 4 || // for flags (they do not depend on polarisation)
                            polarization == (flags & 1) || // for x or y polarisation (dual pol)
                            (polarization & flags & 2) != 0) { // for xy polarisation (full pol, real and imaginary)
                        dataIndex.set(i);
                        return gpDataVector.getFloat(dataIndex);
                    }
                }
            }
        } catch (IOException | InvalidRangeException e) {
            return fillValue;
        }
        return fillValue;
    }

    private Array extractGridPointVector(int gridPointIndex, Array array) throws InvalidRangeException {
        final int[] origin = {gridPointIndex, 0};
        final int[] shape = array.getShape();
        shape[0] = 1;

        return array.section(origin, shape);
    }
}
