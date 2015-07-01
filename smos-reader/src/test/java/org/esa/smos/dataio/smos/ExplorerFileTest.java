package org.esa.smos.dataio.smos;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExplorerFileTest {

    @Test
    public void testGetAncilliaryBandRole() {
         assertEquals("standard_deviation", ExplorerFile.getAncilliaryBandRelation("Soil_Moisture_DQX"));
         assertEquals("uncertainty", ExplorerFile.getAncilliaryBandRelation("Pixel_Radiometric_Accuracy_XY"));
         assertEquals("error", ExplorerFile.getAncilliaryBandRelation("none_of_the_above"));
    }
}
