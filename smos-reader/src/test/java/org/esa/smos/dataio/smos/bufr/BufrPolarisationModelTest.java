package org.esa.smos.dataio.smos.bufr;

import org.esa.smos.dataio.smos.PolarisationModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BufrPolarisationModelTest {

    private BufrPolarisationModel polarisationModel;

    @Before
    public void setUp() {
        polarisationModel = new BufrPolarisationModel();
    }

    @Test
    public void testInterfaceImplemented() {
        //noinspection ConstantConditions
        assertTrue(polarisationModel instanceof PolarisationModel);
    }

    @Test
    public void testGetPolarisationMode() {
        assertEquals(7, polarisationModel.getPolarisationMode(7));
        assertEquals(0, polarisationModel.getPolarisationMode(0));
        assertEquals(1, polarisationModel.getPolarisationMode(1));
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
