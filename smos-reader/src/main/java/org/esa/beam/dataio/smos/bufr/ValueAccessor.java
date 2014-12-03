package org.esa.beam.dataio.smos.bufr;


import ucar.ma2.StructureData;

interface ValueAccessor {

    void read(StructureData snapshotData);
    int getGridPointId();
    int getRawValue();
}
