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

package org.esa.beam.smos.visat;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glayer.support.LayerUtils;
import org.esa.beam.dataio.smos.SmosReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.glayer.WorldMapLayerType;
import org.esa.beam.util.ResourceInstaller;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.VisatPlugIn;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SmosBox implements VisatPlugIn {

    private static final String WORLDMAP_TYPE_PROPERTY_NAME = "worldmap.type";
    private static final String BLUE_MARBLE_LAYER_TYPE = "BlueMarbleLayerType";

    private static volatile SmosBox instance;

    private volatile SnapshotSelectionService snapshotSelectionService;
    private volatile GridPointSelectionService gridPointSelectionService;
    private volatile SceneViewSelectionService sceneViewSelectionService;

    private boolean colorPalettesInstalled = false;

    public static SmosBox getInstance() {
        return instance;
    }

    public final SnapshotSelectionService getSnapshotSelectionService() {
        return snapshotSelectionService;
    }

    public final GridPointSelectionService getGridPointSelectionService() {
        return gridPointSelectionService;
    }

    public final SceneViewSelectionService getSmosViewSelectionService() {
        return sceneViewSelectionService;
    }

    @Override
    public final void start(VisatApp visatApp) {
        synchronized (this) {
            instance = this;
            sceneViewSelectionService = new SceneViewSelectionService(visatApp);
            snapshotSelectionService = new SnapshotSelectionService(sceneViewSelectionService);
            gridPointSelectionService = new GridPointSelectionService();

            sceneViewSelectionService.addSceneViewSelectionListener((oldView, newView) -> {
                if (newView != null) {
                    Layer worldMapLayer = findWorldMapLayer(newView);
                    if (worldMapLayer == null) {
                        worldMapLayer = createWorldMapLayer();
                        final Layer rootLayer = newView.getRootLayer();
                        rootLayer.getChildren().add(worldMapLayer);
                    }
                    worldMapLayer.setVisible(true);
                }
            });
        }

        if (!colorPalettesInstalled) {
            try {
                installColorPalettes();
            } catch (IOException e) {
                visatApp.getLogger().warning("Unable to install SMOS color palettes" + e.getMessage());
            }
        }
    }


    @Override
    public final void stop(VisatApp visatApp) {
        synchronized (this) {
            sceneViewSelectionService.stop();
            sceneViewSelectionService = null;
            snapshotSelectionService.stop();
            snapshotSelectionService = null;
            gridPointSelectionService.stop();
            gridPointSelectionService = null;
            instance = null;
        }
    }

    @Override
    public final void updateComponentTreeUI() {
    }

    static boolean isL1cScienceSmosRaster(RasterDataNode raster) {
        return getL1CScienceSmosReader(raster) != null;
    }

    static boolean isL1cScienceSmosView(ProductSceneView smosView) {
        return getL1CScienceSmosReader(smosView) != null;
    }

    static SmosReader getL1CScienceSmosReader(ProductSceneView smosView) {
        if (smosView == null) {
            return null;
        }

        return getL1CScienceSmosReader(smosView.getRaster());
    }

    private static SmosReader getL1CScienceSmosReader(RasterDataNode raster) {
        if (raster != null) {
            final ProductReader productReader = raster.getProductReader();
            if (productReader instanceof SmosReader) {
                final SmosReader smosReader = (SmosReader) productReader;
                if (smosReader.canSupplySnapshotData()) {
                    return smosReader;
                }
            }
        }

        return null;
    }

    private LayerType getWorldMapLayerType() {
        final VisatApp visatApp = VisatApp.getApp();
        String layerTypeClassName = visatApp.getPreferences().getPropertyString(WORLDMAP_TYPE_PROPERTY_NAME,
                                                                                BLUE_MARBLE_LAYER_TYPE);
        return LayerTypeRegistry.getLayerType(layerTypeClassName);
    }

    private Layer createWorldMapLayer() {
        final LayerType layerType = getWorldMapLayerType();
        final PropertySet template = layerType.createLayerConfig(null);
        template.setValue(ImageLayer.PROPERTY_NAME_PIXEL_BORDER_SHOWN, false);
        return layerType.createLayer(null, template);
    }

    private Layer findWorldMapLayer(ProductSceneView view) {
        return LayerUtils.getChildLayer(view.getRootLayer(), LayerUtils.SearchMode.DEEP,
                                        layer -> layer.getLayerType() instanceof WorldMapLayerType);
    }

    private void installColorPalettes() throws IOException {
        final URL codeSourceUrl = SmosBox.class.getProtectionDomain().getCodeSource().getLocation();
        final File auxdataDir = getSystemAuxdataDir();
        final ResourceInstaller resourceInstaller = new ResourceInstaller(codeSourceUrl, "auxdata/color_palettes/",
                                                                          auxdataDir);

        resourceInstaller.install(".*.cpd", ProgressMonitor.NULL);
        colorPalettesInstalled = true;
    }

    private File getSystemAuxdataDir() {
        // @todo 3 tb/** code duplicated from ColormanipulationForm class. we should have central services classes that over such services. tb 2014-09-18
        return new File(SystemUtils.getApplicationDataDir(), "snap-ui/auxdata/color-palettes");
    }
}
