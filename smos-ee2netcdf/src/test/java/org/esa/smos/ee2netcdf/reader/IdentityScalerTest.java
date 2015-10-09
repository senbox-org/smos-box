package org.esa.smos.ee2netcdf.reader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IdentityScalerTest {

    @Test
    public void testScale() {
        final IdentityScaler scale = new IdentityScaler(1599.4);

        assertEquals(12.6, scale.scale(12.6), 1e-8);
        assertEquals(-8.1, scale.scale(-8.1), 1e-8);
    }

    @Test
    public void testIsValid() {
        final IdentityScaler scaler = new IdentityScaler(815.4);

        assertTrue(scaler.isValid(15.7));
        assertTrue(scaler.isValid(815.39));
        assertTrue(scaler.isValid(815.41));

        assertFalse(scaler.isValid(815.4));
    }
}
