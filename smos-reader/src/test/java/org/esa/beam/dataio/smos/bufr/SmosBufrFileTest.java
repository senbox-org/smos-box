/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.beam.dataio.smos.bufr;

import org.junit.Ignore;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.ma2.StructureMembers;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Trials for reading SMOS BUFR formatted files obtained from 'ideas-nas.eo.esa.int'.
 *
 * @author Ralf Quast
 */
@Ignore
public class SmosBufrFileTest {

    @Test
    public void testOpenAndIterateMessages() throws IOException {
        final String location = "/Users/ralf/Desktop/ideas-nas.eo.esa.int/miras_20131028_003256_20131028_020943_smos_20947_o_20131028_031005_l1c.bufr";

        try (final SmosBufrFile dataset = SmosBufrFile.open(location)) {
            for (int i = 1, messageCount = dataset.getMessageCount(); i < messageCount; i += 100) {
                final StructureDataIterator structureIterator = dataset.getStructureIterator(i);

                int count = 0;
                while (structureIterator.hasNext()) {
                    final StructureData structureData = structureIterator.next();
                    assertNotNull(structureData);

                    final List<StructureMembers.Member> members = structureData.getMembers();
                    assertEquals(33, members.size());

                    final Array gridPointCountData = structureData.getArray("Number_of_grid_points");
                    assertNotNull(gridPointCountData);

                    final Array gridPointIdentifierData = structureData.getArray("Grid_point_identifier");
                    assertNotNull(gridPointIdentifierData);
                    assertEquals(1, gridPointIdentifierData.getSize());

                    final Array snapshotIdentifierData = structureData.getArray("Snapshot_identifier");
                    assertNotNull(snapshotIdentifierData);
                    assertEquals(1, snapshotIdentifierData.getSize());

                    final Array brightnessTemperatureRealPartData = structureData.getArray(
                            "Brightness_temperature_real_part");
                    assertNotNull(brightnessTemperatureRealPartData);
                    assertEquals(1, brightnessTemperatureRealPartData.getSize());
                    count++;
                }

                System.out.println("i = " + i);
                System.out.println("count = " + count);
            }

            //dataset.writeCDL(System.out, false);
        }
    }

}
