package org.esa.beam.dataio.smos;


import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.dataio.AbstractProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;

import java.io.IOException;

abstract public class SmosReader extends AbstractProductReader{

    protected SmosReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    public abstract boolean canSupplyGridPointBtData();
    public abstract boolean canSupplyFullPolData();
    public abstract GridPointBtDataset getBtData(int gridPointIndex) throws IOException;
    public abstract int getGridPointIndex(int gridPointId);
    public abstract int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel);
    public abstract String[] getRawDataTableNames();

    public abstract FlagDescriptor[] getBtFlagDescriptors();
}
