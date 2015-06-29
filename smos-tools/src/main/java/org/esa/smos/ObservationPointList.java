package org.esa.smos;

import java.io.IOException;

public class ObservationPointList implements PointList{

    private final Point[] points;

    public ObservationPointList(Point[] points) {
        this.points = points;
    }

    @Override
    public int getElementCount() {
        return points.length;
    }

    @Override
    public double getLon(int i) throws IOException {
        return points[i].getLon();
    }

    @Override
    public double getLat(int i) throws IOException {
        return points[i].getLat();
    }
}
