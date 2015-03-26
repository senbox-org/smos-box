package org.esa.beam.dataio.smos.bufr;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BufrSupportTest {

    @Test
    public void testIsIntegerBandIndex() {
         assertTrue(BufrSupport.isIntegerBandIndex(10));
         assertTrue(BufrSupport.isIntegerBandIndex(11));

         assertFalse(BufrSupport.isIntegerBandIndex(9));
         assertFalse(BufrSupport.isIntegerBandIndex(12));
    }
}
