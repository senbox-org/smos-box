package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class VariableValueProviderTest {

    private ArrayCache arrayCache;
    private GridPointInfo gridPointInfo;
    private Area area;
    private VariableValueProvider valueProvider;

    @Before
    public void setUp() {
        arrayCache = mock(ArrayCache.class);
        gridPointInfo = mock(GridPointInfo.class);
        area = new Area(new Rectangle(0, 12, 300, 400));
        valueProvider = new VariableValueProvider(arrayCache, "whatever", area, gridPointInfo);
    }

    @Test
    public void testGetArea() {
        final Area testArea = valueProvider.getArea();

        assertEquals(area.getBounds().x, testArea.getBounds().x);
        assertEquals(area.getBounds().y, testArea.getBounds().y);
        assertEquals(area.getBounds().width, testArea.getBounds().width);
        assertEquals(area.getBounds().height, testArea.getBounds().height);
    }

    @Test
    public void testGetGridPointIndex() {
        when(gridPointInfo.getGridPointIndex(23)).thenReturn(198);

        final int gridPointIndex = valueProvider.getGridPointIndex(23);
        assertEquals(198, gridPointIndex);
    }

    @Test
    public void testGetByte() throws IOException {
        final Array data = Array.factory(DataType.BYTE, new int[]{2, 2}, new byte[]{1, 2, 3, 4});
        when(arrayCache.get(anyString())).thenReturn(data);

        final byte aByte = valueProvider.getByte(1);
        assertEquals(2, aByte);
    }

    @Test
    public void testGetShort() throws IOException {
        final Array data = Array.factory(DataType.SHORT, new int[]{2, 2}, new short[]{2, 3, 4, 5});
        when(arrayCache.get(anyString())).thenReturn(data);

        final short aShort = valueProvider.getShort(2);
        assertEquals(4, aShort);
    }

    @Test
    public void testGetInt() throws IOException {
        final Array data = Array.factory(DataType.INT, new int[]{2, 2}, new int[]{3, 4, 5, 6});
        when(arrayCache.get(anyString())).thenReturn(data);

        final int anInt = valueProvider.getInt(3);
        assertEquals(6, anInt);
    }

    @Test
    public void testGetFloat() throws IOException {
        final Array data = Array.factory(DataType.FLOAT, new int[]{2, 2}, new float[]{4.f, 5.f, 6.f, 7.f});
        when(arrayCache.get(anyString())).thenReturn(data);

        final float aFloat = valueProvider.getFloat(0);
        assertEquals(4.f, aFloat, 1e-8);
    }
}
