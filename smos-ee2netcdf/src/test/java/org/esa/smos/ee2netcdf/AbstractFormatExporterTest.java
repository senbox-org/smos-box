package org.esa.smos.ee2netcdf;


import org.esa.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;

import java.util.List;

import static org.junit.Assert.*;

public class AbstractFormatExporterTest {

    private MetadataElement metadataRoot;

    @Before
    public void setUp() {
        metadataRoot = new MetadataElement("root");
    }

    @Test
    public void testExtractMetadata_noMetadata() {
        final List<AttributeEntry> metaProperties = AbstractFormatExporter.extractMetadata(metadataRoot);

        assertNotNull(metaProperties);
        assertEquals(0, metaProperties.size());
    }

    @Test
    public void testExtractMetadata_simleMetadata() {
        metadataRoot.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        metadataRoot.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));

        final List<AttributeEntry> properties = AbstractFormatExporter.extractMetadata(metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("hoppla_1", properties.get(0).getValue());
    }

    @Test
    public void testSetDataType_noTypeNameSet() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();
        try {
            AbstractFormatExporter.setDataType(variableDescriptor, "");
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }

        try {
            AbstractFormatExporter.setDataType(variableDescriptor, null);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testSetDataType_unsupportedType() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();
        try {
            AbstractFormatExporter.setDataType(variableDescriptor, "ArrayOfWordDocuments");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSetDataType_Uint() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "uint");

        assertEquals(DataType.INT, variableDescriptor.getDataType());
        assertTrue(variableDescriptor.isUnsigned());
    }

    @Test
    public void testSetDataType_Int() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "int");

        assertEquals(DataType.INT, variableDescriptor.getDataType());
        assertFalse(variableDescriptor.isUnsigned());
    }

    @Test
    public void testSetDataType_Float() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "float");

        assertEquals(DataType.FLOAT, variableDescriptor.getDataType());
        assertFalse(variableDescriptor.isUnsigned());
    }

    @Test
    public void testSetDataType_UByte() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "ubyte");

        assertEquals(DataType.BYTE, variableDescriptor.getDataType());
        assertTrue(variableDescriptor.isUnsigned());
    }

    @Test
    public void testSetDataType_UShort() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "ushort");

        assertEquals(DataType.SHORT, variableDescriptor.getDataType());
        assertTrue(variableDescriptor.isUnsigned());
    }

    @Test
    public void testSetDataType_Short() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "short");

        assertEquals(DataType.SHORT, variableDescriptor.getDataType());
        assertFalse(variableDescriptor.isUnsigned());
    }

    @Test
    public void testSetDataType_ULong() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "ulong");

        assertEquals(DataType.LONG, variableDescriptor.getDataType());
        assertTrue(variableDescriptor.isUnsigned());
    }

    @Test
    public void testSetDataType_Double() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        AbstractFormatExporter.setDataType(variableDescriptor, "double");

        assertEquals(DataType.DOUBLE, variableDescriptor.getDataType());
        assertFalse(variableDescriptor.isUnsigned());
    }

    @Test
    public void testGetNumDimensions() {
        assertEquals(1, AbstractFormatExporter.getNumDimensions("onedimensional"));
        assertEquals(2, AbstractFormatExporter.getNumDimensions("two dims"));
    }

    @Test
    public void testGetNumDimensions_emptyArgument() {
        try {
            AbstractFormatExporter.getNumDimensions("");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            AbstractFormatExporter.getNumDimensions(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testMustExport_emptyArray() {
        final String[] subsetNames = new String[0];

        assertTrue(AbstractFormatExporter.mustExport("whatever", subsetNames));
        assertTrue(AbstractFormatExporter.mustExport("we_dont_care", subsetNames));
    }

    @Test
    public void testMustExport_nameContainedInSubsetNames() {
        final String[] subsetNames = new String[]{"to_subset", "this_one_too", "this_also"};

        assertTrue(AbstractFormatExporter.mustExport("this_one_too", subsetNames));
        assertTrue(AbstractFormatExporter.mustExport("THIS_also", subsetNames));
    }

    @Test
    public void testMustExport_nameNotContainedInSubsetNames() {
        final String[] subsetNames = new String[]{"to_subset", "this_one_too", "this_also"};

        assertFalse(AbstractFormatExporter.mustExport("BT_Value", subsetNames));
        assertFalse(AbstractFormatExporter.mustExport("I_WANT_THIS", subsetNames));
    }
}
