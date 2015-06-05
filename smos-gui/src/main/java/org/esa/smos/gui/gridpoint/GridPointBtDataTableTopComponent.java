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
import org.esa.snap.rcp.SnapApp;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;

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
        @ActionReference(path = "Menu/Window/Tool Windows/SMOS")
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

    private class SelectColumnActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFrame rootJFrame = UIUtils.getRootJFrame(GridPointBtDataTableTopComponent.this);
            final JDialog jDialog = GridPointTableSelectionDialog.create(rootJFrame);

            jDialog.setVisible(true);
        }
    }
}
