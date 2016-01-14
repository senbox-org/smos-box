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

package org.esa.smos.gui;

import org.esa.smos.dataio.smos.SmosReader;
import org.esa.smos.gui.snapshot.SnapshotSelectionService;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.rcp.actions.help.HelpAction;
import org.esa.snap.ui.product.ProductSceneView;
import org.esa.snap.ui.tool.ToolButtonFactory;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Image;

public abstract class SmosTopComponent extends TopComponent {

    private static final String HELP_ID = "smosIntroduction";

    private JPanel panel;
    private JLabel defaultComponent;
    private JComponent clientComponent;
    private SmosTopComponent.SVSL svsl;

    public SmosTopComponent() {
        final JComponent component = initUi();
        setLayout(new BorderLayout());
        add(component, BorderLayout.CENTER);
    }

    protected final SceneViewSelectionService getSmosViewSelectionService() {
        return SmosBox.getInstance().getSmosViewSelectionService();
    }

    protected final SnapshotSelectionService getSnapshotSelectionService() {
        return SmosBox.getInstance().getSnapshotSelectionService();
    }

    protected final ProductSceneView getSelectedSmosView() {
        return getSmosViewSelectionService().getSelectedSceneView();
    }

    protected final Product getSelectedSmosProduct() {
        return getSmosViewSelectionService().getSelectedSmosProduct();
    }

    protected final SmosReader getSelectedSmosReader() {
        return getSmosViewSelectionService().getSelectedSmosReader();
    }

    protected final long getSelectedSnapshotId(RasterDataNode raster) {
        return getSnapshotSelectionService().getSelectedSnapshotId(raster);
    }

    protected final long getSelectedSnapshotId(ProductSceneView view) {
        final RasterDataNode raster;
        if (view != null) {
            raster = view.getRaster();
        } else {
            raster = null;
        }
        return getSnapshotSelectionService().getSelectedSnapshotId(raster);
    }

    protected final void setSelectedSnapshotId(RasterDataNode raster, long id) {
        getSnapshotSelectionService().setSelectedSnapshotId(raster, id);
    }

    protected JComponent initUi() {
        panel = new JPanel(new BorderLayout());

        final Image image = ImageUtilities.loadImage("org/esa/smos/icons/SmosIcon.png", false);
        if (image != null) {
            defaultComponent = new JLabel(new ImageIcon(image));
        } else {
            defaultComponent = new JLabel();
        }
        defaultComponent.setIconTextGap(10);
        defaultComponent.setText("No SMOS image selected.");

        panel.add(defaultComponent, BorderLayout.CENTER);

        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }

    @Override
    public void componentOpened() {
        svsl = new SVSL();
        getSmosViewSelectionService().addSceneViewSelectionListener(svsl);
        realizeSmosView(getSelectedSmosView());
    }

    @Override
    public void componentClosed() {
        getSmosViewSelectionService().removeSceneViewSelectionListener(svsl);
        realizeSmosView(null);
    }

    @Override
    public void componentShowing() {
        realizeSmosView(getSelectedSmosView());
    }

    @Override
    public void componentHidden() {
        realizeSmosView(null);
    }

    protected void realizeSmosView(ProductSceneView view) {
        if (view != null) {
            if (clientComponent == null) {
                clientComponent = createClientComponent();
            }
            setToolViewComponent(clientComponent);
            updateClientComponent(view);
        } else {
            setToolViewComponent(defaultComponent);
        }
    }

    protected final JComponent getClientComponent() {
        return clientComponent != null ? clientComponent : defaultComponent;
    }

    protected JComponent getDefaultComponent() {
        return defaultComponent;
    }

    protected abstract JComponent createClientComponent();

    protected abstract void updateClientComponent(ProductSceneView smosView);

    protected final void setToolViewComponent(JComponent comp) {
        panel.removeAll();
        panel.add(comp, BorderLayout.CENTER);
        panel.invalidate();
        panel.validate();
        panel.updateUI();
    }

    private class SVSL implements SceneViewSelectionService.SelectionListener {

        @Override
        public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
            realizeSmosView(newView);
        }
    }

    protected AbstractButton createHelpButton() {
        final AbstractButton helpButton = ToolButtonFactory.createButton(new HelpAction(this), false);
        helpButton.setName("helpButton");

        return helpButton;
    }
}
