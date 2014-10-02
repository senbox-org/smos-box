package org.esa.beam.dataio.smos;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DffgTest {

    @Test
    public void testSetRowGetIndex() {
        final Dffg dffg = createDffg();

        assertEquals(73, dffg.getIndex(46.0, 17.0));
        assertEquals(71, dffg.getIndex(40.0, 10.1));
        assertEquals(75,  dffg.getIndex(49.9, 19.9));
    }

    @Test
    public void testSetRowGetIndex_lonWrapover() {
        final Dffg dffg = createDffg();

        assertEquals(73, dffg.getIndex(-314.0, 17.0));
    }

    @Test
    public void testSetRowGetIndex_outOfGeoRange() {
        final Dffg dffg = createDffg();

        assertEquals(-1, dffg.getIndex(39.0, 17.0));
        assertEquals(-1, dffg.getIndex(51.0, 10.1));
        assertEquals(-1,  dffg.getIndex(42.9, 9.9));
        assertEquals(-1,  dffg.getIndex(42.9, 21.1));
    }

    private Dffg createDffg() {
        final Dffg dffg = new Dffg(10.0, 20.0, 40.0, 50.0, 2.0, null);

        dffg.setRow(0, 34, 1.1, 67);
        dffg.setRow(1, 35, 1.2, 68);
        dffg.setRow(2, 36, 1.3, 69);
        dffg.setRow(3, 37, 1.4, 70);
        dffg.setRow(4, 38, 1.5, 71);
        return dffg;
    }
}
