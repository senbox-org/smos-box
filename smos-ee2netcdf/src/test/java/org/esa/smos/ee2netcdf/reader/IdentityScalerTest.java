package org.esa.smos.ee2netcdf.reader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IdentityScalerTest {

    @Test
    public void testScale() {
        final IdentityScaler scale = new IdentityScaler();

        assertEquals(12.6, scale.scale(12.6), 1e-8);
        assertEquals(-8.1, scale.scale(-8.1), 1e-8);
    }
}
