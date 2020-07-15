package org.esa.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ByteStructMemberWriterTest {

    @Test
    public void testWrite() throws IOException {
        final int memberIndex = 2;
        final NVariable variable = mock(NVariable.class);
        final CompoundData gridPointData = mock(CompoundData.class);
        when(gridPointData.getByte(memberIndex)).thenReturn((byte)11);

        final ByteStructMemberWriter writer = new ByteStructMemberWriter(variable, memberIndex, 17, (byte) -4);
        
        writer.write(gridPointData, null, 8);

        assertEquals((byte) -4, writer.array.getByte(7));
        assertEquals((byte) 11, writer.array.getByte(8));
        assertEquals((byte) -4, writer.array.getByte(9));
    }
}
