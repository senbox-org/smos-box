package org.esa.smos.ee2netcdf.reader;

import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.smos.SmosUtils;
import org.esa.smos.dataio.smos.*;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ScienceProductSupport extends AbstractProductTypeSupport {

    private final String typeString;
    private Future<SnapshotInfo> snapshotInfoFuture;
    private double incidentAngleScaleFactor;

    ScienceProductSupport(NetcdfFile netcdfFile, String typeString) {
        super(netcdfFile);
        this.typeString = typeString;
    }

    @Override
    public void initialize(Family<BandDescriptor> bandDescriptors) {
        boolean found = false;
        final List<BandDescriptor> bandDescriptorList = bandDescriptors.asList();
        for (final BandDescriptor bandDescriptor : bandDescriptorList) {
            if (bandDescriptor.getMemberName().startsWith(SmosConstants.INCIDENCE_ANGLE)) {
                incidentAngleScaleFactor = bandDescriptor.getScalingFactor();
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("No " + SmosConstants.INCIDENCE_ANGLE + " variable found");
        }
        super.initialize(bandDescriptors);
    }

    @Override
    public String getLatitudeBandName() {
        return "Grid_Point_Latitude";
    }

    @Override
    public String getLongitudeBandName() {
        return "Grid_Point_Longitude";
    }

    @Override
    public ValueProvider createValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo) {
        if (descriptor.getMemberName().equals("Flags")) {

            return new ScienceFlagsValueProvider(arrayCache, variableName, descriptor, area, gridPointInfo, incidentAngleScaleFactor);
        } else {
            return new ScienceValueProvider(arrayCache, variableName, descriptor, area, gridPointInfo, incidentAngleScaleFactor);
        }
    }

    @Override
    public void createAdditionalBands(Product product, Area area, Family<BandDescriptor> bandDescriptors, String formatName) {
        if (SmosUtils.isDualPolScienceFormat(formatName)) {
            addRotatedDualPoleBands(product, area, bandDescriptors);
        } else {
            addRotatedFullPoleBands(product, area, bandDescriptors);
        }
    }

    @Override
    public boolean canSupplyGridPointBtData() {
        return true;
    }

    @Override
    public boolean canSupplyFullPolData() {
        return SmosUtils.isFullPolScienceFormat(typeString);
    }

    @Override
    public boolean canSupplySnapshotData() {
        return true;
    }

    @Override
    public boolean hasSnapshotInfo() {
        if (snapshotInfoFuture == null) {
            snapshotInfoFuture = Executors.newSingleThreadExecutor().submit(() -> createSnapshotInfo());
        }
        return snapshotInfoFuture.isDone();
    }

    @Override
    public SnapshotInfo getSnapshotInfo() throws IOException {
        try {
            return snapshotInfoFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) {
        try {
            final Object[][] snapshotData = new Object[snapshotDataNames.length][2];
            for (int i = 0; i < snapshotDataNames.length; i++) {
                final String variableName = snapshotDataNames[i];
                final Array array = arrayCache.get(variableName);
                snapshotData[i][0] = variableName;
                snapshotData[i][1] = array.getDouble(snapshotIndex);
            }
            return snapshotData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SnapshotInfo createSnapshotInfo() throws IOException {
        ensureDataStructuresInitialized();

        final Set<Long> all = new TreeSet<>();
        final Set<Long> x = new TreeSet<>();
        final Set<Long> y = new TreeSet<>();
        final Set<Long> xy = new TreeSet<>();

        final Map<Long, Rectangle2D> snapshotAreaMap = new TreeMap<>();
        final Array latitude = arrayCache.get("Grid_Point_Latitude");
        final Array longitude = arrayCache.get("Grid_Point_Longitude");
        final Array btDataCounter = arrayCache.get("BT_Data_Counter");
        final Array snapshotIdOfPixel = arrayCache.get("Snapshot_ID_of_Pixel");
        final Array flags = arrayCache.get("Flags");
        final Array snapshotId = arrayCache.get("Snapshot_ID");

        final Dimension dimension = netcdfFile.findDimension("n_grid_points");
        final int gridPointCount = dimension.getLength();

        for (int i = 0; i < gridPointCount; i++) {
            final int btCount = btDataCounter.getInt(i);
            final Index snapshotIdOfPixelIndex = snapshotIdOfPixel.getIndex();
            final Index flagsIndex = flags.getIndex();

            if (btCount > 0) {
                double lon = longitude.getDouble(i);
                double lat = latitude.getDouble(i);
                // normalisation to [-180, 180] necessary for some L1c test products
                if (lon > 180.0) {
                    lon -= 360.0;
                }
                final Rectangle2D rectangle = DggUtils.createGridPointRectangle(lon, lat);

                long lastId = -1;
                for (int j = 0; j < btCount; j++) {
                    snapshotIdOfPixelIndex.set(i, j);
                    final long id = snapshotIdOfPixel.getLong(snapshotIdOfPixelIndex);

                    if (lastId != id) { // snapshots are ordered
                        all.add(id);
                        if (snapshotAreaMap.containsKey(id)) {
                            // todo: rq/rq - snapshots on the anti-meridian, use area instead of rectangle (2009-10-22)
                            snapshotAreaMap.get(id).add(rectangle);
                        } else {
                            snapshotAreaMap.put(id, rectangle);
                        }
                        lastId = id;
                    }

                    flagsIndex.set(i, j);
                    final int flag = flags.getInt(flagsIndex);
                    switch (flag & SmosConstants.L1C_POL_MODE_FLAGS_MASK) {
                        case SmosConstants.L1C_POL_MODE_X:
                            x.add(id);
                            break;
                        case SmosConstants.L1C_POL_MODE_Y:
                            y.add(id);
                            break;
                        case SmosConstants.L1C_POL_MODE_XY1:
                        case SmosConstants.L1C_POL_MODE_XY2:
                            xy.add(id);
                            break;
                    }
                }
            }
        }

        final Map<Long, Integer> snapshotIndexMap = new TreeMap<>();
        final Dimension snapshotDimension = netcdfFile.findDimension("n_snapshots");
        final int snapshotCount = snapshotDimension.getLength();

        for (int i = 0; i < snapshotCount; i++) {
            final long id = snapshotId.getLong(i);
            if (all.contains(id)) {
                snapshotIndexMap.put(id, i);
            }
        }

        // load snapshot variables
        for (String variableName : snapshotDataNames) {
            arrayCache.get(variableName);
        }

        return new SnapshotInfo(snapshotIndexMap, all, x, y, xy, snapshotAreaMap);
    }

    @Override
    public FlagDescriptor[] getBtFlagDescriptors() {
        try {
            ensureDataStructuresInitialized();
        } catch (IOException e) {
            // @todo 2 tb/tb ad logging here
        }
        return flagDescriptors;
    }

    @Override
    public PolarisationModel getPolarisationModel() {
        return new L1cPolarisationModel();
    }

    private void addRotatedFullPoleBands(Product product, Area area, Family<BandDescriptor> bandDescriptors) {
        BandDescriptor descriptor = bandDescriptors.getMember("BT_Value_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("BT_Value_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("BT_Value_HV_Real");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("BT_Value_HV_Imag");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_HV");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Stokes_1");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Stokes_2");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Stokes_3");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Stokes_4");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));
    }

    private void addRotatedDualPoleBands(Product product, Area area, Family<BandDescriptor> bandDescriptors) {
        BandDescriptor descriptor = bandDescriptors.getMember("BT_Value_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("BT_Value_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Stokes_1");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));

        descriptor = bandDescriptors.getMember("Stokes_2");
        addRotatedBand(product, descriptor, new FaradayValueProvider(area));
    }

    private void addRotatedBand(Product product, BandDescriptor descriptor, ValueProvider valueProvider) {
        if (!descriptor.isVisible()) {
            return;
        }
        final Band band = product.addBand(descriptor.getBandName(), ProductData.TYPE_FLOAT32);

        band.setUnit(descriptor.getUnit());
        band.setDescription(descriptor.getDescription());

        if (descriptor.hasFillValue()) {
            band.setNoDataValueUsed(true);
            band.setNoDataValue(descriptor.getFillValue());
        }
        final SmosMultiLevelSource smosMultiLevelSource = new SmosMultiLevelSource(band, valueProvider);
        final DefaultMultiLevelImage defaultMultiLevelImage = new DefaultMultiLevelImage(smosMultiLevelSource);
        band.setSourceImage(defaultMultiLevelImage);
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }
}
