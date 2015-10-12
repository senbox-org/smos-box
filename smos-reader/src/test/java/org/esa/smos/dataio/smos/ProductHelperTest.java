package org.esa.smos.dataio.smos;

import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ColorPaletteDef;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.ImageInfo;
import org.esa.snap.core.datamodel.Product;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProductHelperTest {

    public static final String VIRTUAL_BAND_NAME = "the_virtual_band";
    private Product product;
    private BandDescriptor bandDescriptor;

    @Before
    public void setUp() {
        product = new Product("bla", "test-Type", 2, 3);

        bandDescriptor = mock(BandDescriptor.class);
        when(bandDescriptor.getBandName()).thenReturn(VIRTUAL_BAND_NAME);
        when(bandDescriptor.hasTypicalMin()).thenReturn(true);
        when(bandDescriptor.getTypicalMin()).thenReturn(0.2);
        when(bandDescriptor.hasTypicalMax()).thenReturn(true);
        when(bandDescriptor.getTypicalMax()).thenReturn(1.2);
    }

    @Test
    public void testAddVirtualBand_bandDescriptionIsAdded() {
        when(bandDescriptor.getDescription()).thenReturn("the virtual description");

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertEquals("the virtual description", band.getDescription());
    }

    @Test
    public void testAddVirtualBand_unitIsAdded() {
        when(bandDescriptor.getUnit()).thenReturn("it's virtual");

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertEquals("it's virtual", band.getUnit());
    }

    @Test
    public void testAddVirtualBand_fillValueIsAdded() {
        when(bandDescriptor.getFillValue()).thenReturn(123.78);

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertEquals(123.78, band.getGeophysicalNoDataValue(), 1e-5);
    }

    @Test
    public void testAddVirtualBand_hasFillValueIsAdded() {
        when(bandDescriptor.hasFillValue()).thenReturn(true);

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertTrue(band.isNoDataValueUsed());
    }

    @Test
    public void testCreateImageInfo_SoilMoisture() {
        when(bandDescriptor.getBandName()).thenReturn("Soil_Moisture");

        final ImageInfo imageInfo = ProductHelper.createImageInfo(null, bandDescriptor);
        assertNotNull(imageInfo);

        final ColorPaletteDef colorPaletteDef = imageInfo.getColorPaletteDef();
        final ColorPaletteDef.Point[] points = colorPaletteDef.getPoints();
        assertEquals(5, points.length);

        assertEquals(new Color(255,136,0), points[0].getColor());
        assertEquals(0.0, points[0].getSample(), 1e-8);

        assertEquals(new Color(225,221,0), points[2].getColor());
        assertEquals(0.1, points[2].getSample(), 1e-8);
    }

    @Test
    public void testCreateImageInfo_TauNad() {
        when(bandDescriptor.getBandName()).thenReturn("Tau_Nad_FO");

        ImageInfo imageInfo = ProductHelper.createImageInfo(null, bandDescriptor);
        assertNotNull(imageInfo);
        assertTauNadColourPalette(imageInfo);

        when(bandDescriptor.getBandName()).thenReturn("Tau_Nad_FV");

        imageInfo = ProductHelper.createImageInfo(null, bandDescriptor);
        assertNotNull(imageInfo);
        assertTauNadColourPalette(imageInfo);
    }

    @Test
    public void testCreateProduct() {
        final File file = new File("nasenmann-file.zip");
        final String productType = "NAS_MAN";

        final Product product = ProductHelper.createProduct(file, productType);
        assertNotNull(product);
        assertEquals("nasenmann-file", product.getName());
        assertEquals(productType, product.getProductType());

        final Dimension rasterDimension = ProductHelper.getSceneRasterDimension();
        assertEquals(rasterDimension.width, product.getSceneRasterWidth());
        assertEquals(rasterDimension.height, product.getSceneRasterHeight());

        final File productFile = product.getFileLocation();
        assertEquals(file.getAbsolutePath(), productFile.getAbsolutePath());

        final Dimension preferredTileSize = product.getPreferredTileSize();
        assertNotNull(preferredTileSize);
        assertEquals(512, preferredTileSize.width);
        assertEquals(512, preferredTileSize.height);

        final GeoCoding geoCoding = product.getSceneGeoCoding();
        assertNotNull(geoCoding);
    }

    private void assertTauNadColourPalette(ImageInfo imageInfo) {
        final ColorPaletteDef colorPaletteDef = imageInfo.getColorPaletteDef();
        final ColorPaletteDef.Point[] points = colorPaletteDef.getPoints();
        assertEquals(3, points.length);

        assertEquals(new Color(213,255,135), points[0].getColor());
        assertEquals(0.0, points[0].getSample(), 1e-8);

        assertEquals(new Color(0,45,0), points[2].getColor());
        assertEquals(1.2, points[2].getSample(), 1e-8);
    }
}
