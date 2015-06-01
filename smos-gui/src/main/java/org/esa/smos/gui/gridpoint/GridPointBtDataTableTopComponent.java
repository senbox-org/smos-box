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

package org.esa.smos.gui.gridpoint;

import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.SmosReader;
import org.esa.smos.gui.TableModelExportRunner;
import org.esa.snap.framework.ui.product.ProductSceneView;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import java.awt.FlowLayout;
import java.io.IOException;

public class GridPointBtDataTableTopComponent extends GridPointBtDataTopComponent {

    public static final String ID = GridPointBtDataTableTopComponent.class.getName();

    private JTable table;
    private JButton columnsButton;
    private JButton exportButton;

    private GridPointBtDataTableModel gridPointBtDataTableModel;

    public GridPointBtDataTableTopComponent() {
        gridPointBtDataTableModel = new GridPointBtDataTableModel();
        table = new JTable(gridPointBtDataTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        boolean enabled = smosView != null;
        SmosReader smosReader = null;
        if (enabled) {
            smosReader = getSelectedSmosReader();
            if (smosReader == null) {
                enabled = false;
            }
        }

        table.setEnabled(enabled);
        columnsButton.setEnabled(enabled);
        exportButton.setEnabled(enabled);
        if (enabled) {
            final String[] names = smosReader.getRawDataTableNames();
            gridPointBtDataTableModel.setColumnNames(names);
            final TableColumnModel columnModel = new DefaultTableColumnModel();
            table.setColumnModel(columnModel);
            table.createDefaultColumnsFromModel();
        }
    }

    @Override
    protected JComponent createGridPointComponent() {
        // @TODO 1 tb/tb find replacement for that code 2015-06-01
//        final TableHeaderPopupMenuInstaller installer = new TableHeaderPopupMenuInstaller(table);
//        installer.addTableHeaderPopupMenuCustomizer(new AutoResizePopupMenuCustomizer());
//        installer.addTableHeaderPopupMenuCustomizer(new TableColumnChooserPopupMenuCustomizer());

        return new JScrollPane(table);
    }

    @Override
    protected JComponent createGridPointComponentOptionsComponent() {
        // @TODO 1 tb/tb find replacement for that code 2015-06-01
//        Action action = TableColumnChooser.getTableColumnChooserButton(table).getAction();
//        action.putValue(Action.NAME, "Columns...");
//        columnsButton = new JButton(action);

        exportButton = new JButton("Export...");
        exportButton.addActionListener(e -> new TableModelExportRunner(getParent(), getShortName(), table.getModel(), table.getColumnModel()).run());

        final JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        optionsPanel.add(columnsButton);
        optionsPanel.add(exportButton);

        return optionsPanel;
    }

    @Override
    protected void updateGridPointBtDataComponent(GridPointBtDataset ds) {
        gridPointBtDataTableModel.setGridPointBtDataset(ds);
    }

    @Override
    protected void updateGridPointBtDataComponent(IOException e) {
        gridPointBtDataTableModel.setGridPointBtDataset(null);
    }

    @Override
    protected void clearGridPointBtDataComponent() {
        gridPointBtDataTableModel.setGridPointBtDataset(null);
    }
}
