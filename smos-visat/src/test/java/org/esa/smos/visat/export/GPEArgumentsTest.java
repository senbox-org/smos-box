package org.esa.smos.visat.export;

import org.junit.Test;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import static org.junit.Assert.*;

public class GPEArgumentsTest {

    @Test
    public void testCreatePointShape() {
        final Shape pointShape = GPEArguments.createPointShape(45.0, 22.0);
        assertNotNull(pointShape);

        final Rectangle2D bounds = pointShape.getBounds2D();
        assertEquals(45.0, bounds.getX(), 1e-8);
        assertEquals(22.0, bounds.getY(), 1e-8);
        assertEquals(0.0, bounds.getWidth(), 1e-8);
        assertEquals(0.0, bounds.getHeight(), 1e-8);
    }

    @Test
    public void testCreateBoxedArea() {
        final Area boxedArea = GPEArguments.createBoxedArea(10.0, 20.0, 30.0, 35.0);
        assertNotNull(boxedArea);

        final Rectangle2D bounds = boxedArea.getBounds2D();
        assertEquals(10.0, bounds.getX(), 1e-8);
        assertEquals(30.0, bounds.getY(), 1e-8);
        assertEquals(10.0, bounds.getWidth(), 1e-8);
        assertEquals(5.0, bounds.getHeight(), 1e-8);
    }
}
