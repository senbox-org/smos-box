package org.esa.beam.dataio.smos.provider;

import org.esa.beam.dataio.smos.L1cScienceSmosFile;

import java.awt.geom.Area;
import java.io.IOException;

public class FirlefanzProvider extends AbstractValueProvider {

    private final L1cScienceSmosFile smosFile;
    private final int memberIndex;
    private final int polarisation;

    public FirlefanzProvider(L1cScienceSmosFile smosFile, int memberIndex, int polarisation) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
        this.polarisation = polarisation;
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return 0; // not required in this implementation tb 2014-09-22
    }

    @Override
    public byte getByte(int gridPointIndex) throws IOException {
        return smosFile.getBrowseBtDataValueByte(gridPointIndex, memberIndex, polarisation);
    }

    @Override
    public short getShort(int gridPointIndex) throws IOException {
        return smosFile.getBrowseBtDataValueShort(gridPointIndex, memberIndex, polarisation);
    }

    @Override
    public int getInt(int gridPointIndex) throws IOException {
        return smosFile.getBrowseBtDataValueInt(gridPointIndex, memberIndex, polarisation);
    }

    @Override
    public float getFloat(int gridPointIndex) throws IOException {
        return smosFile.getBrowseBtDataValueFloat(gridPointIndex, memberIndex, polarisation);
    }

    @Override
    public Area getArea() {
        return smosFile.getArea();
    }
}
