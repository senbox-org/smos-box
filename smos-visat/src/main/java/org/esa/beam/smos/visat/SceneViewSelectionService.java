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

import com.bc.ceres.core.Assert;
import com.bc.ceres.glayer.support.ImageLayer;
import org.esa.beam.dataio.smos.SmosReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.PixelPositionListener;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.snap.rcp.SnapApp;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SceneViewSelectionService implements org.esa.snap.rcp.util.SelectionChangeSupport.Listener<ProductSceneView>{
    private final List<SelectionListener> selectionListeners;
    private final PPL ppl;

    private ProductSceneView selectedSceneView;

    public SceneViewSelectionService() {
        ppl = new PPL();
        this.selectionListeners = new ArrayList<>();

        SnapApp.getDefault().addProductSceneViewSelectionChangeListener(this);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void deselected(ProductSceneView first, ProductSceneView[] more) {
           setSelectedSceneView(null);
    }

    @Override
    public void selected(ProductSceneView first, ProductSceneView[] more) {
        setSelectedSceneView(first);
    }


    public void stop() {
        selectionListeners.clear();
    }

    public ProductSceneView getSelectedSceneView() {
        return selectedSceneView;
    }

    private void setSelectedSceneView(ProductSceneView newView) {
        ProductSceneView oldView = selectedSceneView;
        if (oldView != newView) {
            if (oldView != null) {
                oldView.removePixelPositionListener(ppl);
            }
            if (newView != null) {
                Assert.argument(newView.getProduct().getProductReader() instanceof SmosReader, "view");
            }
            selectedSceneView = newView;
            fireSelectionChange(oldView, newView);
            if (selectedSceneView != null) {
                selectedSceneView.addPixelPositionListener(ppl);
            }
        }
    }

    public Product getSelectedSmosProduct() {
        final ProductSceneView sceneView = getSelectedSceneView();
        return sceneView != null ? sceneView.getProduct() : null;
    }

    public SmosReader getSelectedSmosReader() {
        final Product product = getSelectedSmosProduct();
        if (product != null) {
            final ProductReader productReader = product.getProductReader();
            if (productReader instanceof SmosReader) {
                return (SmosReader) productReader;
            }
        }
        return null;
    }

    public int getGridPointId(int pixelX, int pixelY) {
        return getGridPointId(pixelX, pixelY, 0);
    }

    public int getGridPointId(int pixelX, int pixelY, int currentLevel) {
        final SmosReader smosReader = getSelectedSmosReader();
        if (smosReader != null) {
            return smosReader.getGridPointId(pixelX, pixelY, currentLevel);
        }
        return -1;
    }

    public synchronized void addSceneViewSelectionListener(SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
    }

    public synchronized void removeSceneViewSelectionListener(SelectionListener selectionListener) {
        selectionListeners.remove(selectionListener);
    }

    private void fireSelectionChange(ProductSceneView oldView, ProductSceneView newView) {
        for (SelectionListener selectionListener : selectionListeners) {
            selectionListener.handleSceneViewSelectionChanged(oldView, newView);
        }
    }

    public interface SelectionListener {
        void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView);
    }

    private class PPL implements PixelPositionListener {
        @Override
        public void pixelPosChanged(ImageLayer baseImageLayer, int pixelX, int pixelY, int currentLevel, boolean pixelPosValid, MouseEvent e) {
            int seqnum = -1;
            if (pixelPosValid) {
                seqnum = getGridPointId(pixelX, pixelY, currentLevel);
            }
            SmosBox.getInstance().getGridPointSelectionService().setSelectedGridPointId(seqnum);
        }

        @Override
        public void pixelPosNotAvailable() {
            SmosBox.getInstance().getGridPointSelectionService().setSelectedGridPointId(-1);
        }
    }
}
