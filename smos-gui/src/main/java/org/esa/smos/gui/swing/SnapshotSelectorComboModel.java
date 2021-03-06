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
package org.esa.smos.gui.swing;

import org.esa.smos.dataio.smos.SnapshotInfo;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataListener;
import java.util.HashMap;
import java.util.Map;

public class SnapshotSelectorComboModel implements ComboBoxModel {
    private final ComboBoxModel<String> comboBoxModel;
    private final Map<Object, SnapshotSelectorModel> map;

    public SnapshotSelectorComboModel(SnapshotInfo snapshotInfo) {
        map = new HashMap<>();

        map.put("Any", new SnapshotSelectorModel(snapshotInfo.getSnapshotIds()));
        map.put("X", new SnapshotSelectorModel(snapshotInfo.getSnapshotIdsX()));
        map.put("Y", new SnapshotSelectorModel(snapshotInfo.getSnapshotIdsY()));

        if (snapshotInfo.getSnapshotIdsXY().size() != 0) {
            map.put("XY", new SnapshotSelectorModel(snapshotInfo.getSnapshotIdsXY()));
            comboBoxModel = new DefaultComboBoxModel<>(new String[]{"Any", "X", "Y", "XY"});
        } else {
            comboBoxModel = new DefaultComboBoxModel<>(new String[]{"Any", "X", "Y"});
        }

    }

    SnapshotSelectorModel getSelectedModel() {
        return getModel(comboBoxModel.getSelectedItem());
    }

    SnapshotSelectorModel getModel(Object item) {
        return map.get(item);
    }

    @Override
    public void setSelectedItem(Object newItem) {
        final Object oldItem = getSelectedItem();
        final SnapshotSelectorModel oldModel = map.get(oldItem);
        final SnapshotSelectorModel newModel = map.get(newItem);

        try {
            newModel.setSnapshotId(oldModel.getSnapshotId());
        } catch (IllegalArgumentException e) {
            // the value of the old model is not valid for the new model, so
            // the new model keeps its old value
        }

        comboBoxModel.setSelectedItem(newItem);
    }

    @Override
    public Object getSelectedItem() {
        return comboBoxModel.getSelectedItem();
    }

    @Override
    public int getSize() {
        return comboBoxModel.getSize();
    }

    @Override
    public Object getElementAt(int index) {
        return comboBoxModel.getElementAt(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        comboBoxModel.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        comboBoxModel.removeListDataListener(l);
    }
}
