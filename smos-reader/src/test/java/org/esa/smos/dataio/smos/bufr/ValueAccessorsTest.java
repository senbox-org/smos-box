package org.esa.smos.dataio.smos.bufr;

import org.junit.Test;
import ucar.ma2.StructureData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ValueAccessorsTest {

    @Test
    public void testGet_intAccessor() {
        ValueAccessor valueAccessor = ValueAccessors.get(SmosBufrFile.AZIMUTH_ANGLE);
        assertTrue(valueAccessor instanceof ValueAccessors.IntValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.FARADAY_ROTATIONAL_ANGLE);
        assertTrue(valueAccessor instanceof ValueAccessors.IntValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.GEOMETRIC_ROTATIONAL_ANGLE);
        assertTrue(valueAccessor instanceof ValueAccessors.IntValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.INCIDENCE_ANGLE);
        assertTrue(valueAccessor instanceof ValueAccessors.IntValueAccessor);
    }

    @Test
    public void testGet_unsignedShortAccessor() {
        ValueAccessor valueAccessor = ValueAccessors.get(SmosBufrFile.BRIGHTNESS_TEMPERATURE_REAL_PART);
        assertTrue(valueAccessor instanceof ValueAccessors.UnsignedShortValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.BRIGHTNESS_TEMPERATURE_IMAGINARY_PART);
        assertTrue(valueAccessor instanceof ValueAccessors.UnsignedShortValueAccessor);
    }

    @Test
    public void testGet_shortAccessor() {
        ValueAccessor valueAccessor = ValueAccessors.get(SmosBufrFile.FOOTPRINT_AXIS_1);
        assertTrue(valueAccessor instanceof ValueAccessors.ShortValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.FOOTPRINT_AXIS_2);
        assertTrue(valueAccessor instanceof ValueAccessors.ShortValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.PIXEL_RADIOMETRIC_ACCURACY);
        assertTrue(valueAccessor instanceof ValueAccessors.ShortValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.WATER_FRACTION);
        assertTrue(valueAccessor instanceof ValueAccessors.ShortValueAccessor);

        valueAccessor = ValueAccessors.get(SmosBufrFile.SMOS_INFORMATION_FLAG);
        assertTrue(valueAccessor instanceof ValueAccessors.ShortValueAccessor);
    }

    @Test
    public void testGet_byteAccessor() {
        final ValueAccessor valueAccessor = ValueAccessors.get(SmosBufrFile.POLARISATION);
        assertTrue(valueAccessor instanceof ValueAccessors.ByteValueAccessor);
    }

    @Test
    public void testGet_unknownDatasetName() {
        try {
            ValueAccessors.get("really unknown dataset");
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testRead_intValueAccessor() {
        final StructureData structureData = mock(StructureData.class);
        when(structureData.getScalarInt("whatever")).thenReturn(19);
        when(structureData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER)).thenReturn(20);

        final ValueAccessors.IntValueAccessor intValueAccessor = new ValueAccessors.IntValueAccessor("whatever");
        intValueAccessor.read(structureData);

        assertEquals(19, intValueAccessor.getRawValue());
        assertEquals(20, intValueAccessor.getGridPointId());

        verify(structureData, times(1)).getScalarInt("whatever");
        verify(structureData, times(1)).getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        verifyNoMoreInteractions(structureData);
    }

    @Test
    public void testRead_unsignedShortValueAccessor() {
        final StructureData structureData = mock(StructureData.class);
        when(structureData.getScalarShort("member")).thenReturn((short) 21);
        when(structureData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER)).thenReturn(22);

        final ValueAccessors.UnsignedShortValueAccessor unsignedShortValueAccessor = new ValueAccessors.UnsignedShortValueAccessor("member");
        unsignedShortValueAccessor.read(structureData);

        assertEquals(21, unsignedShortValueAccessor.getRawValue());
        assertEquals(22, unsignedShortValueAccessor.getGridPointId());

        verify(structureData, times(1)).getScalarShort("member");
        verify(structureData, times(1)).getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        verifyNoMoreInteractions(structureData);
    }

    @Test
    public void testRead_shortValueAccessor() {
        final StructureData structureData = mock(StructureData.class);
        when(structureData.getScalarShort("theShort")).thenReturn((short) 23);
        when(structureData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER)).thenReturn(24);

        final ValueAccessors.ShortValueAccessor shortValueAccessor = new ValueAccessors.ShortValueAccessor("theShort");
        shortValueAccessor.read(structureData);

        assertEquals(23, shortValueAccessor.getRawValue());
        assertEquals(24, shortValueAccessor.getGridPointId());

        verify(structureData, times(1)).getScalarShort("theShort");
        verify(structureData, times(1)).getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        verifyNoMoreInteractions(structureData);
    }

    @Test
    public void testRead_byteValueAccessor() {
        final StructureData structureData = mock(StructureData.class);
        when(structureData.getScalarByte("theByte")).thenReturn((byte) 25);
        when(structureData.getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER)).thenReturn(26);

        final ValueAccessors.ByteValueAccessor byteValueAccessor = new ValueAccessors.ByteValueAccessor("theByte");
        byteValueAccessor.read(structureData);

        assertEquals(25, byteValueAccessor.getRawValue());
        assertEquals(26, byteValueAccessor.getGridPointId());

        verify(structureData, times(1)).getScalarByte("theByte");
        verify(structureData, times(1)).getScalarInt(SmosBufrFile.GRID_POINT_IDENTIFIER);
        verifyNoMoreInteractions(structureData);
    }
}
