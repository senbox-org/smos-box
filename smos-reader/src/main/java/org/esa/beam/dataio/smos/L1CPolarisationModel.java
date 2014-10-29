package org.esa.beam.dataio.smos;

class L1CPolarisationModel extends PolarisationModel {

    @Override
    public int getPolarisationMode(int flagValue) {
        return flagValue & SmosConstants.L1C_POL_MODE_FLAGS_MASK;
    }
}
