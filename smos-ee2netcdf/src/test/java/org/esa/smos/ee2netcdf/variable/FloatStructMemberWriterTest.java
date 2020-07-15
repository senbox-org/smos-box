package org.esa.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FloatStructMemberWriterTest {

    @Test
    public void testWrite() throws IOException {
        final int memberIndex = 4;
        final NVariable variable = mock(NVariable.class);
        final CompoundData gridPointData = mock(CompoundData.class);
        when(gridPointData.getFloat(memberIndex)).thenReturn(13.13f);

        final FloatStructMemberWriter writer = new FloatStructMemberWriter(variable, memberIndex, 19, Float.NaN);
        
        writer.write(gridPointData, null, 10);

        assertEquals(Float.NaN, writer.array.getFloat(9), 1e-8);
        assertEquals(13.13f, writer.array.getFloat(10), 1e-8);
        assertEquals(Float.NaN, writer.array.getFloat(11), 1e-8);
    }
}
