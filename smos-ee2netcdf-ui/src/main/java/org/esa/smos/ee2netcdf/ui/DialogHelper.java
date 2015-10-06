package org.esa.smos.ee2netcdf.ui;

import org.esa.smos.ee2netcdf.ExportParameter;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.ui.AppContext;

class DialogHelper {

    static boolean isSupportedType(String productType) {
        return productType.matches(ExportParameter.PRODUCT_TYPE_REGEX);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    static boolean isProductSelectionFeasible(AppContext appContext) {
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct != null) {
            return isSupportedType(selectedProduct.getProductType());
        }
        return false;
    }

    static Product getSelectedSmosProduct(AppContext appContext) {
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct != null) {
            if (isSupportedType(selectedProduct.getProductType())) {
                return selectedProduct;
            }
        }
        return null;
    }
}
