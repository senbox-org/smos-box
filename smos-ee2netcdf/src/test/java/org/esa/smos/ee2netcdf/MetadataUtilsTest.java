package org.esa.smos.ee2netcdf;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MetadataUtilsTest {

    private MetadataElement metadataRoot;
    private List<AttributeEntry> properties;

    @Before
    public void setUp() {
        metadataRoot = new MetadataElement("root");
        properties = new ArrayList<>();
    }

    @Test
    public void testExtractMetadata_noMetadata() {
        MetadataUtils.extractAttributes(metadataRoot, properties, "");

        assertEquals(0, properties.size());
    }

    @Test
    public void testExtractMetadata_simpleMetadata() {
        metadataRoot.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        metadataRoot.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));

        MetadataUtils.extractAttributes(metadataRoot, properties, "");
        assertEquals(2, properties.size());
        assertEquals("attribute_1", properties.get(0).getName());
        assertEquals("hoppla_1", properties.get(0).getValue());
    }

    @Test
    public void testExtractMetadata_simpleMetadata_withPrefix() {
        metadataRoot.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        metadataRoot.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));

        MetadataUtils.extractAttributes(metadataRoot, properties, "___");
        assertEquals(2, properties.size());
        assertEquals("___attribute_1", properties.get(0).getName());
        assertEquals("hoppla_1", properties.get(0).getValue());
    }

    @Test
    public void testExtractMetadata_secondLevel() {
        final MetadataElement secondary = new MetadataElement("secondary");
        secondary.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        secondary.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));
        metadataRoot.addElement(secondary);

        MetadataUtils.extractAttributes(metadataRoot, properties, "");
        assertEquals(2, properties.size());
        assertEquals("secondary:attribute_2", properties.get(1).getName());
        assertEquals("hoppla_2", properties.get(1).getValue());
    }

    @Test
    public void testExtractMetadata_secondLevel_withPrefix() {
        final MetadataElement secondary = new MetadataElement("secondary");
        secondary.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        secondary.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));
        metadataRoot.addElement(secondary);

        MetadataUtils.extractAttributes(metadataRoot, properties, "??");
        assertEquals(2, properties.size());
        assertEquals("??secondary:attribute_2", properties.get(1).getName());
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

        MetadataUtils.extractAttributes(metadataRoot, properties, "");
        assertEquals(2, properties.size());
        assertEquals("secondary:third:attribute_1", properties.get(0).getName());
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

        MetadataUtils.extractAttributes(metadataRoot, properties, "");
        assertEquals(5, properties.size());
        assertEquals("root_1", properties.get(0).getName());
        assertEquals("yeah_6", properties.get(0).getValue());
        assertEquals("root_2", properties.get(1).getName());
        assertEquals("yeah_7", properties.get(1).getValue());
        assertEquals("secondary:att_2", properties.get(2).getName());
        assertEquals("yeah_5", properties.get(2).getValue());
        assertEquals("secondary:third:att_3_1", properties.get(3).getName());
        assertEquals("yeah_3", properties.get(3).getValue());
        assertEquals("secondary:third:att_3_2", properties.get(4).getName());
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

        MetadataUtils.extractAttributes(metadataRoot, properties, "");
        assertEquals(2, properties.size());
        assertEquals("Wilhelm", properties.get(0).getValue());
        assertEquals("Busch", properties.get(1).getValue());
    }

    @Test
    public void testConvertNetcdfAttriutes_emptyList() {
        final List<Attribute> ncAttributes = new ArrayList<>();

        final List<AttributeEntry> convertedAttributes = MetadataUtils.convertNetcdfAttributes(ncAttributes, true);
        assertNotNull(convertedAttributes);
        assertEquals(0, convertedAttributes.size());
    }

    @Test
    public void testConvertNetcdfAttriutes() {
        final List<Attribute> ncAttributes = new ArrayList<>();
        final Attribute attribute_1 = mock(Attribute.class);
        when(attribute_1.getFullName()).thenReturn("attribute_1");
        when(attribute_1.getStringValue()).thenReturn("value 1");
        ncAttributes.add(attribute_1);

        final Attribute attribute_2 = mock(Attribute.class);
        when(attribute_2.getFullName()).thenReturn("attribute_2");
        when(attribute_2.getStringValue()).thenReturn("value 2");
        ncAttributes.add(attribute_2);

        final List<AttributeEntry> convertedAttributes = MetadataUtils.convertNetcdfAttributes(ncAttributes, false);
        assertNotNull(convertedAttributes);
        assertEquals(2, convertedAttributes.size());

        AttributeEntry attributeEntry = convertedAttributes.get(0);
        assertEquals("attribute_1", attributeEntry.getName());
        assertEquals("value 1", attributeEntry.getValue());

        attributeEntry = convertedAttributes.get(1);
        assertEquals("attribute_2", attributeEntry.getName());
        assertEquals("value 2", attributeEntry.getValue());
    }

    @Test
    public void testParseMetadata_emptyList() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();

        MetadataUtils.parseMetadata(metaDataElements, metadataRoot);

        assertEquals(0, metadataRoot.getNumAttributes());
        assertEquals(0, metadataRoot.getNumElements());
    }

    @Test
    public void testParseMetadata_singleTopLevelAttribute() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        final AttributeEntry entry = new AttributeEntry("top_level", "the value");
        metaDataElements.add(entry);

        MetadataUtils.parseMetadata(metaDataElements, metadataRoot);

        assertEquals(1, metadataRoot.getNumElements());
        final MetadataElement global_attributes = metadataRoot.getElement("Global_Attributes");
        final MetadataAttribute attribute = global_attributes.getAttribute("top_level");
        assertNotNull(attribute);
        assertEquals("the value", attribute.getData().getElemString());

    }

    @Test
    public void testParseMetadata_oneElementWithOneAttribute() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        final AttributeEntry entry = new AttributeEntry("an_element:second_level", "the value");
        metaDataElements.add(entry);

        MetadataUtils.parseMetadata(metaDataElements, metadataRoot);

        assertEquals(0, metadataRoot.getNumAttributes());

        assertEquals(1, metadataRoot.getNumElements());
        final MetadataElement element = metadataRoot.getElement("an_element");
        assertNotNull(element);
        final MetadataAttribute attribute = element.getAttribute("second_level");
        assertNotNull(attribute);
        assertEquals("the value", attribute.getData().getElemString());
    }

    @Test
    public void testParseMetadata_secondLevelElementWithTwoAttributes() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        AttributeEntry entry = new AttributeEntry("an_element:second_level:third_level_I", "the value");
        metaDataElements.add(entry);
        entry = new AttributeEntry("an_element:second_level:third_level_II", "second_value");
        metaDataElements.add(entry);

        MetadataUtils.parseMetadata(metaDataElements, metadataRoot);

        assertEquals(0, metadataRoot.getNumAttributes());

        assertEquals(1, metadataRoot.getNumElements());
        final MetadataElement element = metadataRoot.getElement("an_element");
        assertNotNull(element);
        final MetadataElement secondElement = element.getElement("second_level");
        assertNotNull(secondElement);
        final MetadataAttribute third_level_one = secondElement.getAttribute("third_level_I");
        assertNotNull(third_level_one);
        assertEquals("the value", third_level_one.getData().getElemString());
        final MetadataAttribute third_level_two = secondElement.getAttribute("third_level_II");
        assertNotNull(third_level_two);
        assertEquals("second_value", third_level_two.getData().getElemString());
    }

    @Test
    public void testParseMetadata_mixed() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        AttributeEntry entry = new AttributeEntry("an_attribute", "attribute_value");
        metaDataElements.add(entry);

        entry = new AttributeEntry("an_entry:entry_attribute", "e_a_value");
        metaDataElements.add(entry);

        entry = new AttributeEntry("an_entry:entry_element:e_e_attribute", "e_e_a_value");
        metaDataElements.add(entry);

        MetadataUtils.parseMetadata(metaDataElements, metadataRoot);

        assertEquals(2, metadataRoot.getNumElements());
        final MetadataElement global_attributes = metadataRoot.getElement("Global_Attributes");
        final MetadataAttribute attribute = global_attributes.getAttribute("an_attribute");
        assertNotNull(attribute);
        assertEquals("attribute_value", attribute.getData().getElemString());

        final MetadataElement element = metadataRoot.getElement("an_entry");
        assertNotNull(element);
        final MetadataAttribute entry_attribute = element.getAttribute("entry_attribute");
        assertNotNull(entry_attribute);
        assertEquals("e_a_value", entry_attribute.getData().getElemString());

        final MetadataElement nestedElement = element.getElement("entry_element");
        assertNotNull(nestedElement);
        final MetadataAttribute nestedAttribute = nestedElement.getAttribute("e_e_attribute");
        assertNotNull(nestedAttribute);
        assertEquals("e_e_a_value", nestedAttribute.getData().getElemString());
    }

    @Test
    public void testShrinkNames_emptList() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        MetadataUtils.shrinkNames(metaDataElements);

        assertEquals(0, metaDataElements.size());
    }

    @Test
    public void testShrinkNames_oneElement() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        final AttributeEntry entry = new AttributeEntry("Fixed_Header:File_Class", "whatever");
        metaDataElements.add(entry);

        MetadataUtils.shrinkNames(metaDataElements);

        assertEquals(1, metaDataElements.size());
        final AttributeEntry attributeEntry = metaDataElements.get(0);
        assertEquals("FH:File_Class", attributeEntry.getName());
    }

    @Test
    public void testShrinkNames_threeElements() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        AttributeEntry entry = new AttributeEntry("Variable_Header:Specific_Product_Header:Quality_Information:List_of_Retrieval_Schemes:Quality_Description_2:List_of_Quality_Classes:Quality_Record_16:SSS_Class", "whatever");
        metaDataElements.add(entry);
        entry = new AttributeEntry("Variable_Header:Specific_Product_Header:Quality_Information:List_of_Retrieval_Schemes:Quality_Description_1:Sea_Quality:Good_Quality", "whatever");
        metaDataElements.add(entry);
        entry = new AttributeEntry("Variable_Header:Specific_Product_Header:Quality_Information:List_of_Retrieval_Schemes:Quality_Description_1:List_of_Quality_Classes:Quality_Record_25:Poor_Quality_Retrieved_Average_Sigma", "whatever");
        metaDataElements.add(entry);

        MetadataUtils.shrinkNames(metaDataElements);

        assertEquals(3, metaDataElements.size());
        AttributeEntry attributeEntry = metaDataElements.get(0);
        assertEquals("VH:SPH:QI:LORS:Quality_Description_2:LOQC:Quality_Record_16:SSS_Class", attributeEntry.getName());

        attributeEntry = metaDataElements.get(1);
        assertEquals("VH:SPH:QI:LORS:Quality_Description_1:SQ:Good_Quality", attributeEntry.getName());

        attributeEntry = metaDataElements.get(2);
        assertEquals("VH:SPH:QI:LORS:Quality_Description_1:LOQC:Quality_Record_25:Poor_Quality_Retrieved_Average_Sigma", attributeEntry.getName());
    }

    @Test
    public void testExpandNames_emptList() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        MetadataUtils.expandNames(metaDataElements);

        assertEquals(0, metaDataElements.size());
    }

    @Test
    public void testExpandNames_oneElement() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        final AttributeEntry entry = new AttributeEntry("FH:File_Class", "whatever");
        metaDataElements.add(entry);

        MetadataUtils.expandNames(metaDataElements);

        assertEquals(1, metaDataElements.size());
        final AttributeEntry attributeEntry = metaDataElements.get(0);
        assertEquals("Fixed_Header:File_Class", attributeEntry.getName());
    }

    @Test
    public void testExpandNames_threeElements() {
        final List<AttributeEntry> metaDataElements = new ArrayList<>();
        AttributeEntry entry = new AttributeEntry("VH:SPH:QI:LORS:Quality_Description_1:LOQC:Quality_Record_15:Good_Quality", "whatever");
        metaDataElements.add(entry);
        entry = new AttributeEntry("VH:SPH:QI:LORS:Quality_Description_0:LOQC:Quality_Record_4:SST_Class", "whatever");
        metaDataElements.add(entry);
        entry = new AttributeEntry("VH:SPH:QI:LORS:Quality_Description_0:SIQ:Good_Quality_Failed_OOLUT", "whatever");
        metaDataElements.add(entry);

        MetadataUtils.shrinkNames(metaDataElements);

        assertEquals(3, metaDataElements.size());
        AttributeEntry attributeEntry = metaDataElements.get(0);
        assertEquals("Variable_Header:Specific_Product_Header:Quality_Information:List_of_Retrieval_Schemes:Quality_Description_1:List_of_Quality_Classes:Quality_Record_15:Good_Quality", attributeEntry.getName());

        attributeEntry = metaDataElements.get(1);
        assertEquals("Variable_Header:Specific_Product_Header:Quality_Information:List_of_Retrieval_Schemes:Quality_Description_0:List_of_Quality_Classes:Quality_Record_4:SST_Class", attributeEntry.getName());

        attributeEntry = metaDataElements.get(2);
        assertEquals("Variable_Header:Specific_Product_Header:Quality_Information:List_of_Retrieval_Schemes:Quality_Description_0:Sea_Ice_Quality:Good_Quality_Failed_OOLUT", attributeEntry.getName());
    }

    @Test
    public void testGetReplacement() {
        assertEquals("Fixed_Header", MetadataUtils.getReplacement("FH"));
        assertEquals("FH", MetadataUtils.getReplacement("Fixed_Header"));

        assertEquals("Variable_Header", MetadataUtils.getReplacement("VH"));
        assertEquals("VH", MetadataUtils.getReplacement("Variable_Header"));

        assertEquals("Main_Product_Header", MetadataUtils.getReplacement("MPH"));
        assertEquals("MPH", MetadataUtils.getReplacement("Main_Product_Header"));

        assertEquals("Orbit_Information", MetadataUtils.getReplacement("OI"));
        assertEquals("OI", MetadataUtils.getReplacement("Orbit_Information"));

        assertEquals("Specific_Product_Header", MetadataUtils.getReplacement("SPH"));
        assertEquals("SPH", MetadataUtils.getReplacement("Specific_Product_Header"));

        assertEquals("L2_Product_Desciption", MetadataUtils.getReplacement("L2PD"));
        assertEquals("L2PD", MetadataUtils.getReplacement("L2_Product_Desciption"));

        assertEquals("List_of_models", MetadataUtils.getReplacement("LOM"));
        assertEquals("LOM", MetadataUtils.getReplacement("List_of_models"));

        assertEquals("L2_Product_Location", MetadataUtils.getReplacement("L2PL"));
        assertEquals("L2PL", MetadataUtils.getReplacement("L2_Product_Location"));

        assertEquals("List_of_Data_Sets", MetadataUtils.getReplacement("LODS"));
        assertEquals("LODS", MetadataUtils.getReplacement("List_of_Data_Sets"));

        assertEquals("Main_Info", MetadataUtils.getReplacement("MI"));
        assertEquals("MI", MetadataUtils.getReplacement("Main_Info"));

        assertEquals("Time_Info", MetadataUtils.getReplacement("TI"));
        assertEquals("TI", MetadataUtils.getReplacement("Time_Info"));

        assertEquals("Quality_Information", MetadataUtils.getReplacement("QI"));
        assertEquals("QI", MetadataUtils.getReplacement("Quality_Information"));

        assertEquals("List_of_Retrieval_Schemes", MetadataUtils.getReplacement("LORS"));
        assertEquals("LORS", MetadataUtils.getReplacement("List_of_Retrieval_Schemes"));

        assertEquals("List_of_Quality_Classes", MetadataUtils.getReplacement("LOQC"));
        assertEquals("LOQC", MetadataUtils.getReplacement("List_of_Quality_Classes"));

        assertEquals("Near_Coast_Quality", MetadataUtils.getReplacement("NCQ"));
        assertEquals("NCQ", MetadataUtils.getReplacement("Near_Coast_Quality"));

        assertEquals("Sea_Ice_Quality", MetadataUtils.getReplacement("SIQ"));
        assertEquals("SIQ", MetadataUtils.getReplacement("Sea_Ice_Quality"));

        assertEquals("Sea_Quality", MetadataUtils.getReplacement("SQ"));
        assertEquals("SQ", MetadataUtils.getReplacement("Sea_Quality"));
    }

    @Test
    public void testHasShrinkedAttributes() {
        List<Attribute> metaDataElements = new ArrayList<>();
        Attribute entry = new Attribute("FH:File_Class", "whatever");
        metaDataElements.add(entry);

        assertTrue(MetadataUtils.hasShrinkedAttributes(metaDataElements));

        metaDataElements = new ArrayList<>();
        entry = new Attribute("Fixed_Header:File_Class", "whatever");
        metaDataElements.add(entry);

        assertFalse(MetadataUtils.hasShrinkedAttributes(metaDataElements));
    }
}
