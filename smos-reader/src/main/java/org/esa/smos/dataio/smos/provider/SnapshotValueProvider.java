package org.esa.smos.dataio.smos.provider;


import com.bc.ceres.binio.CompoundData;
import org.esa.smos.dataio.smos.L1cScienceSmosFile;

import java.awt.geom.Area;
import java.io.IOException;
import java.text.MessageFormat;

public class SnapshotValueProvider extends AbstractValueProvider {

    private final L1cScienceSmosFile smosFile;
    private final int memberIndex;
    private final int polarisation;
    private final long snapshotId;

    public SnapshotValueProvider(L1cScienceSmosFile smosFile, int memberIndex, int polarisation, long snapshotId) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
        this.polarisation = polarisation;
        this.snapshotId = snapshotId;
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return 0; // not required in this implementation tb 2014-09-22
    }

    @Override
    public byte getByte(int gridPointIndex) throws IOException {
        final CompoundData data = getCompoundData(gridPointIndex);
        return data.getByte(memberIndex);
    }

    @Override
    public short getShort(int gridPointIndex) throws IOException {
        final CompoundData data = getCompoundData(gridPointIndex);
        return data.getShort(memberIndex);
    }

    @Override
    public int getInt(int gridPointIndex) throws IOException {
        final CompoundData data = getCompoundData(gridPointIndex);
        return data.getInt(memberIndex);
    }

    @Override
    public float getFloat(int gridPointIndex) throws IOException {
        final CompoundData data = getCompoundData(gridPointIndex);
        return data.getFloat(memberIndex);
    }

    @Override
    public Area getArea() {
        return smosFile.getSnapshotInfo().getArea(snapshotId);
    }

    private CompoundData getCompoundData(int gridPointIndex) throws IOException {
        final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
        if (data == null) {
            throw new IOException(MessageFormat.format("No data found for grid point ''{0}''.", gridPointIndex));
        }
        return data;
    }
}
