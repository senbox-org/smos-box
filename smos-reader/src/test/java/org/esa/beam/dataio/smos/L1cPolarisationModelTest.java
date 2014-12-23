package org.esa.beam.dataio.smos;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class L1cPolarisationModelTest {

    private L1cPolarisationModel polarisationModel;

    @Before
    public void setUp() {
        polarisationModel = new L1cPolarisationModel();
    }

    @Test
    public void testInterfaceImplemented() {
        assertTrue(polarisationModel instanceof PolarisationModel);
    }

    @Test
    public void testGetPolarisationMode() {
        assertEquals(3, polarisationModel.getPolarisationMode(7));
        assertEquals(0, polarisationModel.getPolarisationMode(0));
        assertEquals(1, polarisationModel.getPolarisationMode(1));
        assertEquals(2, polarisationModel.getPolarisationMode(2));
        assertEquals(3, polarisationModel.getPolarisationMode(3));
        assertEquals(0, polarisationModel.getPolarisationMode(64));
    }

    @Test
    public void testIs_X_Polarised() {
         assertTrue(polarisationModel.is_X_Polarised(0));

         assertFalse(polarisationModel.is_X_Polarised(2));
         assertFalse(polarisationModel.is_X_Polarised(17));
    }

    @Test
    public void testIs_Y_Polarised() {
        assertTrue(polarisationModel.is_Y_Polarised(1));

        assertFalse(polarisationModel.is_Y_Polarised(0));
        assertFalse(polarisationModel.is_Y_Polarised(16));
    }

    @Test
    public void testIs_XY1_Polarised() {
        assertTrue(polarisationModel.is_XY1_Polarised(2));

        assertFalse(polarisationModel.is_XY1_Polarised(1));
        assertFalse(polarisationModel.is_XY1_Polarised(15));
    }

    @Test
    public void testIs_XY2_Polarised() {
        assertTrue(polarisationModel.is_XY2_Polarised(3));

        assertFalse(polarisationModel.is_XY2_Polarised(2));
        assertFalse(polarisationModel.is_XY2_Polarised(14));
    }
}
