package ucar.nc2.iosp.smos.bufr;

import org.junit.Test;

import static org.junit.Assert.*;

public class BufrNumbersTest {

    @Test
    public void testIsMissing() {
        assertTrue(BufrNumbers.isMissing(15L, 4));
        assertFalse(BufrNumbers.isMissing(168L, 8));
    }

    @Test
    public void testInt2() {
        assertEquals(2, BufrNumbers.int2(0, 2));
        assertEquals(768, BufrNumbers.int2(3, 0));
        assertEquals(1025, BufrNumbers.int2(4, 1));

        assertEquals(BufrNumbers.UNDEFINED, BufrNumbers.int2(255, 255));
    }
}
