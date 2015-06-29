package org.esa.smos;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointTest {

    @Test
    public void testCreateAndGet() {
        final Point point = new Point(12.8, -19.5);

        assertEquals(12.8, point.getLon(), 1e-8);
        assertEquals(-19.5, point.getLat(), 1e-8);
    }
}
