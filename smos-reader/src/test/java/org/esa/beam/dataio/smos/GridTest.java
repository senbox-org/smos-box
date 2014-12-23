package org.esa.beam.dataio.smos;

import org.esa.beam.binning.support.ReducedGaussianGrid;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;

public class GridTest {

    private Grid grid;

    @Before
    public void setUp() {
        grid = new Grid(new ReducedGaussianGrid(512));
    }

    @Test
    public void testGetCellIndex_fromGeoLocation() {
        assertEquals(9, grid.getCellIndex(-180, 90));
        assertEquals(51989, grid.getCellIndex(179, 45));
        assertEquals(174264, grid.getCellIndex(0, 0));

        assertEquals(348519, grid.getCellIndex(-180, -90));
        assertEquals(296535, grid.getCellIndex(179, -45));
    }

    @Test
    public void testGetCellIndex_fromLevelledXandY() {
        assertEquals(95, grid.getCellIndex(0, 0, 0));
        assertEquals(185, grid.getCellIndex(30, 30, 0));
        assertEquals(302, grid.getCellIndex(30, 30, 1));
        assertEquals(594, grid.getCellIndex(30, 30, 2));
        assertEquals(1332, grid.getCellIndex(30, 30, 3));
    }

    @Test
    public void testGetGridRect() {
        Rectangle2D gridRect = grid.getGridRect(0.0, 0.0);
        assertEquals(-0.17578125, gridRect.getX(), 1e-8);
        assertEquals(-0.17578125, gridRect.getY(), 1e-8);
        assertEquals(0.3515625, gridRect.getWidth(), 1e-8);
        assertEquals(0.3515625, gridRect.getHeight(), 1e-8);

        gridRect = grid.getGridRect(-60.0, 0.0);
        assertEquals(-60.17578125, gridRect.getX(), 1e-8);
        assertEquals(-0.17578125, gridRect.getY(), 1e-8);
        assertEquals(0.3515625, gridRect.getWidth(), 1e-8);
        assertEquals(0.3515625, gridRect.getHeight(), 1e-8);

        gridRect = grid.getGridRect(0.0, -30.0);
        assertEquals(-0.1875, gridRect.getX(), 1e-8);
        assertEquals(-30.17578125, gridRect.getY(), 1e-8);
        assertEquals(0.375, gridRect.getWidth(), 1e-8);
        assertEquals(0.3515625, gridRect.getHeight(), 1e-8);
    }
}
