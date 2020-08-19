package org.esa.smos.gui.gridpoint;

import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dataio.smos.dddb.FlagDescriptorImpl;
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
        flagDescriptors[0] = new FlagDescriptorImpl(new String[]{"", "POL_FLAG_1", "00000001", "2", "*", ""});
        flagDescriptors[1] = new FlagDescriptorImpl(new String[]{"", "POL_FLAG_2", "00000002", "2", "*", ""});
        flagDescriptors[2] = new FlagDescriptorImpl(new String[]{"", "SUN_GLINT_FOV", "00000008", "2", "*", ""});
        flagDescriptors[3] = new FlagDescriptorImpl(new String[]{"", "MOON_FOV", "00000010", "2", "*", ""});
        flagDescriptors[4] = new FlagDescriptorImpl(new String[]{"", "AF_FOV", "00000400", "2", "*", ""});
        flagDescriptors[5] = new FlagDescriptorImpl(new String[]{"", "RFI_TAIL", "00000800", "2", "*", ""});
        flagDescriptors[6] = new FlagDescriptorImpl(new String[]{"", "BORDER_FOV", "00001000", "2", "*", ""});
        flagDescriptors[7] = new FlagDescriptorImpl(new String[]{"", "SUN_TAILS", "00002000", "2", "*", ""});
        flagDescriptors[8] = new FlagDescriptorImpl(new String[]{"", "RFI_POINT_SOURCE", "00000040", "2", "*", ""});
        flagDescriptors[9] = new FlagDescriptorImpl(new String[]{"", "RFI_FLAG_2", "00008000", "2", "*", ""});

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
