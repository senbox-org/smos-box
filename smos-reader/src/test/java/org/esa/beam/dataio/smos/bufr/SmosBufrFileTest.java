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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Trials for reading SMOS BUFR formatted files obtained from 'ideas-nas.eo.esa.int'.
 *
 * @author Ralf Quast
 */
public class SmosBufrFileTest {

    @Test
    public void testOpenAndIterateMessages() throws IOException, URISyntaxException {
        final URL resource = getClass().getResource(
                "W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028033037_20131028002942_20131028003302_bufr_v505.bin");
        final File file = new File(resource.toURI());

        try (final SmosBufrFile dataset = SmosBufrFile.open(file.getPath())) {
            for (int i = 1, messageCount = dataset.getMessageCount(); i < messageCount; i += 100) {
                final StructureDataIterator structureIterator = dataset.getStructureIterator(i);

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
                }
            }

            //dataset.writeCDL(System.out, false);
        }
    }

}
