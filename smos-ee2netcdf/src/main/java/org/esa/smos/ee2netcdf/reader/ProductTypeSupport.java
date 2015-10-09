package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.PolarisationModel;
import org.esa.smos.dataio.smos.SnapshotInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import ucar.nc2.Variable;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.HashMap;

interface ProductTypeSupport {

    boolean canSupplyGridPointBtData();
    boolean canSupplyFullPolData();
    GridPointBtDataset getBtData(int gridPointIndex) throws IOException;
    String[] getRawDataTableNames();
    FlagDescriptor[] getBtFlagDescriptors();
    PolarisationModel getPolarisationModel();
    boolean canSupplySnapshotData();
    boolean hasSnapshotInfo();
    SnapshotInfo getSnapshotInfo();
    Object[][] getSnapshotData(int snapshotIndex);

    String getLatitudeBandName();
    String getLongitudeBandName();

    void setScalingAndOffset(Band band, BandDescriptor bandDescriptor);

    ValueProvider createValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo);

    void setArrayCache(ArrayCache arrayCache);

    void createAdditionalBands(Product product, Family<BandDescriptor> bandDescriptors, String formatName);

    void setGridPointInfo(GridPointInfo gridPointInfo);
}
