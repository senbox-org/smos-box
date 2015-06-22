package org.esa.smos.ee2netcdf;

import org.esa.snap.framework.datamodel.MetadataAttribute;
import org.esa.snap.framework.datamodel.MetadataElement;
import org.esa.snap.framework.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void testExtractMetadata_simleMetadata() {
        metadataRoot.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        metadataRoot.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));

        MetadataUtils.extractAttributes(metadataRoot, properties, "");
        assertEquals(2, properties.size());
        assertEquals("attribute_1", properties.get(0).getName());
        assertEquals("hoppla_1", properties.get(0).getValue());
    }

    @Test
    public void testExtractMetadata_simleMetadata_withPrefix() {
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

        final List<AttributeEntry> convertedAttributes = MetadataUtils.convertNetcdfAttributes(ncAttributes);
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

        final List<AttributeEntry> convertedAttributes = MetadataUtils.convertNetcdfAttributes(ncAttributes);
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

        assertEquals(1, metadataRoot.getNumAttributes());
        final MetadataAttribute attribute = metadataRoot.getAttribute("top_level");
        assertNotNull(attribute);
        assertEquals("the value", attribute.getData().getElemString());

        assertEquals(0, metadataRoot.getNumElements());
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

        assertEquals(1, metadataRoot.getNumAttributes());
        final MetadataAttribute attribute = metadataRoot.getAttribute("an_attribute");
        assertNotNull(attribute);
        assertEquals("attribute_value", attribute.getData().getElemString());

        assertEquals(1, metadataRoot.getNumElements());
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
}
