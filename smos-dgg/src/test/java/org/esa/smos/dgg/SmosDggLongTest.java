/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.smos.dgg;

import com.bc.ceres.test.LongTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;


/**
 * Antonio Gutierrez (antonio.gutierrez@deimos.com.pt) wrote:
 * <p/>
 * The numbering in the SMOS DGG follows a combination of the first and second counters in the
 * "isea4h9_cell_in_column" file.
 * The second counter is the one which contemplates the 10 zones (each zone being two adjacent
 * triangles of the icosahedron). Each zone is composed by 262144 points, and can be identified
 * by the first 2 digits of the counter (i.e. 01000000000 -> zone 1, 10333333333 -> zone 10).
 * <p/>
 * The first and last point in the file do not belong to any of these 10 zones, but we incorporated
 * them into the first and last zones respectively, so they have one extra point in the end.
 * <p/>
 * The remaining digits of the second counter should indicate the number within the zone, but
 * the encoding used is based on quads, so we reduced it to a sequential numbering. In short,
 * the method we used is the following:
 * <p/>
 * <pre>
 * 1, 00000000000 -> 1
 * 2, 01000000000 -> 2
 * 3, 01000000001 -> 3
 * ...
 * 262144, 01333333332 -> 262144
 * 262145, 01333333333 -> 262145
 * 262146, 02000000000 -> 1000001
 * 262147, 02000000001 -> 1000002
 * ...
 * 524288, 02333333332 -> 1262143
 * 524289, 02333333333 -> 1262144
 * 524290, 03000000000 -> 2000001
 * 524291, 03000000001 -> 2000002
 * ...
 * 2359296, 09333333332 -> 8262143
 * 2359297, 09333333333 -> 8262144
 * 2359298, 10000000000 -> 9000001
 * 2359299, 10000000001 -> 9000002
 * ...
 * 2621441, 10333333333 ->9262144
 * 2621442, 11000000000 ->9262145
 * </pre>
 */
@RunWith(LongTestRunner.class)
public class SmosDggLongTest {

    @Test
    public void testImageToModelTransform() {
        final AffineTransform t = SmosDgg.getInstance().getMultiLevelImage().getModel().getImageToModelTransform(0);

        final Point2D i = new Point2D.Double(0.0, 0.0);
        final Point2D m = new Point2D.Double();
        t.transform(i, m);

        assertEquals(-180.0, m.getX(), 0.0);
        assertEquals(90.0 - 0.02197265625 * 64, m.getY(), 0.0);
    }

}
