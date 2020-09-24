package org.esa.smos.dataio.smos.dddb;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FlagDescriptorImplTest {

    @Test
    public void testConstructFromTokens() {
        final String[] tokens = new String[]{"true", "flagName", "1000", "2000", "combination", "description"};

        final FlagDescriptorImpl flagDescriptor = new FlagDescriptorImpl(tokens);
        assertTrue(flagDescriptor.isVisible());
        assertEquals("flagName", flagDescriptor.getFlagName());
        assertEquals(4096, flagDescriptor.getMask());
        assertEquals("java.awt.Color[r=0,g=32,b=0]", flagDescriptor.getColor().toString());
        assertEquals("combination", flagDescriptor.getCombinedDescriptor());
        assertEquals("description", flagDescriptor.getDescription());
    }

    @Test
    public void testConstructFromTokens_useDefaultValues() {
        final String[] tokens = new String[]{"*", "Merkel", "*", "*", "*", "*"};

        final FlagDescriptorImpl flagDescriptor = new FlagDescriptorImpl(tokens);
        assertFalse(flagDescriptor.isVisible());
        assertEquals("Merkel", flagDescriptor.getFlagName());
        assertEquals(0, flagDescriptor.getMask());
        assertNull(flagDescriptor.getColor());
        assertEquals("", flagDescriptor.getCombinedDescriptor());
        assertEquals("", flagDescriptor.getDescription());
    }

    @Test
    public void testEvaluate() {
        String[] tokens = new String[]{"true", "flagName", "00000001", "2000", "0.78", "description"};

        FlagDescriptorImpl flagDescriptor = new FlagDescriptorImpl(tokens);
        assertTrue(flagDescriptor.evaluate(17));
        assertFalse(flagDescriptor.evaluate(48));

        tokens = new String[]{"true", "flagName", "00000010", "2000", "0.78", "description"};

        flagDescriptor = new FlagDescriptorImpl(tokens);
        assertTrue(flagDescriptor.evaluate(17));
        assertFalse(flagDescriptor.evaluate(33));
    }
}
