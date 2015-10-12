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
import org.esa.smos.gui.SceneViewSelectionService;
import org.esa.smos.gui.SmosBox;
import org.esa.smos.gui.SmosTopComponent;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Placemark;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.datamodel.ProductNodeEvent;
import org.esa.snap.core.datamodel.ProductNodeListener;
import org.esa.snap.ui.product.ProductSceneView;
import org.openide.util.HelpCtx;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public abstract class GridPointBtDataTopComponent extends SmosTopComponent {

    private SmosBox smosBox;

    private JLabel infoLabel;
    private JCheckBox snapToSelectedPinCheckBox;
    private GPSL gpsl;

    @Override
    protected JComponent createClientComponent() {
        infoLabel = new JLabel();
        snapToSelectedPinCheckBox = new JCheckBox("Snap to selected pin");
        snapToSelectedPinCheckBox.addItemListener(new IL());

        final JPanel optionsPanel = new JPanel(new BorderLayout(6, 0));
        optionsPanel.add(snapToSelectedPinCheckBox, BorderLayout.WEST);
        optionsPanel.add(createGridPointComponentOptionsComponent(), BorderLayout.CENTER);

        final JPanel mainPanel = new JPanel(new BorderLayout(2, 2));
        mainPanel.add(infoLabel, BorderLayout.CENTER);
        mainPanel.add(createGridPointComponent(), BorderLayout.CENTER);
        mainPanel.add(optionsPanel, BorderLayout.SOUTH);

        final AbstractButton helpButton = createHelpButton();
        optionsPanel.add(helpButton, BorderLayout.EAST);

        return mainPanel;
    }

    protected JComponent createGridPointComponentOptionsComponent() {
        return new JPanel();
    }

    boolean isSnappedToPin() {
        return snapToSelectedPinCheckBox.isSelected();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        gpsl = new GPSL();

        smosBox = SmosBox.getInstance();
        final GridPointSelectionService gpSelectionService = smosBox.getGridPointSelectionService();

        gpSelectionService.addGridPointSelectionListener(gpsl);
        updateGridPointBtDataComponent(gpSelectionService.getSelectedGridPointId());
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        smosBox.getGridPointSelectionService().removeGridPointSelectionListener(gpsl);
        updateGridPointBtDataComponent(-1);
    }

    final void updateGridPointBtDataComponent() {
        int id = -1;
        if (!isSnappedToPin()) {
            id = smosBox.getGridPointSelectionService().getSelectedGridPointId();
        } else {
            final ProductSceneView view = getSelectedSmosView();
            if (view != null) {
                final Placemark selectedPin = view.getSelectedPin();
                if (selectedPin != null) {
                    final PixelPos pixelPos = selectedPin.getPixelPos();
                    final int x = (int) Math.floor(pixelPos.getX());
                    final int y = (int) Math.floor(pixelPos.getY());
                    id = smosBox.getSmosViewSelectionService().getGridPointId(x, y);
                }
            }
        }
        updateGridPointBtDataComponent(id);
    }

    private void updateGridPointBtDataComponent(int selectedGridPointId) {
        if (selectedGridPointId == -1) {
            setInfoText("No data");
            clearGridPointBtDataComponent();
            return;
        }

        final SmosReader smosReader = getSelectedSmosReader();
        if (!smosReader.canSupplyGridPointBtData()) {
            setInfoText("No data");
            clearGridPointBtDataComponent();
            return;
        }

        final int gridPointIndex = smosReader.getGridPointIndex(selectedGridPointId);
        if (gridPointIndex >= 0) {
            setInfoText("" +
                    "<html>" +
                    "SEQNUM=<b>" + selectedGridPointId + "</b>, " +
                    "INDEX=<b>" + gridPointIndex + "</b>" +
                    "</html>");

            new SwingWorker<GridPointBtDataset, Void>() {
                @Override
                protected GridPointBtDataset doInBackground() throws ExecutionException {
                    try {

                        return smosReader.getBtData(gridPointIndex);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new ExecutionException(e);
                    }
                }

                @Override
                protected void done() {
                    try {
                        updateGridPointBtDataComponent(get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        updateGridPointBtDataComponent(new IOException(e));
                    }
                }

            }.execute();
        } else {
            setInfoText("No data");
            clearGridPointBtDataComponent();
        }
    }

    protected void setInfoText(String text) {
        if (infoLabel != null) {
            infoLabel.setText(text);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        // @todo 1 tb/tb refer to window specific help context
        return super.getHelpCtx();
    }

    protected abstract JComponent createGridPointComponent();

    protected abstract void updateGridPointBtDataComponent(GridPointBtDataset ds);

    protected abstract void updateGridPointBtDataComponent(IOException e);

    protected abstract void clearGridPointBtDataComponent();

    private class GPSL implements GridPointSelectionService.SelectionListener {

        @Override
        public void handleGridPointSelectionChanged(int oldId, int newId) {
            if (!isSnappedToPin()) {
                updateGridPointBtDataComponent(newId);
            }
        }
    }

    private class IL implements ItemListener {

        private final PCL pcl;
        private final PNL pnl;
        private final VSL vsl;

        private IL() {
            pcl = new PCL();
            pnl = new PNL();
            vsl = new VSL();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateGridPointBtDataComponent();
                smosBox.getSmosViewSelectionService().addSceneViewSelectionListener(vsl);
                getSelectedSmosView().addPropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                getSelectedSmosProduct().addProductNodeListener(pnl);
            } else {
                getSelectedSmosProduct().removeProductNodeListener(pnl);
                getSelectedSmosView().removePropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                smosBox.getSmosViewSelectionService().removeSceneViewSelectionListener(vsl);
            }
        }

        private class PCL implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateGridPointBtDataComponent();
            }
        }

        private class PNL implements ProductNodeListener {

            @Override
            public void nodeChanged(ProductNodeEvent event) {
                updatePin(event);
            }

            @Override
            public void nodeDataChanged(ProductNodeEvent event) {
                updatePin(event);
            }

            @Override
            public void nodeAdded(ProductNodeEvent event) {
                updatePin(event);
            }

            @Override
            public void nodeRemoved(ProductNodeEvent event) {
                updatePin(event);
            }

            private void updatePin(ProductNodeEvent event) {
                final ProductNode sourceNode = event.getSourceNode();
                if (sourceNode instanceof Placemark) {
                    updateGridPointBtDataComponent();
                }
            }
        }

        private class VSL implements SceneViewSelectionService.SelectionListener {

            @Override
            public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
                if (oldView != null) {
                    oldView.removePropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                }
                if (newView != null) {
                    newView.addPropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                }
                updateGridPointBtDataComponent();
            }
        }
    }
}
