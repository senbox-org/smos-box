/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.smos.dataio.smos;

import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import org.esa.smos.dataio.smos.provider.BTValueProvider;
import org.esa.smos.dataio.smos.provider.SnapshotValueProvider;

import java.awt.geom.Area;
import java.io.IOException;

public class L1cScienceValueProvider extends AbstractValueProvider {

    private final L1cScienceSmosFile smosFile;
    private final int memberIndex;
    private final int polarisation;
    private volatile long snapshotId;

    private AbstractValueProvider valueProviderImpl;

    L1cScienceValueProvider(L1cScienceSmosFile smosFile, int memberIndex, int polarization) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
        this.polarisation = polarization;
        this.snapshotId = -1;
        valueProviderImpl = new BTValueProvider(smosFile, memberIndex, polarisation);
    }

    public final long getSnapshotId() {
        return snapshotId;
    }

    public final void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
        if (snapshotId < 0) {
            valueProviderImpl = new BTValueProvider(smosFile, memberIndex, polarisation);
        } else {
            valueProviderImpl = new SnapshotValueProvider(smosFile, memberIndex, polarisation, snapshotId);
        }
    }

    @Override
    public Area getArea() {
        return valueProviderImpl.getArea();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return smosFile.getGridPointIndex(seqnum);
    }

    @Override
    public byte getByte(int gridPointIndex) throws IOException {
        return valueProviderImpl.getByte(gridPointIndex);
    }

    @Override
    public short getShort(int gridPointIndex) throws IOException {
        return valueProviderImpl.getShort(gridPointIndex);
    }

    @Override
    public int getInt(int gridPointIndex) throws IOException {
        return valueProviderImpl.getInt(gridPointIndex);
    }

    @Override
    public float getFloat(int gridPointIndex) throws IOException {
        return valueProviderImpl.getFloat(gridPointIndex);
    }
}
