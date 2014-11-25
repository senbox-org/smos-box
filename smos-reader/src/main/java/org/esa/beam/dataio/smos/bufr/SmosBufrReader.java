package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.smos.GridPointBtDataset;
import org.esa.beam.dataio.smos.PolarisationModel;
import org.esa.beam.dataio.smos.SmosReader;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;

import java.io.IOException;

/**
 * @author Ralf Quast
 */
public class SmosBufrReader extends SmosReader {

    public SmosBufrReader(SmosBufrReaderPlugIn smosBufrReaderPlugIn) {
        super(smosBufrReaderPlugIn);
    }

    @Override
    public boolean canSupplyGridPointBtData() {
        return false;
    }

    @Override
    public boolean canSupplyFullPolData() {
        return false;
    }

    @Override
    public GridPointBtDataset getBtData(int gridPointIndex) throws IOException {
        return null;
    }

    @Override
    public int getGridPointIndex(int gridPointId) {
        return 0;
    }

    @Override
    public int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel) {
        return 0;
    }

    @Override
    public String[] getRawDataTableNames() {
        return new String[0];
    }

    @Override
    public FlagDescriptor[] getBtFlagDescriptors() {
        return new FlagDescriptor[0];
    }

    @Override
    public PolarisationModel getPolarisationModel() {
        return null;
    }

    @Override
    public boolean canSupplySnapshotData() {
        return false;
    }

    @Override
    public boolean hasSnapshotInfo() {
        return false;
    }

    @Override
    public SnapshotInfo getSnapshotInfo() {
        return null;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) throws IOException {
        return new Object[0][];
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        return null;
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {

    }
}
