package org.esa.beam.dataio.smos;

import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.*;

public class DggUtilsTest {

    @Test
    public void testCreateTileRectangle() {
        Rectangle2D rectangle = DggUtils.createTileRectangle(0, 0);
        assertEquals(-180.0, rectangle.getX(), 0.0);
        assertEquals(77.51953125, rectangle.getY(), 0.0);
        assertEquals(11.25, rectangle.getWidth(), 0.0);
        assertEquals(11.07421875, rectangle.getHeight(), 0.0);

        rectangle = DggUtils.createTileRectangle(31, 0);
        assertEquals(168.75, rectangle.getX(), 0.0);
        assertEquals(77.51953125, rectangle.getY(), 0.0);
        assertEquals(11.25, rectangle.getWidth(), 0.0);
        assertEquals(11.07421875, rectangle.getHeight(), 0.0);

        rectangle = DggUtils.createTileRectangle(31, 15);
        assertEquals(168.75, rectangle.getX(), 0.0);
        assertEquals(-88.59375, rectangle.getY(), 0.0);
        assertEquals(11.25, rectangle.getWidth(), 0.0);
        assertEquals(11.07421875, rectangle.getHeight(), 0.0);
    }

    @Test
    public void testCreateGridPointRectangle() {
        final Rectangle2D rectangle = DggUtils.createGridPointRectangle(34, -10);
        assertEquals(33.98, rectangle.getX(), 0.0);
        assertEquals(-10.01, rectangle.getY(), 0.0);
        assertEquals(0.04, rectangle.getWidth(), 0.0);
        assertEquals(0.02, rectangle.getHeight(), 0.0);
    }

    @Test
    public void testCreateGridPointRectangle_lonCrossesAntiMeridian() {
        Rectangle2D rectangle = DggUtils.createGridPointRectangle(-181, 19);
        assertEquals(-180.0, rectangle.getX(), 0.0);
        assertEquals(18.99, rectangle.getY(), 0.0);
        assertEquals(0.04, rectangle.getWidth(), 0.0);
        assertEquals(0.02, rectangle.getHeight(), 0.0);

        rectangle = DggUtils.createGridPointRectangle(179.99, 20);
        assertEquals(179.93, rectangle.getX(), 0.0);
        assertEquals(19.99, rectangle.getY(), 0.0);
        assertEquals(0.04, rectangle.getWidth(), 0.0);
        assertEquals(0.02, rectangle.getHeight(), 0.0);
    }

    @Test
    public void testCreateGridPointRectangle_latBeyondPole() {
        Rectangle2D rectangle = DggUtils.createGridPointRectangle(35, -90.2);
        assertEquals(34.98, rectangle.getX(), 0.0);
        assertEquals(-90.0, rectangle.getY(), 0.0);
        assertEquals(0.04, rectangle.getWidth(), 0.0);
        assertEquals(0.02, rectangle.getHeight(), 0.0);

        rectangle = DggUtils.createGridPointRectangle(36, 90.0);
        assertEquals(35.98, rectangle.getX(), 0.0);
        assertEquals(89.97, rectangle.getY(), 0.0);
        assertEquals(0.04, rectangle.getWidth(), 0.0);
        assertEquals(0.02, rectangle.getHeight(), 0.0);
    }
}
