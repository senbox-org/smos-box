package org.esa.smos.ee2netcdf.reader;

import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.smos.SmosUtils;
import org.esa.smos.dataio.smos.*;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dataio.smos.provider.*;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

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

    static boolean allRequiredArrayPresent(Array latitude, Array longitude, Array btDataCounter, Array snapshotIdOfPixel, Array flags, Array snapshotId) {
        return latitude != null && longitude != null && btDataCounter != null && snapshotIdOfPixel != null && flags != null & snapshotId != null;
    }

    static boolean containsAccuracy_XY_Bands(Product product) {
        return product.containsBand("Pixel_Radiometric_Accuracy_X") && product.containsBand("Pixel_Radiometric_Accuracy_Y");
    }

    static boolean containsAccuracy_XY_FP_Bands(Product product) {
        return containsAccuracy_XY_Bands(product) && product.containsBand("Pixel_Radiometric_Accuracy_XY");
    }

    static boolean containsBT_XY_Bands(Product product) {
        return product.containsBand("BT_Value_X") && product.containsBand("BT_Value_Y");
    }

    static boolean containsBT_XY_FP_Bands(Product product) {
        return containsBT_XY_Bands(product) && product.containsBand("BT_Value_XY_Real");
    }

    static boolean containsAllRotationBands(Product product) {
        return product.containsBand("Faraday_Rotation_Angle_X") && product.containsBand("Faraday_Rotation_Angle_Y") && product.containsBand("Geometric_Rotation_Angle_X") && product.containsBand("Geometric_Rotation_Angle_Y");
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
    public boolean canOpenFile() {
        return containsVariable("Grid_Point_Latitude") &&
                containsVariable("Grid_Point_Longitude") &&
                containsVariable("Grid_Point_ID") &&
                containsVariable("Flags") &&
                containsVariable("Incidence_Angle");
    }

    @Override
    public AbstractValueProvider createValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo) {
        if (descriptor.getMemberName().equals("Flags")) {
            final Variable incidenceAngleVariable = netcdfFile.findVariable(SmosConstants.INCIDENCE_ANGLE);
            if (incidenceAngleVariable == null) {
                return null;
            }
            return new ScienceFlagsValueProvider(arrayCache, variableName, descriptor, area, gridPointInfo, incidentAngleScaleFactor);
        } else {
            return new ScienceValueProvider(arrayCache, variableName, descriptor, area, gridPointInfo, incidentAngleScaleFactor);
        }
    }

    @Override
    public void createAdditionalBands(Product product, Area area, Family<BandDescriptor> bandDescriptors, String formatName, HashMap<String, AbstractValueProvider> valueProvierMap) {
        if (SmosUtils.isDualPolScienceFormat(formatName)) {
            addRotatedDualPoleBands(product, bandDescriptors, valueProvierMap);
        } else {
            addRotatedFullPoleBands(product, bandDescriptors, valueProvierMap);
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
            snapshotInfoFuture = Executors.newSingleThreadExecutor().submit(this::createSnapshotInfo);
        }

        return snapshotInfoFuture.isDone();
    }

    @Override
    public SnapshotInfo getSnapshotInfo() {
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
        final Map<Long, Integer> snapshotIndexMap = new TreeMap<>();

        final Array latitude = arrayCache.get("Grid_Point_Latitude");
        final Array longitude = arrayCache.get("Grid_Point_Longitude");
        final Array btDataCounter = arrayCache.get("BT_Data_Counter");
        final Array snapshotIdOfPixel = arrayCache.get("Snapshot_ID_of_Pixel");
        final Array flags = arrayCache.get("Flags");
        final Array snapshotId = arrayCache.get("Snapshot_ID");

        if (allRequiredArrayPresent(latitude, longitude, btDataCounter, snapshotIdOfPixel, flags, snapshotId)) {
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
        }

        final Family<BandDescriptor> bandDescriptors = Dddb.getInstance().getBandDescriptors(typeString);
        final List<BandDescriptor> bandDescriptorsList = bandDescriptors.asList();
        List<FlagDescriptor> flagDescriptorList = null;
        for (final BandDescriptor descriptor : bandDescriptorsList) {
            if (descriptor.getBandName().equals("RFI_Flags")) {
                final Family<FlagDescriptor> flagDescriptors = descriptor.getFlagDescriptors();
                flagDescriptorList = flagDescriptors.asList();
                break;
            }
        }

        return new SnapshotInfo(snapshotIndexMap, all, x, y, xy, snapshotAreaMap, flagDescriptorList);
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

    private void addRotatedFullPoleBands(Product product, Family<BandDescriptor> bandDescriptors, HashMap<String, AbstractValueProvider> valueProviderMap) {
        if (containsAllRotationBands(product)) {
            FP valueProvider;
            BandDescriptor descriptor;

            if (containsBT_XY_FP_Bands(product)) {
                valueProvider = new FPH(product, valueProviderMap, false);
                descriptor = bandDescriptors.getMember("BT_Value_H");
                addRotatedBand(product, descriptor, valueProvider);

                valueProvider = new FPV(product, valueProviderMap, false);
                descriptor = bandDescriptors.getMember("BT_Value_V");
                addRotatedBand(product, descriptor, valueProvider);

                valueProvider = new FPHVR(product, valueProviderMap, false);
                descriptor = bandDescriptors.getMember("BT_Value_HV_Real");
                addRotatedBand(product, descriptor, valueProvider);

                ProductHelper.addVirtualBand(product, bandDescriptors.getMember("Stokes_1"), "(BT_Value_X + BT_Value_Y) / 2.0");
                ProductHelper.addVirtualBand(product, bandDescriptors.getMember("Stokes_2"), "(BT_Value_H - BT_Value_V) / 2.0");
                ProductHelper.addVirtualBand(product, bandDescriptors.getMember("Stokes_3"), "BT_Value_HV_Real");
            }

            if (product.containsBand("BT_Value_XY_Imag")) {
                ProductHelper.addVirtualBand(product, bandDescriptors.getMember("BT_Value_HV_Imag"), "BT_Value_XY_Imag");
            }

            if (containsAccuracy_XY_FP_Bands(product)) {
                valueProvider = new FPH(product, valueProviderMap, true);
                descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
                addRotatedBand(product, descriptor, valueProvider);

                valueProvider = new FPV(product, valueProviderMap, true);
                descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
                addRotatedBand(product, descriptor, valueProvider);

                valueProvider = new FPHVR(product, valueProviderMap, true);
                descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_HV");
                addRotatedBand(product, descriptor, valueProvider);
            }

            if (product.containsBand("BT_Value_XY_Imag")) {
                ProductHelper.addVirtualBand(product, bandDescriptors.getMember("Stokes_4"), "BT_Value_XY_Imag");
            }
        }
    }

    private void addRotatedDualPoleBands(Product product, Family<BandDescriptor> bandDescriptors, HashMap<String, AbstractValueProvider> valueProviderMap) {
        if (containsAllRotationBands(product)) {
            DP valueProvider;
            BandDescriptor descriptor;

            if (containsBT_XY_Bands(product)) {
                valueProvider = new DPH(product, valueProviderMap, false);
                descriptor = bandDescriptors.getMember("BT_Value_H");
                addRotatedBand(product, descriptor, valueProvider);

                valueProvider = new DPV(product, valueProviderMap, false);
                descriptor = bandDescriptors.getMember("BT_Value_V");
                addRotatedBand(product, descriptor, valueProvider);

                ProductHelper.addVirtualBand(product, bandDescriptors.getMember("Stokes_1"), "(BT_Value_X + BT_Value_Y) / 2.0");
                ProductHelper.addVirtualBand(product, bandDescriptors.getMember("Stokes_2"), "(BT_Value_H - BT_Value_V) / 2.0");
            }

            if (containsAccuracy_XY_Bands(product)) {
                valueProvider = new DPH(product, valueProviderMap, true);
                descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_H");
                addRotatedBand(product, descriptor, valueProvider);

                valueProvider = new DPV(product, valueProviderMap, true);
                descriptor = bandDescriptors.getMember("Pixel_Radiometric_Accuracy_V");
                addRotatedBand(product, descriptor, valueProvider);
            }
        }
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
