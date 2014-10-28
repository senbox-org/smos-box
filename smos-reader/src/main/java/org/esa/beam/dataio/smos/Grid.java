package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelModel;
import org.esa.beam.binning.PlanetaryGrid;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class Grid {

    private final PlanetaryGrid grid;
    private final MultiLevelModel model;

    public Grid(PlanetaryGrid grid) {
        this.grid = grid;
        model = SmosDgg.getInstance().getMultiLevelImage().getModel();
    }

    public int getCellIndex(double lon, double lat) {
        return (int) grid.getBinIndex(lat, lon);
    }

    public int getCellIndex(int levelPixelX, int levelPixelY, int currentLevel) {
        final AffineTransform i2m = model.getImageToModelTransform(currentLevel);
        final Point2D point = new Point2D.Double();

        point.setLocation(levelPixelX, levelPixelY);
        i2m.transform(point, point);

        return (int) grid.getBinIndex(point.getY(), point.getX());
    }
}
