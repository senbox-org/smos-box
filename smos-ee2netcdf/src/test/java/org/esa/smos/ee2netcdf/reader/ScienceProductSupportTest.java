package org.esa.smos.ee2netcdf.reader;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScienceProductSupportTest {

    private ScienceProductSupport support;

    @Before
    public void setUp() {
         // no specific functionality concerning the NetCDF file tb 2015-06-29
        support = new ScienceProductSupport(null, "MIR_SCSD1C");
    }

    @Test
    public void testGetLatitudeBandName() {
        assertEquals("Grid_Point_Latitude", support.getLatitudeBandName());
    }

    @Test
    public void testGetLongitudeBandName() {
        assertEquals("Grid_Point_Longitude", support.getLongitudeBandName());
    }

    @Test
    public void testCanSupplyGridPointBtData() {
        assertTrue(support.canSupplyGridPointBtData());
    }

    @Test
    public void testCanSupplySnapshotData() {
        assertTrue(support.canSupplySnapshotData());
    }

    @Test
    public void testCanSupplyFullPolData() {
        final ScienceProductSupport dualPole = new ScienceProductSupport(null, "MIR_SCSD1C");
        assertFalse(dualPole.canSupplyFullPolData());

        final ScienceProductSupport fullPole = new ScienceProductSupport(null, "MIR_SCSF1C");
        assertTrue(fullPole.canSupplyFullPolData());
    }

    @Test
    public void testContainsRotatedBands() {
        final Product product = new Product("test", "test_type", 2, 2);
        assertFalse(ScienceProductSupport.containsAllRotationBands(product));

        product.addBand("Faraday_Rotation_Angle_X", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsAllRotationBands(product));

        product.addBand("Faraday_Rotation_Angle_Y", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsAllRotationBands(product));

        product.addBand("Geometric_Rotation_Angle_X", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsAllRotationBands(product));

        product.addBand("Geometric_Rotation_Angle_y", ProductData.TYPE_INT8);
        assertTrue(ScienceProductSupport.containsAllRotationBands(product));
    }

    @Test
    public void testContainsBT_XY_Bands() {
        final Product product = new Product("test", "test_type", 2, 2);
        assertFalse(ScienceProductSupport.containsBT_XY_Bands(product));

        product.addBand("BT_Value_X", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsBT_XY_Bands(product));

        product.addBand("BT_Value_Y", ProductData.TYPE_INT8);
        assertTrue(ScienceProductSupport.containsBT_XY_Bands(product));
    }

    @Test
    public void testContainsBT_XY_FP_Bands() {
        final Product product = new Product("test", "test_type", 2, 2);
        assertFalse(ScienceProductSupport.containsBT_XY_FP_Bands(product));

        product.addBand("BT_Value_X", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsBT_XY_FP_Bands(product));

        product.addBand("BT_Value_Y", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsBT_XY_FP_Bands(product));

        product.addBand("BT_Value_XY_REAL", ProductData.TYPE_INT8);
        assertTrue(ScienceProductSupport.containsBT_XY_FP_Bands(product));
    }

    @Test
    public void testContainsAccuracy_XY_Bands() {
        final Product product = new Product("test", "test_type", 2, 2);
        assertFalse(ScienceProductSupport.containsAccuracy_XY_Bands(product));

        product.addBand("Pixel_Radiometric_Accuracy_X", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsAccuracy_XY_Bands(product));

        product.addBand("Pixel_Radiometric_Accuracy_Y", ProductData.TYPE_INT8);
        assertTrue(ScienceProductSupport.containsAccuracy_XY_Bands(product));
    }

    @Test
    public void testContainsAccuracy_XY_FP_Bands() {
        final Product product = new Product("test", "test_type", 2, 2);
        assertFalse(ScienceProductSupport.containsAccuracy_XY_FP_Bands(product));

        product.addBand("Pixel_Radiometric_Accuracy_X", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsAccuracy_XY_FP_Bands(product));

        product.addBand("Pixel_Radiometric_Accuracy_Y", ProductData.TYPE_INT8);
        assertFalse(ScienceProductSupport.containsAccuracy_XY_FP_Bands(product));

        product.addBand("Pixel_Radiometric_Accuracy_XY", ProductData.TYPE_INT8);
        assertTrue(ScienceProductSupport.containsAccuracy_XY_FP_Bands(product));
    }

    // @todo 2 tb/tb do not forget to add a  test for band-scaling 2015-06-29
}
