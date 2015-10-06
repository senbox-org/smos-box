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

package org.esa.smos.gui.snapshot;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.CollectionLayer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glayer.swing.LayerCanvas;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.ceres.glevel.support.FileMultiLevelSource;
import com.bc.ceres.grender.Rendering;
import com.bc.ceres.grender.Viewport;
import org.esa.smos.dataio.smos.CellValueProvider;
import org.esa.smos.dataio.smos.L1cScienceValueProvider;
import org.esa.smos.dataio.smos.SmosConstants;
import org.esa.smos.dataio.smos.SmosMultiLevelSource;
import org.esa.smos.dataio.smos.SmosReader;
import org.esa.smos.dataio.smos.SnapshotInfo;
import org.esa.smos.dataio.smos.bufr.LightBufrMultiLevelSource;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.smos.gui.ProgressBarProgressMonitor;
import org.esa.smos.gui.SmosBox;
import org.esa.smos.gui.SmosTopComponent;
import org.esa.smos.gui.TableModelExportRunner;
import org.esa.smos.gui.swing.SnapshotSelectorCombo;
import org.esa.smos.gui.swing.SnapshotSelectorComboModel;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.RasterDataNode;
import org.esa.snap.framework.datamodel.VirtualBand;
import org.esa.snap.glevel.TiledFileMultiLevelSource;
import org.esa.snap.netbeans.docwin.WindowUtilities;
import org.esa.snap.rcp.windows.ProductSceneViewTopComponent;
import org.esa.snap.ui.product.ProductSceneView;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.jfree.layout.CenterLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.windows.TopComponent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@TopComponent.Description(
        preferredID = "SnapshotInfoTopComponent",
        iconBase = "org/esa/smos/icons/SmosSnapshot.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "navigator", openAtStartup = false, position = 4)
@ActionID(category = "Window", id = "org.esa.smos.gui.snapshot.SnapshotInfoTopComponent")
@ActionReferences({
        @ActionReference(path = "Menu/View/Tool Windows/SMOS", position = 10),
        @ActionReference(path = "Toolbars/SMOS", position = 10)
})
@TopComponent.OpenActionRegistration(
        displayName = SnapshotInfoTopComponent.DISPLAY_NAME,
        preferredID = "SnapshotInfoTopComponent"
)

public class SnapshotInfoTopComponent extends SmosTopComponent {

    static final String DISPLAY_NAME = "SMOS L1C Snapshot";

    private static final SnapshotTableModel NULL_MODEL = new SnapshotTableModel(new Object[0][0]);

    private SnapshotSelectorCombo snapshotSelectorCombo;
    private JXTable snapshotTable;
    private ButtonModel locateSnapshotButtonModel;
    private ButtonModel browseButtonModel;
    private ButtonModel snapshotButtonModel;
    private ButtonModel followModeButtonModel;
    private ButtonModel synchronizeButtonModel;

    @SuppressWarnings({"FieldCanBeLocal"})
    private SnapshotIdListener snapshotIdListener;

    public SnapshotInfoTopComponent() {
        super();
        setDisplayName(DISPLAY_NAME);
    }

    @Override
    protected JComponent createClientComponent() {
        snapshotIdListener = new SnapshotIdListener();

        JPanel mainPanel = new JPanel(new BorderLayout(4, 4));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        snapshotSelectorCombo = new SnapshotSelectorCombo();
        snapshotSelectorCombo.addSliderChangeListener(snapshotIdListener);
        snapshotSelectorCombo.addComboBoxActionListener(e -> updateTable(snapshotSelectorCombo.getSnapshotId()));

        JComponent comboComponent = SnapshotSelectorCombo.createComponent(snapshotSelectorCombo, false);
        mainPanel.add(comboComponent, BorderLayout.NORTH);

        final JPanel snapshotTablePanel = createSnapshotTablePanel();
        mainPanel.add(snapshotTablePanel, BorderLayout.CENTER);

        final JPanel viewSettingsPanel = createViewSettingsPanel();

        final JPanel southPanel = new JPanel(new BorderLayout(6, 0));
        southPanel.add(viewSettingsPanel, BorderLayout.WEST);
        final AbstractButton helpButton = createHelpButton();
        southPanel.add(helpButton, BorderLayout.EAST);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        final SmosReader l1CScienceSmosReader = SmosBox.getL1CScienceSmosReader(smosView);
        if (l1CScienceSmosReader != null) {
            if (!l1CScienceSmosReader.hasSnapshotInfo()) {
                startPolModeInitWaiting(smosView, l1CScienceSmosReader);
                return;
            }
            final long snapshotId = getSelectedSnapshotId(smosView.getRaster());
            updateUI(smosView, snapshotId, true);
        } else {
            super.realizeSmosView(null);
        }
    }

    private void updateTable(long snapshotId) {
        final SmosReader smosReader = getSelectedSmosReader();
        if (smosReader != null && smosReader.canSupplySnapshotData()) {
            final SnapshotInfo snapshotInfo = smosReader.getSnapshotInfo();
            final int snapshotIndex = snapshotInfo.getSnapshotIndex(snapshotId);
            if (snapshotIndex != -1) {
                final SwingWorker<TableModel, Object> worker = new SwingWorker<TableModel, Object>() {
                    @Override
                    protected TableModel doInBackground() throws Exception {
                        final Object[][] snapshotData = smosReader.getSnapshotData(snapshotIndex);
                        return new SnapshotTableModel(snapshotData);
                    }

                    @Override
                    protected void done() {
                        try {
                            final TableModel tableModel = get();
                            snapshotTable.setModel(tableModel);
                        } catch (InterruptedException | ExecutionException e) {
                            snapshotTable.setModel(NULL_MODEL);
                        }
                    }
                };
                worker.execute();
                return;
            }
        }
        snapshotTable.setModel(NULL_MODEL);
    }

    // @todo 1 tb/tb refactor, this is ugly code 2014-10-31
    private void updateSnapshotImage(RasterDataNode raster, long snapshotId) {
        final MultiLevelImage sourceImage = raster.getSourceImage();
        if (sourceImage instanceof DefaultMultiLevelImage) {
            final DefaultMultiLevelImage multiLevelImage = (DefaultMultiLevelImage) sourceImage;
            final MultiLevelSource source = multiLevelImage.getSource();
            if (source instanceof SmosMultiLevelSource) {
                final SmosMultiLevelSource smosMultiLevelSource = (SmosMultiLevelSource) source;
                final ValueProvider valueProvider = smosMultiLevelSource.getValueProvider();
                if (valueProvider instanceof L1cScienceValueProvider) {
                    final L1cScienceValueProvider btDataValueProvider = (L1cScienceValueProvider) valueProvider;
                    if (btDataValueProvider.getSnapshotId() != snapshotId) {
                        btDataValueProvider.setSnapshotId(snapshotId);
                        resetRasterImages(raster);
                    }
                }
            } else if (source instanceof LightBufrMultiLevelSource) {
                final LightBufrMultiLevelSource lightBufrMultiLevelSource = (LightBufrMultiLevelSource) source;
                final CellValueProvider valueProvider = lightBufrMultiLevelSource.getValueProvider();
                if (valueProvider.getSnapshotId() != snapshotId) {
                    valueProvider.setSnapshotId((int) snapshotId);
                    resetRasterImages(raster);
                }
            }
        }
    }

    private void locateSnapshotId(final ProductSceneView smosView, final long id) {
        final SmosReader smosReader = SmosBox.getL1CScienceSmosReader(smosView);
        if (smosReader != null) {
            final SnapshotInfo snapshotInfo = smosReader.getSnapshotInfo();
            final Rectangle2D snapshotRegion = snapshotInfo.getArea(id).getBounds2D();
            if (snapshotRegion != null) {
                final Viewport vp = smosView.getLayerCanvas().getViewport();
                final AffineTransform m2v = vp.getModelToViewTransform();
                final Point2D.Double center = new Point2D.Double(snapshotRegion.getCenterX(), snapshotRegion.getCenterY());
                m2v.transform(center, center);
                final Rectangle viewBounds = vp.getViewBounds();
                final double vx = viewBounds.getCenterX();
                final double vy = viewBounds.getCenterY();
                vp.moveViewDelta(vx - center.getX(), vy - center.getY());
            } else {
                final String message = MessageFormat.format("No snapshot found with ID = {0}", id);
                JOptionPane.showMessageDialog(getParent(), message);
            }
        }
    }

    // package access for testing only tb 2014-04-10
    static boolean isXPolarized(Band band) {
        return band.getName().endsWith("_X");
    }

    // package access for testing only tb 2014-04-10
    static boolean isYPolarized(Band band) {
        return band.getName().endsWith("_Y");
    }

    // package access for testing only tb 2014-04-10
    static boolean isXYPolarized(Band band) {
        return band.getName().contains("_XY");
    }

    private class SnapshotIdListener implements ChangeListener {

        private SnapshotRegionOverlay overlay = new SnapshotRegionOverlay();
        private boolean overlayAdded;

        @Override
        public void stateChanged(ChangeEvent e) {
            final long snapshotId = snapshotSelectorCombo.getSnapshotId();
            updateTable(snapshotId);
            if (snapshotSelectorCombo.isAdjusting()) {
                if (snapshotButtonModel.isSelected()) {
                    overlay.setId(snapshotId);
                    if (!overlayAdded) {
                        overlayAdded = true;
                        getSelectedSmosView().getLayerCanvas().addOverlay(overlay);
                    } else {
                        getSelectedSmosView().getLayerCanvas().repaint();
                    }
                }
                return;
            }
            getSelectedSmosView().getLayerCanvas().removeOverlay(overlay);
            overlayAdded = false;
            if (synchronizeButtonModel.isSelected() && snapshotButtonModel.isSelected()) {
                final ProductSceneView smosView = getSelectedSmosView();
                if (smosView != null) {
                    if (followModeButtonModel.isSelected()) {
                        locateSnapshotId(smosView, snapshotId);
                    }
                    updateAllImagesAndViews(smosView.getProduct(), snapshotId);
                }
            }
        }
    }

    private class SnapshotRegionOverlay implements LayerCanvas.Overlay {

        private long snapshotId;

        @Override
        public void paintOverlay(LayerCanvas canvas, Rendering rendering) {
            final ProductSceneView view = getSelectedSmosView();

            final SmosReader smosReader = SmosBox.getL1CScienceSmosReader(view);
            if (smosReader != null) {
                final Rectangle2D snapshotRegion = smosReader.getSnapshotInfo().getArea(snapshotId).getBounds2D();
                if (snapshotRegion != null) {
                    final Graphics2D graphics = rendering.getGraphics();
                    final Viewport vp = view.getLayerCanvas().getViewport();
                    final AffineTransform m2v = vp.getModelToViewTransform();
                    final Shape transformedShape = m2v.createTransformedShape(snapshotRegion);
                    final Color color = graphics.getColor();
                    graphics.setColor(Color.WHITE);
                    graphics.draw(transformedShape);
                    graphics.setColor(color);
                }
            }
        }

        void setId(long snapshotId) {
            this.snapshotId = snapshotId;
        }
    }

    private class ToggleSnapshotModeAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            final ProductSceneView smosView = getSelectedSmosView();
            final long snapshotId;

            if (snapshotButtonModel.isSelected()) {
                snapshotId = snapshotSelectorCombo.getSnapshotId();
                if (followModeButtonModel.isSelected()) {
                    locateSnapshotId(smosView, snapshotId);
                }
            } else {
                snapshotId = -1;
            }
            updateAllImagesAndViews(smosView.getProduct(), snapshotId);
            updateUI(smosView, snapshotId, false);
        }
    }

    private void updateAllImagesAndViews(Product smosProduct, long snapshotId) {
        final long xPolId;
        final long yPolId;
        final long crossPolId;

        if (snapshotId != -1) {
            final SmosReader smosReader = getSelectedSmosReader();
            final SnapshotInfo snapshotInfo = smosReader.getSnapshotInfo();
            xPolId = findSnapshotId(snapshotInfo.getSnapshotIdsX(), snapshotId);
            yPolId = findSnapshotId(snapshotInfo.getSnapshotIdsY(), snapshotId);
            crossPolId = findSnapshotId(snapshotInfo.getSnapshotIdsXY(), snapshotId);
        } else {
            xPolId = -1;
            yPolId = -1;
            crossPolId = -1;
        }

        for (final Band band : smosProduct.getBands()) {
            if (band.getName().equals(SmosConstants.LAND_SEA_MASK_NAME)) {
                continue;
            }
            if (band instanceof VirtualBand) {
                resetRasterImages(band);
            } else if (isXPolarized(band)) {
                updateSnapshotImage(band, xPolId);
            } else if (isYPolarized(band)) {
                updateSnapshotImage(band, yPolId);
            } else if (isXYPolarized(band)) {
                updateSnapshotImage(band, crossPolId);
            } else if (band.isFlagBand()) {
                updateSnapshotImage(band, snapshotId);
            } else {
                resetRasterImages(band);
            }
        }
        for (final Band band : smosProduct.getBands()) {
            if (band.getName().equals(SmosConstants.LAND_SEA_MASK_NAME)) {
                continue;
            }
            if (band instanceof VirtualBand) {
                updateViews(band, snapshotId);
            } else if (isXPolarized(band)) {
                updateViews(band, xPolId);
            } else if (isYPolarized(band)) {
                updateViews(band, yPolId);
            } else if (isXYPolarized(band)) {
                updateViews(band, crossPolId);
            } else {
                updateViews(band, snapshotId);
            }
        }
        for (final Band band : smosProduct.getBands()) {
            if (band.getName().equals(SmosConstants.LAND_SEA_MASK_NAME)) {
                continue;
            }
            if (band instanceof VirtualBand) {
                setSelectedSnapshotId(band, snapshotId);
            } else if (isXPolarized(band)) {
                setSelectedSnapshotId(band, xPolId);
            } else if (isYPolarized(band)) {
                setSelectedSnapshotId(band, yPolId);
            } else if (isXYPolarized(band)) {
                setSelectedSnapshotId(band, crossPolId);
            } else {
                setSelectedSnapshotId(band, snapshotId);
            }
        }
    }

    private void updateViews(RasterDataNode raster, long snapshotId) {
        final ProductSceneViewTopComponent sceneViewTopComponent = getProductSceneViewTopComponent(raster);
        if (sceneViewTopComponent != null) {
            final ProductSceneView view = sceneViewTopComponent.getView();
            if (getSelectedSnapshotId(view.getRaster()) != snapshotId) {
                regenerateImageLayers(view.getRootLayer());
            }
        }
    }

    private ProductSceneViewTopComponent getProductSceneViewTopComponent(RasterDataNode raster) {
        return WindowUtilities.getOpened(ProductSceneViewTopComponent.class)
                .filter(topComponent -> raster == topComponent.getView().getRaster())
                .findFirst()
                .orElse(null);
    }

    private class LocateSnapshotAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            final ProductSceneView smosView = getSelectedSmosView();
            locateSnapshotId(smosView, getSelectedSnapshotId(smosView));
        }
    }

    private JPanel createViewSettingsPanel() {
        final JCheckBox synchroniseCheckBox = new JCheckBox("Synchronise with view", false);
        synchronizeButtonModel = synchroniseCheckBox.getModel();
        synchronizeButtonModel.addActionListener(e -> {
            final ProductSceneView smosView = getSelectedSmosView();
            final long snapshotId = getSelectedSnapshotId(smosView);
            updateUI(smosView, snapshotId, false);
        });

        final ToggleSnapshotModeAction toggleSnapshotModeAction = new ToggleSnapshotModeAction();

        final JRadioButton browseButton = new JRadioButton("Browse", true);
        browseButtonModel = browseButton.getModel();
        browseButtonModel.addActionListener(toggleSnapshotModeAction);

        final JRadioButton snapshotButton = new JRadioButton("Snapshot", false);
        snapshotButtonModel = snapshotButton.getModel();
        snapshotButtonModel.addActionListener(toggleSnapshotModeAction);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(browseButton);
        buttonGroup.add(snapshotButton);

        final JCheckBox followModeCheckBox = new JCheckBox("Follow", false);
        followModeButtonModel = followModeCheckBox.getModel();

        final JButton locateSnapshotButton = new JButton("Locate in view");
        locateSnapshotButton.addActionListener(new LocateSnapshotAction());
        locateSnapshotButton.setToolTipText("Locate selected snapshot in view");
        locateSnapshotButtonModel = locateSnapshotButton.getModel();

        final JPanel viewSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
        viewSettingsPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        viewSettingsPanel.add(synchroniseCheckBox);
        viewSettingsPanel.add(browseButton);
        viewSettingsPanel.add(snapshotButton);
        viewSettingsPanel.add(followModeCheckBox);
        viewSettingsPanel.add(locateSnapshotButton);

        return viewSettingsPanel;
    }

    private void updateUI(ProductSceneView smosView, long snapshotId, boolean resetSelectorComboModel) {
        if (resetSelectorComboModel) {
            final SmosReader smosReader = SmosBox.getL1CScienceSmosReader(smosView);
            snapshotSelectorCombo.setModel(new SnapshotSelectorComboModel(smosReader.getSnapshotInfo()));
        }
        final boolean sync = synchronizeButtonModel.isSelected();
        if (sync) {
            final String bandName = smosView.getRaster().getName();
            if (bandName.endsWith("_X")) {
                snapshotSelectorCombo.setComboBoxSelectedIndex(1);
            } else if (bandName.endsWith("_Y")) {
                snapshotSelectorCombo.setComboBoxSelectedIndex(2);
            } else if (bandName.contains("_XY")) {
                snapshotSelectorCombo.setComboBoxSelectedIndex(3);
            } else {
                snapshotSelectorCombo.setComboBoxSelectedIndex(0);
            }
            if (snapshotId != -1) {
                snapshotSelectorCombo.setSnapshotId(snapshotId);
                snapshotButtonModel.setSelected(true);
            } else {
                browseButtonModel.setSelected(true);
            }
        }

        updateTable(snapshotSelectorCombo.getSnapshotId());

        snapshotSelectorCombo.setComboBoxEnabled(!sync);
        browseButtonModel.setEnabled(sync);
        snapshotButtonModel.setEnabled(sync);
        followModeButtonModel.setEnabled(sync && snapshotButtonModel.isSelected());
        locateSnapshotButtonModel.setEnabled(sync && snapshotButtonModel.isSelected());
    }

    private JPanel createSnapshotTablePanel() {
        snapshotTable = new JXTable(NULL_MODEL);
        snapshotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        snapshotTable.setSortable(false);
        snapshotTable.setEditable(false);
        snapshotTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number) {
                    setHorizontalAlignment(RIGHT);
                }
                return this;
            }
        });

        final JPopupMenu tablePopup = new JPopupMenu();
        final JMenuItem exportItem = new JMenuItem("Export...");

        final Container parent = getParent();
        final Window window = SwingUtilities.getWindowAncestor(parent);

        exportItem.addActionListener(e -> new TableModelExportRunner(window,
                                                                     getDisplayName(),
                                                                     snapshotTable.getModel(),
                                                                     (TableColumnModelExt) snapshotTable.getColumnModel()).run());
        tablePopup.add(exportItem);
        snapshotTable.add(tablePopup);

        MouseListener popupListener = new PopupListener(tablePopup);
        snapshotTable.addMouseListener(popupListener);

        final JPanel snapshotTablePanel = new JPanel(new BorderLayout());
        final JLabel tableLabel = new JLabel("Snapshot Information:");
        tableLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        snapshotTablePanel.add(tableLabel, BorderLayout.NORTH);
        snapshotTablePanel.add(new JScrollPane(snapshotTable), BorderLayout.CENTER);
        return snapshotTablePanel;
    }

    private void startPolModeInitWaiting(ProductSceneView smosView, SmosReader l1CScienceSmosReader) {
        final JPanel centerPanel = new JPanel(new CenterLayout());
        centerPanel.setPreferredSize(new Dimension(300, 200));
        final JPanel panel = new JPanel(new BorderLayout());
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        final JLabel message = new JLabel();
        panel.add(message, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        centerPanel.add(panel);
        setToolViewComponent(centerPanel);

        final ProgressMonitor progressMonitor = new ProgressBarProgressMonitor(progressBar, message);
        final SwingWorker worker = new PolModeWaiter(smosView, l1CScienceSmosReader, progressMonitor);
        progressMonitor.beginTask("Indexing snapshots and polarisation modes...", 100);
        worker.execute();
    }

    private class PolModeWaiter extends SwingWorker {

        private final ProductSceneView smosView;
        private final ProgressMonitor pm;
        private final SmosReader l1CScienceSmosReader;

        private PolModeWaiter(ProductSceneView smosView, SmosReader l1CScienceSmosReader, ProgressMonitor pm) {
            this.smosView = smosView;
            this.pm = pm;
            this.l1CScienceSmosReader = l1CScienceSmosReader;
        }

        @Override
        protected Object doInBackground() throws InterruptedException {
            try {
                while (!l1CScienceSmosReader.hasSnapshotInfo()) {
                    Thread.sleep(100);
                }
            } finally {
                pm.done();
            }
            return null;
        }

        @Override
        protected void done() {
            setToolViewComponent(getClientComponent());
            updateClientComponent(smosView);
        }
    }

    private static class PopupListener extends MouseAdapter {

        private final JPopupMenu popup;

        PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private static long findSnapshotId(List<Long> snapshotIds, long requestedId) {
        if (snapshotIds.size() == 0) {
            return -1;
        }
        final int foundIndex = Collections.binarySearch(snapshotIds, requestedId);
        if (foundIndex >= 0) {
            return snapshotIds.get(foundIndex);
        }
        final int lowerIndex = -(foundIndex + 1) - 1;
        final int upperIndex = -(foundIndex + 1);

        final long lowerId;
        if (lowerIndex < 0) {
            lowerId = snapshotIds.get(0);
        } else {
            lowerId = snapshotIds.get(lowerIndex);
        }
        final long upperId;
        if (upperIndex < snapshotIds.size()) {
            upperId = snapshotIds.get(upperIndex);
        } else {
            upperId = snapshotIds.get(snapshotIds.size() - 1);
        }
        final long foundId;
        if (requestedId - lowerId < upperId - requestedId) {
            foundId = lowerId;
        } else {
            foundId = upperId;
        }

        return foundId;
    }

    private static void regenerateImageLayers(Layer layer) {
        final List<Layer> children = layer.getChildren();
        for (int i = children.size(); i-- > 0; ) {
            final Layer child = children.get(i);
            if (child instanceof ImageLayer) {
                final ImageLayer imageLayer = (ImageLayer) child;

                // skip static image layers
                if (imageLayer.getMultiLevelSource() instanceof FileMultiLevelSource) {
                    continue;
                }
                if (imageLayer.getMultiLevelSource() instanceof TiledFileMultiLevelSource) {
                    continue;
                }
                imageLayer.regenerate();
            } else {
                if (child instanceof CollectionLayer) {
                    regenerateImageLayers(child);
                }
            }
        }
    }

    private static void resetRasterImages(final RasterDataNode raster) {
        raster.getSourceImage().reset();
        if (raster.isValidMaskImageSet()) {
            raster.resetValidMask();
            raster.getValidMaskImage().reset();
        }
        if (raster.isGeophysicalImageSet()) {
            raster.getGeophysicalImage().reset();
        }
        raster.setStx(null);
    }
}
