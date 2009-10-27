package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.HashMap;

class FPV extends FP {

    FPV(Product product, HashMap<String, FieldValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, true);
    }

    @Override
    protected double compute(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return (aa + bb) * btxy;
    }
}