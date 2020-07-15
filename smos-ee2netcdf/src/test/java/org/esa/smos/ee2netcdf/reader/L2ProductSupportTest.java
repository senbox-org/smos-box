package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.*;
import java.awt.geom.Area;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class L2ProductSupportTest {

    private L2ProductSupport support;
    private NetcdfFile netcdfFile;

    @Before
    public void setUp() {
        netcdfFile = mock(NetcdfFile.class);
        support = new L2ProductSupport(netcdfFile);
    }

    @Test
    public void testGetLatitudeBandName() {
        assertEquals("Latitude", support.getLatitudeBandName());
    }

    @Test
    public void testGetLongitudeBandName() {
        assertEquals("Longitude", support.getLongitudeBandName());
    }

    @Test
    public void testCanOpenFile() {
        final Variable variable = mock(Variable.class);

        assertFalse(support.canOpenFile());

        when(netcdfFile.findVariable("Latitude")).thenReturn(variable);
        assertFalse(support.canOpenFile());

        when(netcdfFile.findVariable("Longitude")).thenReturn(variable);
        assertFalse(support.canOpenFile());

        when(netcdfFile.findVariable("Grid_Point_ID")).thenReturn(variable);
        assertTrue(support.canOpenFile());
    }

    @Test
    public void testSetScaleAndOffset_withoutChi2Factor() {
        final Band chi_2 = new Band("Chi_2", ProductData.TYPE_FLOAT32, 2, 2);
        final double scaling = 2.1;
        final double offset = -0.34;
        final BandDescriptor descriptor = createChi2Descriptor(scaling, offset);

        support.setScalingAndOffset(chi_2, descriptor);

        assertEquals(scaling, chi_2.getScalingFactor(), 1e-8);
        assertEquals(offset, chi_2.getScalingOffset(), 1e-8);
    }

    @Test
    public void testSetScaleAndOffset_withChi2Factor() {
        final Band chi_2 = new Band("Chi_2", ProductData.TYPE_FLOAT32, 2, 2);
        final double scaling = 1.2;
        final double offset = -0.68;
        final BandDescriptor descriptor = createChi2Descriptor(scaling, offset);

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getStringValue()).thenReturn("5.000000");
        when(netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Chi_2_Scale")).thenReturn(attribute);

        // need to trigger attribute reading in constructor tb 2015-06-29
        support = new L2ProductSupport(netcdfFile);

        support.setScalingAndOffset(chi_2, descriptor);

        assertEquals(scaling * 5.0, chi_2.getScalingFactor(), 1e-8);
        assertEquals(offset, chi_2.getScalingOffset(), 1e-8);
    }

    @Test
    public void testSetScaleAndOffset_otherVariable() {
        final Band band = new Band("Hasimaus", ProductData.TYPE_FLOAT32, 2, 2);
        final double scaling = 2.2;
        final double offset = -0.35;
        final BandDescriptor descriptor = createOtherDescriptor(scaling, offset);

        support.setScalingAndOffset(band, descriptor);

        assertEquals(scaling, band.getScalingFactor(), 1e-8);
        assertEquals(offset, band.getScalingOffset(), 1e-8);
    }

    @Test
    public void testCreateValueProvider() {
        final ArrayCache arrayCache = mock(ArrayCache.class);
        final BandDescriptor descriptor = mock(BandDescriptor.class);
        final GridPointInfo gridPointInfo = mock(GridPointInfo.class);

        final AbstractValueProvider valueProvider = support.createValueProvider(arrayCache, "whatever", descriptor, new Area(new Rectangle(0, 0, 12, 34)), gridPointInfo);
        assertNotNull(valueProvider);
        assertTrue(valueProvider instanceof VariableValueProvider);
    }

    private BandDescriptor createChi2Descriptor(double scaling, double offset) {
        final BandDescriptor descriptor = createDescriptorMock(scaling, offset);
        when(descriptor.getMemberName()).thenReturn("Chi_2");
        return descriptor;
    }

    @NotNull
    private BandDescriptor createDescriptorMock(double scaling, double offset) {
        final BandDescriptor descriptor = mock(BandDescriptor.class);
        when(descriptor.getScalingFactor()).thenReturn(scaling);
        when(descriptor.getScalingOffset()).thenReturn(offset);
        return descriptor;
    }

    private BandDescriptor createOtherDescriptor(double scaling, double offset) {
        final BandDescriptor descriptor = createDescriptorMock(scaling, offset);
        when(descriptor.getMemberName()).thenReturn("Hasimaus");
        return descriptor;
    }
}
