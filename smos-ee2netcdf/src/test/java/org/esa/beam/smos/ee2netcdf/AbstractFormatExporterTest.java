package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class AbstractFormatExporterTest {

    private MetadataElement metadataRoot;

    @Before
    public void setUp() {
        metadataRoot = new MetadataElement("root");
    }

    @Test
    public void testExtractMetadata_noMetadata() {
        final List<AbstractFormatExporter.AttributeEntry> metaProperties = AbstractFormatExporter.extractMetadata(
                metadataRoot);

        assertNotNull(metaProperties);
        assertEquals(0, metaProperties.size());
    }

    @Test
    public void testExtractMetadata_firstLevel() {
        metadataRoot.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        metadataRoot.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));

        final List<AbstractFormatExporter.AttributeEntry> properties = AbstractFormatExporter.extractMetadata(
                metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("hoppla_1", properties.get(0).getValue());
    }

    @Test
    public void testExtractMetadata_secondLevel() {
        final MetadataElement secondary = new MetadataElement("secondary");
        secondary.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        secondary.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));
        metadataRoot.addElement(secondary);

        final List<AbstractFormatExporter.AttributeEntry> properties = AbstractFormatExporter.extractMetadata(
                metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("hoppla_2", properties.get(1).getValue());
    }

    @Test
    public void testExtractMetadata_thirdLevel() {
        final MetadataElement secondary = new MetadataElement("secondary");
        final MetadataElement third = new MetadataElement("third");
        third.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        third.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));
        secondary.addElement(third);
        metadataRoot.addElement(secondary);

        final List<AbstractFormatExporter.AttributeEntry> properties = AbstractFormatExporter.extractMetadata(
                metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("hoppla_1", properties.get(0).getValue());
    }

    @Test
    public void testExtractMetadata_mixedLevel() {
        final MetadataElement secondary = new MetadataElement("secondary");
        final MetadataElement third = new MetadataElement("third");
        third.addAttribute(new MetadataAttribute("att_3_1", ProductData.ASCII.createInstance("yeah_3"), true));
        third.addAttribute(new MetadataAttribute("att_3_2", ProductData.ASCII.createInstance("yeah_4"), true));
        secondary.addElement(third);
        secondary.addAttribute(new MetadataAttribute("att_2", ProductData.ASCII.createInstance("yeah_5"), true));
        metadataRoot.addElement(secondary);
        metadataRoot.addAttribute(new MetadataAttribute("root_1", ProductData.ASCII.createInstance("yeah_6"), true));
        metadataRoot.addAttribute(new MetadataAttribute("root_2", ProductData.ASCII.createInstance("yeah_7"), true));

        final List<AbstractFormatExporter.AttributeEntry> properties = AbstractFormatExporter.extractMetadata(
                metadataRoot);
        assertEquals(5, properties.size());
        assertEquals("yeah_6", properties.get(0).getValue());
        assertEquals("yeah_7", properties.get(1).getValue());
        assertEquals("yeah_5", properties.get(2).getValue());
        assertEquals("yeah_3", properties.get(3).getValue());
        assertEquals("yeah_4", properties.get(4).getValue());
    }

    @Test
    public void testExtractMetadata_withDuplicateNamedElements() {
        final MetadataElement secondary = new MetadataElement("secondary");
        final MetadataElement third_1 = new MetadataElement("third");
        third_1.addAttribute(new MetadataAttribute("att_3_1", ProductData.ASCII.createInstance("Wilhelm"), true));
        final MetadataElement third_2 = new MetadataElement("third");
        third_2.addAttribute(new MetadataAttribute("att_3_1", ProductData.ASCII.createInstance("Busch"), true));
        secondary.addElement(third_1);
        secondary.addElement(third_2);
        metadataRoot.addElement(secondary);

        final List<AbstractFormatExporter.AttributeEntry> properties = AbstractFormatExporter.extractMetadata(
                metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("Wilhelm", properties.get(0).getValue());
        assertEquals("Busch", properties.get(1).getValue());
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
    public void testEnsureNetCDFName() {
         assertEquals("bla_bla_bla", AbstractFormatExporter.ensureNetCDFName("bla_bla_bla"));
         assertEquals("bla_bla_bla", AbstractFormatExporter.ensureNetCDFName("bla.bla.bla"));
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
