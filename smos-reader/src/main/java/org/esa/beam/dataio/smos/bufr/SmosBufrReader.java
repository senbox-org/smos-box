package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.beam.dataio.smos.*;
import org.esa.beam.dataio.smos.dddb.FlagDescriptor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.dgg.SmosDgg;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ralf Quast
 */
public class SmosBufrReader extends SmosReader {

    private BufrSupport bufrSupport;
    private ValueDecoders valueDecoders;

    private final Map<Integer, IndexArea> snapshotMessageIndexMap;

    public SmosBufrReader(SmosBufrReaderPlugIn smosBufrReaderPlugIn) {
        super(smosBufrReaderPlugIn);

        snapshotMessageIndexMap = new HashMap<>();
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
        final File inputFile = getInputFile();
        bufrSupport = new BufrSupport();
        bufrSupport.open(inputFile.getPath());

        final Product product = bufrSupport.createProduct(inputFile, "SMOS.MIRAS.NRT_BUFR");

        bufrSupport.extractMetaData(product);
        valueDecoders = bufrSupport.extractValueDecoders();

        scanFile();

        return product;
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (bufrSupport != null) {
            bufrSupport.close();
            bufrSupport = null;
        }
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

    private void scanFile() throws IOException {

        for (int i = 0, messageCount = bufrSupport.getMessageCount(); i < messageCount; i++) {
            final IndexArea current = new IndexArea(i);
            int snapshotId = -1;

            final StructureDataIterator observationIterator = bufrSupport.getStructureIterator(i);
            boolean firstIteration = true;
            Rectangle2D snapshotArea = null;
            while (observationIterator.hasNext()) {
                final StructureData structureData = observationIterator.next();

                if (firstIteration) {
                    snapshotId = structureData.getScalarInt(SmosBufrFile.SNAPSHOT_IDENTIFIER);
                    firstIteration = false;
                }

                final int highAccuracyLon = structureData.getScalarInt(SmosBufrFile.LONGITUDE_HIGH_ACCURACY);
                final float lon = (float) valueDecoders.lonDecoder.decode(highAccuracyLon);

                final int highAccuracyLat = structureData.getScalarInt(SmosBufrFile.LATITUDE_HIGH_ACCURACY);
                final float lat = (float) valueDecoders.latDecoder.decode(highAccuracyLat);

                if (snapshotArea == null) {
                    snapshotArea = DggUtils.createGridPointRectangle(lon, lat);
                } else {
                    final Rectangle2D gridPointRectangle = DggUtils.createGridPointRectangle(lon, lat);
                    snapshotArea.add(gridPointRectangle);
                }
            }

            current.setArea(snapshotArea);
            snapshotMessageIndexMap.put(snapshotId, current);
        }
    }
}
