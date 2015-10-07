package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BrowseProductSupportTest {

    private BrowseProductSupport support;
    private NetcdfFile netcdfFile;

    @Before
    public void setUp() throws IOException {
        netcdfFile = mock(NetcdfFile.class);
        support = new BrowseProductSupport(netcdfFile);
    }

    @Test
    public void testGetLatitudeBandName() {
        assertEquals("Grid_Point_Latitude", support.getLatitudeBandName());
    }

    @Test
    public void testGetLongitudeBandName() {
        assertEquals("Grid_Point_Longitude", support.getLongitudeBandName());
    }

    @Test
    public void testSetScaleAndOffset_withoutRadimetricAccuracyScale() {
        final Band radAccuracy = new Band("Pixel_Radiometric_Accuracy_X", ProductData.TYPE_FLOAT32, 2, 2);
        final double scaling = 3.2;
        final double offset = -0.68;
        final BandDescriptor descriptor = createBandDescriptor("Pixel_Radiometric_Accuracy_X", scaling, offset);

        support.setScalingAndOffset(radAccuracy, descriptor);

        assertEquals(scaling, radAccuracy.getScalingFactor(), 1e-8);
        assertEquals(offset, radAccuracy.getScalingOffset(), 1e-8);
    }

    @Test
    public void testSetScaleAndOffset_withRadimetricAccuracyScale() throws IOException {
        final Band radAccuracy = new Band("Pixel_Radiometric_Accuracy_X", ProductData.TYPE_FLOAT32, 2, 2);
        final double scaling = 6.4;
        final double offset = -1.36;
        final BandDescriptor descriptor = createBandDescriptor("Pixel_Radiometric_Accuracy_X", scaling, offset);

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getStringValue()).thenReturn("010");
        when(netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Radiometric_Accuracy_Scale")).thenReturn(attribute);

        // need to trigger attribute reading in constructor tb 2015-06-29
        support = new BrowseProductSupport(netcdfFile);

        support.setScalingAndOffset(radAccuracy, descriptor);

        assertEquals(10.0 * scaling, radAccuracy.getScalingFactor(), 1e-8);
        assertEquals(offset, radAccuracy.getScalingOffset(), 1e-8);
    }

    @Test
    public void testSetScaleAndOffset_withoutFoorprintAxisScale() {
        final Band footprintAxis = new Band("Footprint_Axis2_X", ProductData.TYPE_FLOAT32, 2, 2);
        final double scaling = 9.6;
        final double offset = -2.37;
        final BandDescriptor descriptor = createBandDescriptor("Footprint_Axis2_X", scaling, offset);

        support.setScalingAndOffset(footprintAxis, descriptor);

        assertEquals(scaling, footprintAxis.getScalingFactor(), 1e-8);
        assertEquals(offset, footprintAxis.getScalingOffset(), 1e-8);
    }

    @Test
    public void testSetScaleAndOffset_withFootprintAxisScale() throws IOException {
        final Band footprintAxis = new Band("Footprint_Axis2_Y", ProductData.TYPE_FLOAT32, 2, 2);
        final double scaling = 11.45;
        final double offset = -2.008;
        final BandDescriptor descriptor = createBandDescriptor("Footprint_Axis2_Y", scaling, offset);

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getStringValue()).thenReturn("100");
        when(netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Pixel_Footprint_Scale")).thenReturn(attribute);

        // need to trigger attribute reading in constructor tb 2015-06-29
        support = new BrowseProductSupport(netcdfFile);

        support.setScalingAndOffset(footprintAxis, descriptor);

        assertEquals(100.0 * scaling, footprintAxis.getScalingFactor(), 1e-8);
        assertEquals(offset, footprintAxis.getScalingOffset(), 1e-8);
    }

    @Test
    public void testCanSupplyGridPointBTData() {
         assertTrue(support.canSupplyGridPointBtData());
    }

    @Test
    public void testGetBtData() throws IOException {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.LONG);
        final Dimension dimension = mock(Dimension.class);
        when(dimension.getLength()).thenReturn(4);
        final Attribute schemaAttribute = mock(Attribute.class);
        when(schemaAttribute.getStringValue()).thenReturn("DBL_SM_XXXX_MIR_BWSF1C_0400.binXschema.xml");
        when(netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Main_Info:Datablock_Schema")).thenReturn(schemaAttribute);
        when(netcdfFile.findVariable(null, "Footprint_Axis1")).thenReturn(variable);
        when(netcdfFile.findVariable(null, "Footprint_Axis2")).thenReturn(variable);
        when(netcdfFile.findVariable(null, "Flags")).thenReturn(variable);
        when(netcdfFile.findVariable(null, "BT_Value")).thenReturn(variable);
        when(netcdfFile.findVariable(null, "Azimuth_Angle")).thenReturn(variable);
        when(netcdfFile.findVariable(null, "Radiometric_Accuracy_of_Pixel")).thenReturn(variable);
        when(netcdfFile.findDimension("n_bt_data")).thenReturn(dimension);

        final GridPointBtDataset btData = support.getBtData(12);
        assertNotNull(btData);

        assertEquals(-1, btData.getIncidenceAngleBandIndex());
        assertEquals(1, btData.getRadiometricAccuracyBandIndex());
        assertEquals(-1, btData.getBTValueImaginaryBandIndex());
        assertEquals(-1, btData.getBTValueRealBandIndex());
    }

    private BandDescriptor createBandDescriptor(String memberName, double scaling, double offset) {
        final BandDescriptor descriptor = mock(BandDescriptor.class);
        when(descriptor.getScalingFactor()).thenReturn(scaling);
        when(descriptor.getScalingOffset()).thenReturn(offset);
        when(descriptor.getMemberName()).thenReturn(memberName);
        return descriptor;
    }
}
