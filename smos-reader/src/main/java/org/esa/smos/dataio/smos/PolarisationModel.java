package org.esa.smos.dataio.smos;

public abstract class PolarisationModel {

    abstract public int getPolarisationMode(int flagValue);

    public boolean is_X_Polarised(int polarisationMode) {
        return polarisationMode == SmosConstants.L1C_POL_MODE_X;
    }

    public boolean is_Y_Polarised(int polarisationMode) {
        return polarisationMode == SmosConstants.L1C_POL_MODE_Y;
    }

    public boolean is_XY1_Polarised(int polarisationMode) {
        return polarisationMode == SmosConstants.L1C_POL_MODE_XY1;
    }

    public boolean is_XY2_Polarised(int polarisationMode) {
        return polarisationMode == SmosConstants.L1C_POL_MODE_XY2;
    }
}
