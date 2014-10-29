package org.esa.beam.dataio.smos.bufr;

import org.esa.beam.dataio.smos.PolarisationModel;

public class BufrPolarisationModel extends PolarisationModel {

    @Override
    public int getPolarisationMode(int flagValue) {
        return flagValue;
    }
}
