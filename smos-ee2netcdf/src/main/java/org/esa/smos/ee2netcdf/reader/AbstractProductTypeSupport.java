package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.PolarisationModel;
import org.esa.smos.dataio.smos.SmosConstants;
import org.esa.smos.dataio.smos.SnapshotInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.ee2netcdf.ExporterUtils;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractProductTypeSupport implements ProductTypeSupport {

    protected final NetcdfFile netcdfFile;
    protected ArrayCache arrayCache;
    protected GridPointInfo gridPointInfo;
    protected HashMap<String, Integer> memberNamesMap;
    protected FlagDescriptor[] flagDescriptors;
    private HashMap<String, Scaler> scalerMap;
    private Class[] tableClasses;

    AbstractProductTypeSupport(NetcdfFile netcdfFile) {
        this.netcdfFile = netcdfFile;
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
        }

        final Array btDataCountArray = arrayCache.get("BT_Data_Counter");
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
                    final double rawValue = array.getDouble(index);
                    if (scaler.isValid(rawValue)) {
                        tableData[i][variablesIndex] = scaler.scale(rawValue);
                    } else {
                        tableData[i][variablesIndex] = Double.NaN;
                    }
                }
            }
        }


        gridPointBtDataset.setData(tableData);

        return gridPointBtDataset;
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
    public Object[][] getSnapshotData(int snapshotIndex) {
        return new Object[0][];
    }

    @Override
    public void createAdditionalBands(Product product, Family<BandDescriptor> bandDescriptors, String formatName) {
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
        if (memberNamesMap != null && tableClasses != null && scalerMap != null) {
            return;
        }

        memberNamesMap = new HashMap<>();
        scalerMap = new HashMap<>();
        final ArrayList<Class> tableClassesList = new ArrayList<>();
        final String schemaDescription = NetcdfProductReader.getSchemaDescription(netcdfFile);
        final Dddb dddb = Dddb.getInstance();
        final Family<BandDescriptor> bandDescriptors = dddb.getBandDescriptors(schemaDescription);
        if (bandDescriptors == null) {
            throw new IOException("Unsupported file schema: '" + schemaDescription + "`");
        }

        int index = 0;
        for (final BandDescriptor descriptor : bandDescriptors.asList()) {
            if (!descriptor.isVisible()) {
                continue;
            }

            final String eeVariableName = dddb.getEEVariableName(descriptor.getMemberName(), schemaDescription);
            final String ncVariableName = ExporterUtils.ensureNetCDFName(eeVariableName);
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
