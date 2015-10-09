package org.esa.smos.ee2netcdf.reader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinearScalingTest {

    @Test
    public void testScale() {
        LinearScaler scale = new LinearScaler(5.0, -0.8);
        assertEquals(19.2, scale.scale(4.0), 1e-8) ;
        assertEquals(9.2, scale.scale(2.0), 1e-8) ;

        scale = new LinearScaler(-2.0, 1.2);
        assertEquals(-6.8, scale.scale(4.0), 1e-8) ;
        assertEquals(-2.8, scale.scale(2.0), 1e-8) ;
    }
}
