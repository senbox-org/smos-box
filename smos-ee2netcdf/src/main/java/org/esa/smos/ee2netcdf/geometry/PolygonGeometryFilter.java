package org.esa.smos.ee2netcdf.geometry;

import com.bc.ceres.binio.CompoundData;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.io.IOException;

class PolygonGeometryFilter implements GeometryFilter {

    private final Polygon polygon;
    private final GeometryFactory geometryFactory;

    PolygonGeometryFilter(Geometry geometry) {
        polygon = (Polygon) geometry;

        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    }

    @Override
    public boolean accept(CompoundData compoundData) throws IOException {
        final float lat = compoundData.getFloat(1);
        final float lon = compoundData.getFloat(2);

        final Coordinate coord = new Coordinate(lon, lat);
        final Point point = geometryFactory.createPoint(coord);

        return polygon.contains(point);
    }
}
