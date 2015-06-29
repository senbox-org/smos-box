package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.framework.datamodel.Band;

interface ProductTypeSupport {

    String getLatitudeBandName();
    String getLongitudeBandName();

    void setScalingAndOffset(Band band, BandDescriptor bandDescriptor);
}
