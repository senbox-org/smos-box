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

import java.util.HashMap;


public class GridPointBtDataset {

    private final HashMap<String, Integer> memberNamesMap;
    private final Class[] columnClasses;
    private final Number[][] data;
    private int flagBandIndex;
    private int incidenceAngleBandIndex;

    public GridPointBtDataset(HashMap<String, Integer> memberNamesMap, Class[] columnClasses, Number[][] data) {
        this.memberNamesMap = memberNamesMap;
        this.columnClasses = columnClasses;
        this.data = data;
        flagBandIndex = -1;
        incidenceAngleBandIndex = -1;
    }

    public int getColumnIndex(String name) {
        final Integer index = memberNamesMap.get(name);
        if (index == null) {
            return -1;
        }
        return index;
    }

    public Number[][] getData() {
        return data;
    }

    public Class[] getColumnClasses() {
        return columnClasses;
    }

    public void setFlagBandIndex(int flagbandIndex) {
        this.flagBandIndex = flagbandIndex;
    }

    public int getFlagBandIndex() {
        return flagBandIndex;
    }

    public void setIncidenceAngleBandIndex(int incidenceAngleBandIndex) {
        this.incidenceAngleBandIndex = incidenceAngleBandIndex;
    }

    public int getIncidenceAngleBandIndex() {
        return incidenceAngleBandIndex;
    }
}
