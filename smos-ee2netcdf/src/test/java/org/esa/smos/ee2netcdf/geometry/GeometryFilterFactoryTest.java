package org.esa.smos.ee2netcdf.geometry;

import com.bc.ceres.binding.ConversionException;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GeometryFilterFactoryTest {

    private JtsGeometryConverter jtsGeometryConverter;

    @Before
    public void setUp() {
        jtsGeometryConverter = new JtsGeometryConverter();
    }

    @Test
    public void testCreate_noGeometrySupplied() {
        final GeometryFilter filter = GeometryFilterFactory.create(null);
        assertTrue(filter instanceof NoConstraintGeometryFilter);
    }

    @Test
    public void testCreate_withPolygon() throws ConversionException {
        final Geometry geometry = jtsGeometryConverter.parse("POLYGON((5 1, 5 2, 6 2, 6 1, 5 1))");

        final GeometryFilter filter = GeometryFilterFactory.create(geometry);
        assertTrue(filter instanceof PolygonGeometryFilter);
    }

    @Test
    public void testCreate_withUnsupportedGeometry() throws ConversionException {
        final Geometry geometry = jtsGeometryConverter.parse("POINT(5 1)");

        try {
            GeometryFilterFactory.create(geometry);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
