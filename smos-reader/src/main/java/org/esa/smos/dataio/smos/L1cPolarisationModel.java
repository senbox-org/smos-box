package org.esa.smos.dataio.smos;

class L1cPolarisationModel extends PolarisationModel {

    @Override
    public int getPolarisationMode(int flagValue) {
        return flagValue & SmosConstants.L1C_POL_MODE_FLAGS_MASK;
    }
}
