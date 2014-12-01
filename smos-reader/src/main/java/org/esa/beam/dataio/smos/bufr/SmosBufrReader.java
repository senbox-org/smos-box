package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.beam.dataio.smos.GridPointBtDataset;
import org.esa.beam.dataio.smos.PolarisationModel;
import org.esa.beam.dataio.smos.SmosReader;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;

/**
 * @author Ralf Quast
 */
public class SmosBufrReader extends SmosReader {

    public SmosBufrReader(SmosBufrReaderPlugIn smosBufrReaderPlugIn) {
        super(smosBufrReaderPlugIn);
    }

    @Override
    public final boolean canSupplyGridPointBtData() {
        return false;
    }

    @Override
    public final boolean canSupplyFullPolData() {
        return true;
    }

    @Override
    public final GridPointBtDataset getBtData(int gridPointIndex) throws IOException {
        return null;
    }

    @Override
    public final int getGridPointIndex(int gridPointId) {
        return -1;
    }

    @Override
    public int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel) {
        // TODO - check
        final MultiLevelImage levelImage = SmosDgg.getInstance().getMultiLevelImage();
        final RenderedImage image = levelImage.getImage(currentLevel);
        final Raster data = image.getData(new Rectangle(levelPixelX, levelPixelY, 1, 1));
        return data.getSample(levelPixelX, levelPixelY, 0);
    }

    @Override
    public final String[] getRawDataTableNames() {
        return null;
    }

    @Override
    public final FlagDescriptor[] getBtFlagDescriptors() {
        return null;
    }

    @Override
    public final PolarisationModel getPolarisationModel() {
        return new BufrPolarisationModel();
    }

    @Override
    public final boolean canSupplySnapshotData() {
        return true;
    }

    @Override
    public final boolean hasSnapshotInfo() {
        return true;
    }

    @Override
    public SnapshotInfo getSnapshotInfo() {
        // TODO - implement
        return null;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) throws IOException {
        // TODO - implement
        return new Object[0][];
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        // TODO - implement
        return null;
    }

    @Override
    protected final void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {
        synchronized (this) {
            final RenderedImage image = destBand.getSourceImage();
            final Raster data = image.getData(new Rectangle(destOffsetX, destOffsetY, destWidth, destHeight));

            data.getDataElements(destOffsetX, destOffsetY, destWidth, destHeight, destBuffer.getElems());
        }
    }

}
