package org.esa.smos.ee2netcdf.reader;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.PolarisationModel;
import org.esa.smos.dataio.smos.ProductHelper;
import org.esa.smos.dataio.smos.SmosReader;
import org.esa.smos.dataio.smos.SnapshotInfo;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.snap.framework.dataio.AbstractProductReader;
import org.esa.snap.framework.dataio.ProductReader;
import org.esa.snap.framework.dataio.ProductReaderPlugIn;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductData;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

public class NetcdfProductReader extends SmosReader {


    private NetcdfFile netcdfFile;

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be <code>null</code> for internal reader
     *                     implementations
     */
    protected NetcdfProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
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
        Product product;

        synchronized (this) {
            final File inputFile = getInputFile();
            netcdfFile = NetcdfFileOpener.open(inputFile.getAbsolutePath());
            if (netcdfFile == null) {
                throw new IOException("Unable to read file");
            }
            final Attribute fileTypeAttrbute = netcdfFile.findGlobalAttribute("Fixed_Header:File_Type");

            product = ProductHelper.createProduct(inputFile, fileTypeAttrbute.getStringValue());
        }

        return product;
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY, Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }
}
