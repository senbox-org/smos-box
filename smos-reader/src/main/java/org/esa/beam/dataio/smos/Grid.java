package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelModel;
import org.esa.beam.binning.PlanetaryGrid;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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

    public Rectangle2D getGridRect(double lon, double lat) {
        final long binIndex = grid.getBinIndex(lat, lon);
        final int rowIndex = grid.getRowIndex(binIndex);
        final int numCols = grid.getNumCols(rowIndex);

        final double height = 180.0 / grid.getNumRows();
        final double width = 360.0 / numCols;

        return new Rectangle2D.Double(lon - 0.5 * width, lat - 0.5 * height, width, height);
    }
}
