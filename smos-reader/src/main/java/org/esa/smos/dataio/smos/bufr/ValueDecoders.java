package org.esa.smos.dataio.smos.bufr;

class ValueDecoders {

    ValueDecoder lonDecoder;
    ValueDecoder latDecoder;
    ValueDecoder incidenceAngleDecoder;

    ValueDecoder tecDecoder;
    ValueDecoder snapshotAccuracyDecoder;
    ValueDecoder raPpDecoder;
    ValueDecoder raCpDecoder;

    ValueDecoder[] dataDecoders;
}
