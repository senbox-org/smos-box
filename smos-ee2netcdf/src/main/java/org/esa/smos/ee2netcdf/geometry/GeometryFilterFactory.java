package org.esa.smos.ee2netcdf.geometry;


import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

public class GeometryFilterFactory {

    public static GeometryFilter create(Geometry region) {
        if (region == null) {
            return new NoConstraintGeometryFilter();
        } else if (region instanceof Polygon) {
            return new PolygonGeometryFilter(region);
        }

        throw new IllegalArgumentException("Unsupported region geometry: " + region.toString());
    }
}
