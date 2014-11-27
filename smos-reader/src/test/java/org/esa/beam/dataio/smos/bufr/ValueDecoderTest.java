package org.esa.beam.dataio.smos.bufr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValueDecoderTest {

    @Test
    public void testDecode() {
        final ValueDecoder valueDecoder = new ValueDecoder(1.8, 0.65, 32767);

        assertEquals(0.65, valueDecoder.decode(0), 0.0);
        assertEquals(2.45, valueDecoder.decode(1), 0.0);
        assertEquals(-1.15, valueDecoder.decode(-1), 0.0);
    }

    @Test
    public void testDecode_missingValue() {
        final ValueDecoder valueDecoder = new ValueDecoder(1.9, 0.55, 32767);

        assertTrue(Double.isNaN(valueDecoder.decode(32767)));
    }

    @Test
    public void testIsValid() {
        final ValueDecoder valueDecoder = new ValueDecoder(2.0, 0.45, 32766);

        assertTrue(valueDecoder.isValid(109));
        assertFalse(valueDecoder.isValid(32766));
    }
}
