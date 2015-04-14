package org.esa.smos.dataio.smos;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExplorerFileTest {

    @Test
    public void testGetAncilliaryBandRole() {
         assertEquals("standard_deviation", ExplorerFile.getAcilliaryBandRole("Soil_Moisture_DQX"));
         assertEquals("uncertainty", ExplorerFile.getAcilliaryBandRole("Pixel_Radiometric_Accuracy_XY"));
         assertEquals("error", ExplorerFile.getAcilliaryBandRole("none_of_the_above"));
    }
}
