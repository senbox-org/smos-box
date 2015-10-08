package org.esa.smos.ee2netcdf.reader;

import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void getGetSchemaDescription_missingResource() throws IOException {
        try {
            NetcdfProductReader.getSchemaDescription(netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}
