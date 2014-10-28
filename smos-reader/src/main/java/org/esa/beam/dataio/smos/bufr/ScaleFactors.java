package org.esa.beam.dataio.smos.bufr;

import java.util.HashMap;

class ScaleFactors {

    ScaleFactor lon;
    ScaleFactor lat;
    ScaleFactor incidenceAngle;

    HashMap<String, ScaleFactor> bandScaleFactors = new HashMap<>();
}
