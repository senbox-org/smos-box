package org.esa.smos.ee2netcdf.reader;

import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class NetcdfProductReaderTest {

    private NetcdfFile netcdfFile;

    @Before
    public void setUp() {
        netcdfFile = mock(NetcdfFile.class);
    }

    @Test
    public void getGetSchemaDescription() throws IOException {
        final Attribute schemaAttribute = mock(Attribute.class);
        when(schemaAttribute.getStringValue()).thenReturn("DBL_SM_XXXX_MIR_BWSF1C_0400.binXschema.xml");

        when(netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Main_Info:Datablock_Schema")).thenReturn(schemaAttribute);

        final String schemaDescription = NetcdfProductReader.getSchemaDescription(netcdfFile);
        assertEquals("DBL_SM_XXXX_MIR_BWSF1C_0400", schemaDescription);
    }

    @Test
    public void getGetSchemaDescription_missingResource() {
        try {
            NetcdfProductReader.getSchemaDescription(netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testCalculateMinMaxIfMissing_present() throws IOException {
        final Variable variable = mock(Variable.class);
        final BandDescriptor bandDescriptor = mock(BandDescriptor.class);

        when(bandDescriptor.hasTypicalMin()).thenReturn(true);
        when(bandDescriptor.hasTypicalMax()).thenReturn(true);

        NetcdfProductReader.calculateMinMaxIfMissing(variable, bandDescriptor);

        verify(bandDescriptor, times(1)).hasTypicalMin();
        verify(bandDescriptor, times(1)).hasTypicalMax();
        verifyNoMoreInteractions(bandDescriptor);
    }

    @Test
    public void testCalculateMinMaxIfMissing_bothMissing() throws IOException {
        final Array array = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{17, 15, 16, 13, 12, 17, 18, 16, 19});
        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(array);

        final BandDescriptor bandDescriptor = mock(BandDescriptor.class);
        when(bandDescriptor.hasTypicalMin()).thenReturn(false);
        when(bandDescriptor.hasTypicalMax()).thenReturn(false);
        when(bandDescriptor.hasFillValue()).thenReturn(false);

        NetcdfProductReader.calculateMinMaxIfMissing(variable, bandDescriptor);

        verify(bandDescriptor, times(1)).setTypicalMin(12.0);
        verify(bandDescriptor, times(1)).setTypicalMax(19.0);
        verify(bandDescriptor, times(1)).hasFillValue();
        verify(bandDescriptor, times(1)).hasTypicalMin();
        verify(bandDescriptor, times(1)).hasTypicalMax();
        verifyNoMoreInteractions(bandDescriptor);
    }

    @Test
    public void testCalculateMinMaxIfMissing_bothMissing_withFillValue() throws IOException {
        final Array array = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{17, 15, 16, -1, -1, 17, 18, 16, 19});
        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(array);

        final BandDescriptor bandDescriptor = mock(BandDescriptor.class);
        when(bandDescriptor.hasTypicalMin()).thenReturn(false);
        when(bandDescriptor.hasTypicalMax()).thenReturn(false);
        when(bandDescriptor.hasFillValue()).thenReturn(true);
        when(bandDescriptor.getFillValue()).thenReturn(-1.0);

        NetcdfProductReader.calculateMinMaxIfMissing(variable, bandDescriptor);

        verify(bandDescriptor, times(1)).setTypicalMin(15.0);
        verify(bandDescriptor, times(1)).setTypicalMax(19.0);
        verify(bandDescriptor, times(1)).hasFillValue();
        verify(bandDescriptor, times(1)).getFillValue();
        verify(bandDescriptor, times(1)).hasTypicalMin();
        verify(bandDescriptor, times(1)).hasTypicalMax();
        verifyNoMoreInteractions(bandDescriptor);
    }

    @Test
    public void testCalculateMinMaxIfMissing_minMissing() throws IOException {
        final Array array = Array.factory(DataType.FLOAT, new int[]{3, 3}, new float[]{1.8f, 3.4f, 2.6f, -1.45f, 1.88f, 1.93f, -0.223f, 0.65f, 0.f});
        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(array);

        final BandDescriptor bandDescriptor = mock(BandDescriptor.class);
        when(bandDescriptor.hasTypicalMin()).thenReturn(false);
        when(bandDescriptor.hasTypicalMax()).thenReturn(true);
        when(bandDescriptor.hasFillValue()).thenReturn(false);

        NetcdfProductReader.calculateMinMaxIfMissing(variable, bandDescriptor);

        verify(bandDescriptor, times(1)).setTypicalMin(-1.45f);
        verify(bandDescriptor, times(1)).hasFillValue();
        verify(bandDescriptor, times(1)).hasTypicalMin();
        verify(bandDescriptor, times(1)).hasTypicalMax();
        verifyNoMoreInteractions(bandDescriptor);
    }

    @Test
    public void testCalculateMinMaxIfMissing_maxMissing_withFillValue() throws IOException {
        final Array array = Array.factory(DataType.FLOAT, new int[]{6, 1}, new float[]{1.9f, 3.2f, -999.f, 3.1f, 1.88f, -999.f});
        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(array);

        final BandDescriptor bandDescriptor = mock(BandDescriptor.class);
        when(bandDescriptor.hasTypicalMin()).thenReturn(true);
        when(bandDescriptor.hasTypicalMax()).thenReturn(false);
        when(bandDescriptor.hasFillValue()).thenReturn(true);
        when(bandDescriptor.getFillValue()).thenReturn(-999.0);

        NetcdfProductReader.calculateMinMaxIfMissing(variable, bandDescriptor);

        verify(bandDescriptor, times(1)).setTypicalMax(3.2f);
        verify(bandDescriptor, times(1)).hasFillValue();
        verify(bandDescriptor, times(1)).getFillValue();
        verify(bandDescriptor, times(1)).hasTypicalMin();
        verify(bandDescriptor, times(1)).hasTypicalMax();
        verifyNoMoreInteractions(bandDescriptor);
    }
}
