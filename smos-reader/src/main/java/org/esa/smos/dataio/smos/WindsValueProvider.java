package org.esa.smos.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;

import java.awt.geom.Area;
import java.io.IOException;

public class WindsValueProvider extends AbstractValueProvider {

    private final AuxWindsFile auxWindsFile;
    private final int memberIndex;
    private final boolean mapPoint;

    public WindsValueProvider(AuxWindsFile auxWindsFile, int memberIndex, boolean mapPoint) {
        this.auxWindsFile = auxWindsFile;
        this.memberIndex = memberIndex;
        this.mapPoint = mapPoint;
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return auxWindsFile.getGridPointIndex(seqnum);
    }

    @Override
    public byte getByte(int gridPointIndex) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public short getShort(int gridPointIndex) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getInt(int gridPointIndex) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public float getFloat(int gridPointIndex) throws IOException {
        CompoundData compoundData = auxWindsFile.getCompound(gridPointIndex, mapPoint);
        return compoundData.getFloat(memberIndex);
    }

    @Override
    public Area getArea() {
        return auxWindsFile.getArea();
    }
}
