package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.GridPointInfo;
import org.esa.smos.dataio.smos.SmosConstants;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.awt.Rectangle;
import java.awt.geom.Area;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ScienceValueProviderTest {


    @Test
    public void test_interpolatedValueUsesValidZeroValueAndBtDataCounter() throws Exception {
        final ArrayCache arrayCache = mock(ArrayCache.class);
        when(arrayCache.get("BT_Data_Counter")).thenReturn(Array.factory(DataType.INT, new int[]{1}, new int[]{2}));
        when(arrayCache.get("Flags")).thenReturn(Array.factory(DataType.INT, new int[]{1, 4}, new int[]{0, 0, 0, 0}));
        when(arrayCache.get(SmosConstants.INCIDENCE_ANGLE)).thenReturn(Array.factory(DataType.INT, new int[]{1, 4}, new int[]{40, 45, 50, 50}));
        when(arrayCache.get("Faraday_Rotation_Angle")).thenReturn(Array.factory(DataType.FLOAT, new int[]{1, 4}, new float[]{0.0f, 10.0f, 1000.0f, 1000.0f}));

        final BandDescriptor descriptor = mock(BandDescriptor.class);
        when(descriptor.getFillValue()).thenReturn(0.0);
        when(descriptor.getPolarization()).thenReturn(0);

        final ScienceValueProvider valueProvider = new ScienceValueProvider(arrayCache, "Faraday_Rotation_Angle", descriptor, new Area(new Rectangle(1, 1)), mock(GridPointInfo.class), 1.0);

        assertEquals(5.0f, valueProvider.getFloat(0), 1e-8f);
    }
}
