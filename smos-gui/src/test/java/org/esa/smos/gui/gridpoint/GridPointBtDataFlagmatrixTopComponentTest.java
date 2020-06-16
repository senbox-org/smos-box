package org.esa.smos.gui.gridpoint;

import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GridPointBtDataFlagmatrixTopComponentTest {

    @Test
    public void testCalculateFlaggingData() {
        final Number[][] data = new Number[8][2];
        data[0][0] = 0;         // none
        data[1][0] = 2;         // POL_FLAG_2
        data[2][0] = 8;         // SUN_GLINT_FOV
        data[3][0] = 16;        // MOON_FOV
        data[4][0] = 1024;      // AF_FOV
        data[5][0] = 12288;     // BORDER_FOV & SUN_TAILS
        data[6][0] = 64;        // RFI_POINT_SOURCE
        data[7][0] = 49160;     // RFI_HIGH & SUN_GLINT_FOV

        final FlagDescriptor[] flagDescriptors = new FlagDescriptor[10];
        flagDescriptors[0] = mock(FlagDescriptor.class);
        when(flagDescriptors[0].getMask()).thenReturn(1);
        flagDescriptors[1] = mock(FlagDescriptor.class);
        when(flagDescriptors[1].getMask()).thenReturn(2);
        flagDescriptors[2] = mock(FlagDescriptor.class);
        when(flagDescriptors[2].getMask()).thenReturn(8);
        flagDescriptors[3] = mock(FlagDescriptor.class);
        when(flagDescriptors[3].getMask()).thenReturn(16);
        flagDescriptors[4] = mock(FlagDescriptor.class);
        when(flagDescriptors[4].getMask()).thenReturn(1024);
        flagDescriptors[5] = mock(FlagDescriptor.class);
        when(flagDescriptors[5].getMask()).thenReturn(2048);
        flagDescriptors[6] = mock(FlagDescriptor.class);
        when(flagDescriptors[6].getMask()).thenReturn(4096);
        flagDescriptors[7] = mock(FlagDescriptor.class);
        when(flagDescriptors[7].getMask()).thenReturn(8192);
        flagDescriptors[8] = mock(FlagDescriptor.class);
        when(flagDescriptors[8].getMask()).thenReturn(64);
        flagDescriptors[9] = mock(FlagDescriptor.class);
        when(flagDescriptors[9].getMask()).thenReturn(49152);

        final double[][] flagData = GridPointBtDataFlagmatrixTopComponent.calculateFlaggingData(0, data, flagDescriptors);
        assertEquals(3, flagData.length);
        assertEquals(80, flagData[0].length);
        assertEquals(80, flagData[1].length);
        assertEquals(80, flagData[2].length);

        assertEquals(1.0, flagData[0][0], 1e-8);
        assertEquals(2.0, flagData[0][1], 1e-8);
        assertEquals(3.0, flagData[0][2], 1e-8);

        assertEquals(0.0, flagData[1][0], 1e-8);
        assertEquals(1.0, flagData[1][10], 1e-8);
        assertEquals(2.0, flagData[1][20], 1e-8);
        assertEquals(5.0, flagData[1][40], 1e-8);
        assertEquals(7.0, flagData[1][60], 1e-8);
        assertEquals(8.0, flagData[1][70], 1e-8);
        assertEquals(9.0, flagData[1][79], 1e-8);

        assertEquals(0.0, flagData[2][0], 1e-8);
        assertEquals(2.0, flagData[2][9], 1e-8);
        assertEquals(3.0, flagData[2][18], 1e-8);
        assertEquals(3.0, flagData[2][23], 1e-8);
        assertEquals(1.0, flagData[2][27], 1e-8);
        assertEquals(2.0, flagData[2][36], 1e-8);
        assertEquals(1.0, flagData[2][53], 1e-8);
        assertEquals(2.0, flagData[2][61], 1e-8);
        assertEquals(3.0, flagData[2][70], 1e-8);
        assertEquals(1.0, flagData[2][79], 1e-8);
    }
}
