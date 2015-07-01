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
import org.esa.snap.framework.ui.UIUtils;
import org.esa.snap.framework.ui.product.ProductSceneView;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.windows.TopComponent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@TopComponent.Description(
        preferredID = "GridPointBtDataTableTopComponent",
        iconBase = "org/esa/smos/icons/SmosGridPoint.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "navigator",
        openAtStartup = false,
        position = 2
)
@ActionID(category = "Window", id = "org.esa.smos.gui.gridpoint.GridPointBtDataTableTopComponent")
@ActionReferences({
        @ActionReference(path = "Menu/View/Tool Windows/SMOS"),
        @ActionReference(path = "Toolbars/SMOS", position = 40)
})
@TopComponent.OpenActionRegistration(
        displayName = GridPointBtDataTableTopComponent.DISPLAY_NAME,
        preferredID = "GridPointBtDataTableTopComponent"
)

public class GridPointBtDataTableTopComponent extends GridPointBtDataTopComponent {

    static final String DISPLAY_NAME = "SMOS L1C Table";

    private JXTable table;
    private JButton columnsButton;
    private JButton exportButton;

    private GridPointBtDataTableModel gridPointBtDataTableModel;

    public GridPointBtDataTableTopComponent() {
        super();

        gridPointBtDataTableModel = new GridPointBtDataTableModel();
        table = new JXTable(gridPointBtDataTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSortable(false);
        table.setEditable(false);

        setDisplayName(DISPLAY_NAME);
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
            final TableColumnModel columnModel = new DefaultTableColumnModelExt();
            table.setColumnModel(columnModel);
            table.createDefaultColumnsFromModel();
        }
    }

    @Override
    protected JComponent createGridPointComponent() {
        return new JScrollPane(table);
    }

    @Override
    protected JComponent createGridPointComponentOptionsComponent() {
        columnsButton = new JButton("Select Columns ...");
        columnsButton.addActionListener(new SelectColumnActionListener());

        exportButton = new JButton("Export...");
        exportButton.addActionListener(e -> new TableModelExportRunner(getParent(), getShortName(), table.getModel(), (TableColumnModelExt) table.getColumnModel()).run());

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

    private void updateVisibility(HashMap<String, Boolean> columnVisibilityMap) {
        final Set<Map.Entry<String, Boolean>> entries = columnVisibilityMap.entrySet();
        for (Map.Entry<String, Boolean> entry : entries) {
            final TableColumnExt columnExt = table.getColumnExt(entry.getKey());
            columnExt.setVisible(entry.getValue());
        }
        table.updateUI();
    }

    private class SelectColumnActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFrame rootJFrame = UIUtils.getRootJFrame(GridPointBtDataTableTopComponent.this);
            final String[] columnNames = gridPointBtDataTableModel.getColumnNames();
            final ColumnProperty[] columnProperties = new ColumnProperty[columnNames.length];
            int index = 0;
            for (String columnName : columnNames) {
                final TableColumnExt columnExt = table.getColumnExt(columnName);
                if (columnExt == null) {
                    columnProperties[index] = new ColumnProperty(columnName, false);
                } else {
                    columnProperties[index] = new ColumnProperty(columnName, columnExt.isVisible());
                }
                ++index;
            }
            final GridPointTableSelectionDialog dialog = GridPointTableSelectionDialog.create(rootJFrame, columnProperties);

            dialog.setVisible(true);

            if (dialog.isCanceled()) {
                return;
            }

            updateVisibility(dialog.getSelection());
        }
    }
}
