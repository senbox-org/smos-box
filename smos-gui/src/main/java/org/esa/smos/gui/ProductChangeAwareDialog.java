package org.esa.smos.gui;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.core.datamodel.ProductNodeEvent;
import org.esa.snap.core.datamodel.ProductNodeListener;
import org.esa.snap.rcp.util.SelectionSupport;
import org.esa.snap.ui.ModelessDialog;
import org.netbeans.api.annotations.common.NullAllowed;

import java.awt.Window;

abstract public class ProductChangeAwareDialog extends ModelessDialog implements SelectionSupport.Handler<Product> {


    protected ProductChangeAwareDialog(Window parent, String title, int buttonMask, String helpID) {
        super(parent, title, buttonMask, helpID);
    }

    protected void productRemoved(Product product) {
    }

    protected void productAdded() {
    }

    protected void geometryAdded() {
    }

    protected void geometryRemoved() {

    }

    protected void productSelectionChanged() {

    }

    protected static class ProductManagerListener implements ProductManager.Listener {

        private final ProductChangeAwareDialog dialog;

        public ProductManagerListener(ProductChangeAwareDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void productAdded(ProductManager.Event event) {
            dialog.productAdded();
        }

        @Override
        public void productRemoved(ProductManager.Event event) {
            dialog.productRemoved(event.getProduct());
        }
    }


    public static class GeometryListener implements ProductNodeListener {

        private final ProductChangeAwareDialog dialog;

        public GeometryListener(ProductChangeAwareDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void nodeChanged(ProductNodeEvent event) {
        }

        @Override
        public void nodeDataChanged(ProductNodeEvent event) {
        }

        @Override
        public void nodeAdded(ProductNodeEvent event) {
            dialog.geometryAdded();
        }

        @Override
        public void nodeRemoved(ProductNodeEvent event) {
            dialog.geometryRemoved();
        }
    }

    @Override
    public void selectionChange(@NullAllowed Product oldValue, @NullAllowed Product newValue) {
        if (oldValue != newValue) {
            productSelectionChanged();
        }
    }
}
