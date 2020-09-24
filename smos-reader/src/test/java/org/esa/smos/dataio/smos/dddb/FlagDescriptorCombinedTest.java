package org.esa.smos.dataio.smos.dddb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FlagDescriptorCombinedTest {

    @Test
    public void testConstruct() {
        final String[] tokens = new String[]{"X-pol", "00000003", "0"};

        final FlagDescriptorCombined desciptor = new FlagDescriptorCombined(tokens);
        assertEquals("X-pol", desciptor.getFlagName());
        assertEquals(3, desciptor.getMask());
        assertFalse(desciptor.isVisible());
        assertNull(desciptor.getColor());
        assertEquals("", desciptor.getCombinedDescriptor());
        assertEquals("", desciptor.getDescription());
    }

    @Test
    public void testEvaluate_polarisation() {
        String[] tokens = new String[]{"X-pol", "00000003", "0"};
        FlagDescriptorCombined desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(256));    // bit 0 not set, bit 1 not set
        assertFalse(desciptor.evaluate(257));   // bit 0 set, bit 1 not set

        tokens = new String[]{"Y-pol", "00000003", "1"};
        desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(513));    // bit 0 set, bit 1 not set
        assertFalse(desciptor.evaluate(520));   // bit 0 not set, bit 1 not set

        tokens = new String[]{"Re(XY-pol)", "00000003", "2"};
        desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(1026));    // bit 0 not set, bit 1 set
        assertFalse(desciptor.evaluate(2048));   // bit 0 not set, bit 1 not set

        tokens = new String[]{"Im(XY-pol)", "00000003", "3"};
        desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(1027));    // bit 0 set, bit 1 set
        assertFalse(desciptor.evaluate(2049));   // bit 0 not set, bit 1 set
    }

    @Test
    public void testEvaluate_rfi() {
        String[] tokens = new String[]{"RFI_FREE", "0000C000", "0"};
        FlagDescriptorCombined desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(256));    // bit 15 not set, bit 16 not set
        assertFalse(desciptor.evaluate(16385));   // bit 15 set, bit 16 not set

        tokens = new String[]{"RFI_LOW", "0000C000", "16384"};
        desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(16385));    // bit 15 set, bit 16 not set
        assertFalse(desciptor.evaluate(8192));   // bit 15 not set, bit 16 not set

        tokens = new String[]{"RFI_MEDIUM", "0000C000", "32768"};
        desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(32769));    // bit 15 not set, bit 16 set
        assertFalse(desciptor.evaluate(16385));   // bit 15 set, bit 16 not set

        tokens = new String[]{"RFI_HIGH", "0000C000", "49152"};
        desciptor = new FlagDescriptorCombined(tokens);
        assertTrue(desciptor.evaluate(49153));    // bit 15 set, bit 16 set
        assertFalse(desciptor.evaluate(16385));   // bit 15 set, bit 16 not set
    }
}
