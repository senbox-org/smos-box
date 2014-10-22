package org.esa.beam.dataio.smos.bufr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScaleFactorTest {

    @Test
    public void testScale() {
        final ScaleFactor scaleFactor = new ScaleFactor(1.8, 0.65, 32767);

        assertEquals(0.65, scaleFactor.scale(0), 1e-8);
        assertEquals(2.45, scaleFactor.scale(1), 1e-8);
        assertEquals(-1.15, scaleFactor.scale(-1), 1e-8);
    }

    @Test
    public void testScale_missingValue() {
        final ScaleFactor scaleFactor = new ScaleFactor(1.9, 0.55, 32767);

        assertEquals(Double.NaN, scaleFactor.scale(32767), 1e-8);
    }
}
