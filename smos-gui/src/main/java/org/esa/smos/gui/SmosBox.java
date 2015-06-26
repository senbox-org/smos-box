package org.esa.smos.gui;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glayer.support.LayerUtils;
import org.esa.smos.dataio.smos.SmosReader;
import org.esa.smos.gui.gridpoint.GridPointSelectionService;
import org.esa.smos.gui.snapshot.SnapshotSelectionService;
import org.esa.snap.framework.dataio.ProductReader;
import org.esa.snap.framework.datamodel.RasterDataNode;
import org.esa.snap.framework.ui.product.ProductSceneView;
import org.esa.snap.glayer.WorldMapLayerType;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.util.ResourceInstaller;
import org.esa.snap.util.SystemUtils;
import org.esa.snap.util.io.FileUtils;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.windows.OnShowing;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class SmosBox {

    private static final Logger LOGGER = Logger.getLogger(SmosBox.class.getName());
    private static final String WORLDMAP_TYPE_PROPERTY_NAME = "worldmap.type";
    private static final String BLUE_MARBLE_LAYER_TYPE = "BlueMarbleLayerType";

    private static volatile SmosBox instance;

    private static volatile SnapshotSelectionService snapshotSelectionService;
    private static volatile GridPointSelectionService gridPointSelectionService;
    private static volatile SceneViewSelectionService sceneViewSelectionService;

    private static boolean colorPalettesInstalled = false;

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


    private SmosBox() {
    }

    @OnStart
    public static class StartOp implements Runnable {

        @Override
        public void run() {
            LOGGER.info("Starting SmosBox Desktop");
            instance = new SmosBox();

            sceneViewSelectionService = new SceneViewSelectionService();
            snapshotSelectionService = new SnapshotSelectionService(sceneViewSelectionService);
            gridPointSelectionService = new GridPointSelectionService();
        }
    }

    @OnShowing()
    public static class ShowingOp implements Runnable {

        @Override
        public void run() {
            LOGGER.info("Showing SmosBox Desktop");
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

            if (!colorPalettesInstalled) {
                try {
                    installColorPalettes();
                } catch (IOException e) {
                    LOGGER.warning("Unable to install SMOS color palettes" + e.getMessage());
                }
            }
        }
    }

    @OnStop
    public static class StopOp implements Runnable {

        @Override
        public void run() {
            LOGGER.info("Stopping SmosBox Desktop");
            sceneViewSelectionService.stop();
            sceneViewSelectionService = null;
            snapshotSelectionService.stop();
            snapshotSelectionService = null;
            gridPointSelectionService.stop();
            gridPointSelectionService = null;
            instance = null;
        }
    }

    public static boolean isL1cScienceSmosRaster(RasterDataNode raster) {
        return getL1CScienceSmosReader(raster) != null;
    }

    public static boolean isL1cScienceSmosView(ProductSceneView smosView) {
        return getL1CScienceSmosReader(smosView) != null;
    }

    public static SmosReader getL1CScienceSmosReader(ProductSceneView smosView) {
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

    private static LayerType getWorldMapLayerType() {
        String layerTypeClassName = SnapApp.getDefault().getPreferences().get(WORLDMAP_TYPE_PROPERTY_NAME, BLUE_MARBLE_LAYER_TYPE);
        return LayerTypeRegistry.getLayerType(layerTypeClassName);
    }

    private static Layer createWorldMapLayer() {
        final LayerType layerType = getWorldMapLayerType();
        final PropertySet template = layerType.createLayerConfig(null);
        template.setValue(ImageLayer.PROPERTY_NAME_PIXEL_BORDER_SHOWN, false);
        return layerType.createLayer(null, template);
    }

    private static Layer findWorldMapLayer(ProductSceneView view) {
        return LayerUtils.getChildLayer(view.getRootLayer(), LayerUtils.SearchMode.DEEP,
                                        layer -> layer.getLayerType() instanceof WorldMapLayerType);
    }

    private static void installColorPalettes() throws IOException {
        final Path codeSourceUrl;
        try {
            codeSourceUrl = FileUtils.getPathFromURI(SmosBox.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            final File auxdataDir = getSystemAuxdataDir();
            Path sourceDirPath = codeSourceUrl.resolve("auxdata/color_palettes");
            final ResourceInstaller resourceInstaller = new ResourceInstaller(sourceDirPath, auxdataDir.toPath());

            resourceInstaller.install(".*.cpd", ProgressMonitor.NULL);
            colorPalettesInstalled = true;
        } catch (URISyntaxException e) {
            throw new IOException("Could not install colour palettes.", e);
        }
    }

    private static File getSystemAuxdataDir() {
        // @todo 3 tb/** code duplicated from ColormanipulationForm class. we should have central services classes that over such services. tb 2014-09-18
        return new File(SystemUtils.getApplicationDataDir(), "snap-rcp/auxdata/color-palettes");
    }

}
