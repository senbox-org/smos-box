package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.*;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import org.esa.smos.ee2netcdf.ExporterUtils;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.*;

abstract class AbstractProductTypeSupport implements ProductTypeSupport {

    protected final NetcdfFile netcdfFile;
    protected ArrayCache arrayCache;
    protected GridPointInfo gridPointInfo;
    protected HashMap<String, Integer> memberNamesMap;
    protected FlagDescriptor[] flagDescriptors;
    private HashMap<String, Scaler> scalerMap;
    protected String[] snapshotDataNames;
    private Class[] tableClasses;

    AbstractProductTypeSupport(NetcdfFile netcdfFile) {
        this.netcdfFile = netcdfFile;
    }

    @Override
    public void initialize(Family<BandDescriptor> bandDescriptors) {
        // nothing to do as default tb 2015-10-14
    }

    @Override
    public void setScalingAndOffset(Band band, BandDescriptor bandDescriptor) {
        band.setScalingOffset(bandDescriptor.getScalingOffset());
        band.setScalingFactor(bandDescriptor.getScalingFactor());
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
        ensureDataStructuresInitialized();

        final GridPointBtDataset gridPointBtDataset = new GridPointBtDataset(memberNamesMap, tableClasses);
        final Integer pixel_radiometric_accuracy = memberNamesMap.get("Radiometric_Accuracy_of_Pixel");
        if (pixel_radiometric_accuracy != null) {
            gridPointBtDataset.setRadiometricAccuracyBandIndex(pixel_radiometric_accuracy);
        }
        final Integer flagsBandIndex = memberNamesMap.get(SmosConstants.BT_FLAGS_NAME);
        if (flagsBandIndex != null) {
            gridPointBtDataset.setFlagBandIndex(flagsBandIndex);
            gridPointBtDataset.setPolarisationFlagBandIndex(flagsBandIndex);
        }
        final Integer bt_value_real = memberNamesMap.get("BT_Value_Real");
        if (bt_value_real != null) {
            gridPointBtDataset.setBTValueRealBandIndex(bt_value_real);
        }
        final Integer bt_value_imag = memberNamesMap.get("BT_Value_Imag");
        if (bt_value_imag != null) {
            gridPointBtDataset.setBTValueImaginaryBandIndex(bt_value_imag);
        }
        final Integer incidence_angle = memberNamesMap.get(SmosConstants.INCIDENCE_ANGLE);
        if (incidence_angle != null) {
            gridPointBtDataset.setIncidenceAngleBandIndex(incidence_angle);
        }

        final Array btDataCountArray = arrayCache.get("BT_Data_Counter");
        if (btDataCountArray != null) {
            final int numMeasurements = btDataCountArray.getInt(gridPointIndex);
            Number[][] tableData = new Number[numMeasurements][memberNamesMap.size()];

            if (gridPointIndex >= 0) {
                final Set<Map.Entry<String, Integer>> entries = memberNamesMap.entrySet();
                for (final Map.Entry<String, Integer> entry : entries) {
                    final Integer variablesIndex = entry.getValue();

                    final Array array = arrayCache.get(entry.getKey());
                    final Scaler scaler = scalerMap.get(entry.getKey());
                    final Index index = array.getIndex();
                    for (int i = 0; i < numMeasurements; i++) {
                        index.set(gridPointIndex, i);
                        final Number[] tableVector = tableData[i];

                        final double rawValue = array.getDouble(index);
                        if (scaler.isValid(rawValue)) {
                            tableVector[variablesIndex] = scaler.scale(rawValue);
                        } else {
                            tableVector[variablesIndex] = Double.NaN;
                        }
                    }
                }
            }
            gridPointBtDataset.setData(tableData);
        }

        return gridPointBtDataset;
    }

