package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;

import java.awt.geom.Area;
import java.io.IOException;

public class L1cScienceDataValueProvider implements ValueProvider {

    private final L1cScienceSmosFile smosFile;
    private final int memberIndex;
    private final int polarisation;
    private long snapshotId;

    L1cScienceDataValueProvider(L1cScienceSmosFile smosFile, int memberIndex, int polarization) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
        this.polarisation = polarization;
        this.snapshotId = -1;
    }

    public long getSnapshotId() {
        synchronized (this) {
            return snapshotId;
        }
    }

    public void setSnapshotId(long snapshotId) {
        synchronized (this) {
            this.snapshotId = snapshotId;
        }
    }

    @Override
    public Area getDomain() {
        return smosFile.getEnvelope();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return smosFile.getGridPointIndex(seqnum);
    }

    @Override
    public byte getValue(int gridPointIndex, byte noDataValue) {
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueByte(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getByte(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueShort(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getShort(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueInt(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getInt(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueFloat(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getFloat(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }
}
