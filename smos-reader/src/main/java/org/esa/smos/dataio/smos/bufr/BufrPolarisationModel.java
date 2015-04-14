package org.esa.smos.dataio.smos.bufr;

import org.esa.smos.dataio.smos.PolarisationModel;

public class BufrPolarisationModel extends PolarisationModel {

    @Override
    public int getPolarisationMode(int flagValue) {
        return flagValue;
    }
}
