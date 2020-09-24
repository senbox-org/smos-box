package org.esa.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DoubleStructMemberWriterTest {

    @Test
    public void testWrite() throws IOException {
        final int memberIndex = 3;
        final NVariable variable = mock(NVariable.class);
        final CompoundData gridPointData = mock(CompoundData.class);
        when(gridPointData.getDouble(memberIndex)).thenReturn(12.12);

        final DoubleStructMemberWriter writer = new DoubleStructMemberWriter(variable, memberIndex, 18, Double.NaN);

        writer.write(gridPointData, null, 9);

        assertEquals(Double.NaN, writer.array.getDouble(8), 1e-8);
        assertEquals(12.12, writer.array.getDouble(9), 1e-8);
        assertEquals(Double.NaN, writer.array.getDouble(10), 1e-8);
    }
}
