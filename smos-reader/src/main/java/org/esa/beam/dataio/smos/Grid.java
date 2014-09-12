package org.esa.beam.dataio.smos;

import org.esa.beam.binning.PlanetaryGrid;

public class Grid {

    private final PlanetaryGrid grid;

    public Grid(PlanetaryGrid grid) {
        this.grid = grid;
    }

    public int getCellIndex(double lon, double lat) {
        return (int) grid.getBinIndex(lat, lon);
    }
}
