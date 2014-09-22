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

package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;

import java.awt.geom.Area;
import java.io.IOException;
import java.text.MessageFormat;

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
    protected byte getByte(int gridPointIndex) throws IOException {
        return valueProviderImpl.getByte(gridPointIndex);
    }

    @Override
    protected short getShort(int gridPointIndex) throws IOException {
        return valueProviderImpl.getShort(gridPointIndex);
    }

    @Override
    protected int getInt(int gridPointIndex) throws IOException {
        return valueProviderImpl.getInt(gridPointIndex);
    }

    @Override
    protected float getFloat(int gridPointIndex) throws IOException {
        return valueProviderImpl.getFloat(gridPointIndex);
    }

    class BTValueProvider extends AbstractValueProvider {

        private final L1cScienceSmosFile smosFile;
        private final int memberIndex;
        private final int polarisation;

        BTValueProvider(L1cScienceSmosFile smosFile, int memberIndex, int polarisation) {
            this.smosFile = smosFile;
            this.memberIndex = memberIndex;
            this.polarisation = polarisation;
        }

        @Override
        protected int getGridPointIndex(int seqnum) {
            return 0; // not required in this implementation tb 2014-09-22
        }

        @Override
        protected byte getByte(int gridPointIndex) throws IOException {
            return smosFile.getBrowseBtDataValueByte(gridPointIndex, memberIndex, polarisation);
        }

        @Override
        protected short getShort(int gridPointIndex) throws IOException {
            return smosFile.getBrowseBtDataValueShort(gridPointIndex, memberIndex, polarisation);
        }

        @Override
        protected int getInt(int gridPointIndex) throws IOException {
            return smosFile.getBrowseBtDataValueInt(gridPointIndex, memberIndex, polarisation);
        }

        @Override
        protected float getFloat(int gridPointIndex) throws IOException {
            return smosFile.getBrowseBtDataValueFloat(gridPointIndex, memberIndex, polarisation);
        }

        @Override
        public Area getArea() {
            return smosFile.getArea();
        }
    }

    class SnapshotValueProvider extends AbstractValueProvider {

        private final L1cScienceSmosFile smosFile;
        private final int memberIndex;
        private final int polarisation;
        private final long snapshotId;

        SnapshotValueProvider(L1cScienceSmosFile smosFile, int memberIndex, int polarisation, long snapshotId) {
            this.smosFile = smosFile;
            this.memberIndex = memberIndex;
            this.polarisation = polarisation;
            this.snapshotId = snapshotId;
        }

        @Override
        protected int getGridPointIndex(int seqnum) {
            return 0; // not required in this implementation tb 2014-09-22
        }

        @Override
        protected byte getByte(int gridPointIndex) throws IOException {
            final CompoundData data = getCompoundData(gridPointIndex);
            return data.getByte(memberIndex);
        }

        @Override
        protected short getShort(int gridPointIndex) throws IOException {
            final CompoundData data = getCompoundData(gridPointIndex);
            return data.getShort(memberIndex);
        }

        @Override
        protected int getInt(int gridPointIndex) throws IOException {
            final CompoundData data = getCompoundData(gridPointIndex);
            return data.getInt(memberIndex);
        }

        @Override
        protected float getFloat(int gridPointIndex) throws IOException {
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
}
