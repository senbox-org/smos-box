package org.esa.smos.ee2netcdf.reader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinearScalerTest {

    @Test
    public void testScale() {
        LinearScaler scale = new LinearScaler(5.0, -0.8, -0.9);
        assertEquals(19.2, scale.scale(4.0), 1e-8);
        assertEquals(9.2, scale.scale(2.0), 1e-8);

        scale = new LinearScaler(-2.0, 1.2, -100.0);
        assertEquals(-6.8, scale.scale(4.0), 1e-8);
        assertEquals(-2.8, scale.scale(2.0), 1e-8);
    }

    @Test
    public void testIsValid() {
        final LinearScaler scaler = new LinearScaler(12, 13, 56.23);

        assertTrue(scaler.isValid(-16));
        assertTrue(scaler.isValid(56.22));
        assertTrue(scaler.isValid(56.24));

        assertFalse(scaler.isValid(56.23));
    }
}