    @Override
    public String[] getRawDataTableNames() {
        try {
            ensureDataStructuresInitialized();
        } catch (IOException e) {
            // @todo 2 tb/tb ad logging here
            return new String[0];
        }

        final String[] names = new String[memberNamesMap.size()];
        final Set<Map.Entry<String, Integer>> entries = memberNamesMap.entrySet();
        for (final Map.Entry<String, Integer> entry : entries) {
            names[entry.getValue()] = entry.getKey();
        }
        return names;
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
    public SnapshotInfo getSnapshotInfo() throws IOException {
        return null;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) {
        return new Object[0][];
    }

    @Override
    public void createAdditionalBands(Product product, Area area, Family<BandDescriptor> bandDescriptors, String formatName, HashMap<String, AbstractValueProvider> valueProvierMap) {
        // nothing to do here, must override if something should be achieved tb 2015-07-01
    }

    @Override
    public void setArrayCache(ArrayCache arrayCache) {
        this.arrayCache = arrayCache;
    }

    @Override
    public void setGridPointInfo(GridPointInfo gridPointInfo) {
        this.gridPointInfo = gridPointInfo;
    }

    protected void ensureDataStructuresInitialized() throws IOException {
        if (memberNamesMap != null && tableClasses != null && scalerMap != null && snapshotDataNames != null) {
            return;
        }

        memberNamesMap = new HashMap<>();
        scalerMap = new HashMap<>();
        final ArrayList<Class> tableClassesList = new ArrayList<>();
        final ArrayList<String> snapshotBandNamesList = new ArrayList<>();
        final String schemaDescription = NetcdfProductReader.getSchemaDescription(netcdfFile);
        final Dddb dddb = Dddb.getInstance();
        final Family<BandDescriptor> bandDescriptors = dddb.getBandDescriptors(schemaDescription);
        if (bandDescriptors == null) {
            throw new IOException("Unsupported file schema: '" + schemaDescription + "`");
        }

        int index = 0;
        for (final BandDescriptor descriptor : bandDescriptors.asList()) {
            final String eeVariableName = dddb.getEEVariableName(descriptor.getMemberName(), schemaDescription);
            final String ncVariableName = ExporterUtils.ensureNetCDFName(eeVariableName);

            if (!descriptor.isGridPointData()) {
                final Variable variable = netcdfFile.findVariable(null, ncVariableName);
                if (variable != null) {
                    snapshotBandNamesList.add(descriptor.getMemberName());
                }
            }
            if (!descriptor.isVisible()) {
                continue;
            }

            final Variable variable = netcdfFile.findVariable(null, ncVariableName);
            if (variable == null) {
                continue;
            }
            if (memberNamesMap.containsKey(eeVariableName)) {
                continue;
            }

            final double scalingFactor = descriptor.getScalingFactor();
            final double scalingOffset = descriptor.getScalingOffset();
            final double fillValue = descriptor.getFillValue();
            if (scalingFactor != 1.0 || scalingOffset != 0.0) {
                scalerMap.put(eeVariableName, new LinearScaler(scalingFactor, scalingOffset, fillValue));
            } else {
                scalerMap.put(eeVariableName, new IdentityScaler(fillValue));
            }
            tableClassesList.add(variable.getDataType().getClassType());
            memberNamesMap.put(eeVariableName, index);
            ++index;
        }

        snapshotDataNames = snapshotBandNamesList.toArray(new String[snapshotBandNamesList.size()]);
        tableClasses = tableClassesList.toArray(new Class[tableClassesList.size()]);

        for (final BandDescriptor descriptor : bandDescriptors.asList()) {
            final Family<FlagDescriptor> bandFlagDescriptors = descriptor.getFlagDescriptors();
            if (bandFlagDescriptors != null) {
                final List<FlagDescriptor> flagDescriptorList = bandFlagDescriptors.asList();
                flagDescriptors = flagDescriptorList.toArray(new FlagDescriptor[flagDescriptorList.size()]);
                break;
            }
        }
    }
}
