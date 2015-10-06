package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BrowseProductSupportTest {

    private BrowseProductSupport support;
    private NetcdfFile netcdfFile;

    @Before
    public void setUp() {
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
    public void testSetScaleAndOffset_withRadimetricAccuracyScale() {
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
    public void testSetScaleAndOffset_withFootprintAxisScale() {
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

    private BandDescriptor createBandDescriptor(String memberName, double scaling, double offset) {
        final BandDescriptor descriptor = mock(BandDescriptor.class);
        when(descriptor.getScalingFactor()).thenReturn(scaling);
        when(descriptor.getScalingOffset()).thenReturn(offset);
        when(descriptor.getMemberName()).thenReturn(memberName);
        return descriptor;
    }
}
