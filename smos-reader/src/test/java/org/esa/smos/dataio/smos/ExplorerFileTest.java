package org.esa.smos.dataio.smos;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExplorerFileTest {

    @Test
    public void testGetAncilliaryBandRole() {
         assertEquals("standard_deviation", ExplorerFile.getAncilliaryBandRole("Soil_Moisture_DQX"));
         assertEquals("uncertainty", ExplorerFile.getAncilliaryBandRole("Pixel_Radiometric_Accuracy_XY"));
         assertEquals("error", ExplorerFile.getAncilliaryBandRole("none_of_the_above"));
    }
}
