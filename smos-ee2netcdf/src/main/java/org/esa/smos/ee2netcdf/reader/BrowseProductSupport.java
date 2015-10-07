
package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.smos.ee2netcdf.ExporterUtils;
import org.esa.snap.framework.datamodel.Band;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class BrowseProductSupport extends AbstractProductTypeSupport {

    private final double radAccuracyScale;
    private final double footprintScale;
    private HashMap<String, Integer> memberNamesMap;
    private Class[] tableClasses;

    BrowseProductSupport(NetcdfFile netcdfFile) {
        super(netcdfFile);

        final Attribute radAccuracyAttribute = netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Radiometric_Accuracy_Scale");
        if (radAccuracyAttribute != null) {
            radAccuracyScale = Double.valueOf(radAccuracyAttribute.getStringValue());
        } else {
            radAccuracyScale = 1.0;
        }
        final Attribute footprintScaleAttribute = netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Pixel_Footprint_Scale");
        if (footprintScaleAttribute != null) {
            footprintScale = Double.valueOf(footprintScaleAttribute.getStringValue());
        } else {
            footprintScale = 1.0;
        }
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
    public void setScalingAndOffset(Band band, BandDescriptor bandDescriptor) {
        final String memberName = bandDescriptor.getMemberName();
        if (memberName.startsWith("Footprint_Axis")) {
            band.setScalingFactor(bandDescriptor.getScalingFactor() * footprintScale);
            band.setScalingOffset(bandDescriptor.getScalingOffset());
        } else if (memberName.startsWith("Pixel_Radiometric_Accuracy")) {
            band.setScalingFactor(bandDescriptor.getScalingFactor() * radAccuracyScale);
            band.setScalingOffset(bandDescriptor.getScalingOffset());
        } else {
            super.setScalingAndOffset(band, bandDescriptor);
        }
    }

    @Override
    public ValueProvider createValueProvider(ArrayCache arrayCache, String variableName, BandDescriptor descriptor, Area area, GridPointInfo gridPointInfo) {
        final int polarization = descriptor.getPolarization();
        if (polarization < 0) {
            return new VariableValueProvider(arrayCache, variableName, area, gridPointInfo);
        } else {
            return new BrowseValueProvider(arrayCache, variableName, polarization, area, gridPointInfo);
        }
    }

    @Override
    public boolean canSupplyGridPointBtData() {
        return true;
    }

    @Override
    public GridPointBtDataset getBtData(int gridPointIndex) throws IOException {
        ensureMemberNamesAndClasses();

        System.out.println("gridPointIndex = " + gridPointIndex);

        final GridPointBtDataset gridPointBtDataset = new GridPointBtDataset(memberNamesMap, tableClasses);
        final Integer pixel_radiometric_accuracy = memberNamesMap.get("Radiometric_Accuracy_of_Pixel");
        if (pixel_radiometric_accuracy != null) {
            gridPointBtDataset.setRadiometricAccuracyBandIndex(pixel_radiometric_accuracy);
        }

        final Dimension dimension = netcdfFile.findDimension("n_bt_data");

        final Number[][] tableData = new Number[dimension.getLength()][memberNamesMap.size()];

        // @todo 1 tb/tb continue here:
        // implement context class that allows accessing the value providers for the bands
        // ask the value providers about data
        // fill data into table data

        gridPointBtDataset.setData(tableData);

        return gridPointBtDataset;
    }

    @Override
    public String[] getRawDataTableNames()  {
        try {
            ensureMemberNamesAndClasses();
        } catch (IOException e) {
            // @todo 2 tb/tb ad logging here
            return new String[0];
        }
        return memberNamesMap.keySet().toArray(new String[memberNamesMap.size()]);
    }

    private void ensureMemberNamesAndClasses() throws IOException {
        if (memberNamesMap != null && tableClasses != null) {
            return;
        }

        memberNamesMap = new HashMap<>();
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
            tableClassesList.add(variable.getDataType().getClassType());
            memberNamesMap.put(eeVariableName, index);
            ++index;
        }

        tableClasses = tableClassesList.toArray(new Class[tableClassesList.size()]);
    }
}
