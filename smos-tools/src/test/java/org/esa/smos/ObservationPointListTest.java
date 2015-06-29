package org.esa.smos;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ObservationPointListTest {

    @Test
    public void testConstructAndGet() throws IOException {
        final Point[] points = new Point[3];
        points[0] = new Point(0, 11);
        points[1] = new Point(1, 12);
        points[2] = new Point(2, 13);

        final ObservationPointList list = new ObservationPointList(points);
        assertEquals(3, list.getElementCount());

        assertEquals(1, list.getLon(1), 1e-8);
        assertEquals(12, list.getLat(1), 1e-8);
    }
}
