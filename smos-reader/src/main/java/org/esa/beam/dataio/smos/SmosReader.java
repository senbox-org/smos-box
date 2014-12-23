package org.esa.beam.dataio.smos;


import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

abstract public class SmosReader extends AbstractProductReader{

    protected SmosReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    // @todo 2 tb/** this wants to be a separate class 2014-10-30 -------------------
    public abstract boolean canSupplyGridPointBtData();
    public abstract boolean canSupplyFullPolData();
    public abstract GridPointBtDataset getBtData(int gridPointIndex) throws IOException;
    public abstract int getGridPointIndex(int gridPointId);
    public abstract int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel);
    public abstract String[] getRawDataTableNames();
    public abstract FlagDescriptor[] getBtFlagDescriptors();
    public abstract PolarisationModel getPolarisationModel();
    // ------------------------------------------------------------------------------

    public abstract boolean canSupplySnapshotData();
    public abstract boolean hasSnapshotInfo();
    public abstract SnapshotInfo getSnapshotInfo();
    public abstract Object[][] getSnapshotData(int snapshotIndex) throws IOException;

    protected File getInputFile() {
        final Object input = getInput();

        if (input instanceof String) {
            return new File((String) input);
        }
        if (input instanceof File) {
            return (File) input;
        }

        throw new IllegalArgumentException(MessageFormat.format("Illegal input: {0}", input));
    }
}
