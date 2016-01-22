package org.esa.smos.ee2netcdf.reader;


import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class ArrayCacheTest {

    private ArrayCache arrayCache;
    private NetcdfFile netcdfFile;
    private Variable variable;

    @Before
    public void setUp() throws IOException {
        netcdfFile = mock(NetcdfFile.class);
        variable = mock(Variable.class);
        when(netcdfFile.findVariable(null, "a_variable")).thenReturn(variable);
        final Array array = mock(Array.class);
        when(variable.read()).thenReturn(array);

        arrayCache = new ArrayCache(netcdfFile);
    }

    @Test
    public void testRequestedArrayIsRead() throws IOException {
        final Array resultArray =  arrayCache.get("a_variable");
        assertNotNull(resultArray);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsTakenFromCacheWhenRequestedASecondTime() throws IOException {
        final Array resultArray =  arrayCache.get("a_variable");
        assertNotNull(resultArray);

        final Array secondResultArray =  arrayCache.get("a_variable");
        assertNotNull(secondResultArray);

        assertSame(resultArray, secondResultArray);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testArrayIsNullWhenVariableIsNotPresent() throws IOException {
        final Array resultArray =  arrayCache.get("not_present_variable");
        assertNull(resultArray);

        verify(netcdfFile, times(1)).findVariable(null, "not_present_variable");
        verifyNoMoreInteractions(netcdfFile, variable);
    }
}
