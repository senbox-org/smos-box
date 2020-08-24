package org.esa.smos.ee2netcdf;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.StringUtils;
import ucar.nc2.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MetadataUtils {

    private static final String GLOBAL_ATTRIBUTES = "Global_Attributes";
    private final static char SEPARATOR = ':';
    private static final HashMap<String, String> NAME_REPLACEMENTS = new HashMap<>();

    static {
        NAME_REPLACEMENTS.put("Fixed_Header", "FH");
        NAME_REPLACEMENTS.put("FH", "Fixed_Header");
        NAME_REPLACEMENTS.put("Variable_Header", "VH");
        NAME_REPLACEMENTS.put("VH", "Variable_Header");
        NAME_REPLACEMENTS.put("Main_Product_Header", "MPH");
        NAME_REPLACEMENTS.put("MPH", "Main_Product_Header");
        NAME_REPLACEMENTS.put("Orbit_Information", "OI");
        NAME_REPLACEMENTS.put("OI", "Orbit_Information");
        NAME_REPLACEMENTS.put("Specific_Product_Header", "SPH");
        NAME_REPLACEMENTS.put("SPH", "Specific_Product_Header");
        NAME_REPLACEMENTS.put("L2_Product_Desciption", "L2PD");   // that one typo comes from the ESA file spec .... tb 2020-08-21
        NAME_REPLACEMENTS.put("L2PD", "L2_Product_Desciption");
        NAME_REPLACEMENTS.put("List_of_models", "LOM");
        NAME_REPLACEMENTS.put("LOM", "List_of_models");
        NAME_REPLACEMENTS.put("L2_Product_Location", "L2PL");
        NAME_REPLACEMENTS.put("L2PL", "L2_Product_Location");
        NAME_REPLACEMENTS.put("List_of_Data_Sets", "LODS");
        NAME_REPLACEMENTS.put("LODS", "List_of_Data_Sets");
        NAME_REPLACEMENTS.put("Main_Info", "MI");
        NAME_REPLACEMENTS.put("MI", "Main_Info");
        NAME_REPLACEMENTS.put("Time_Info", "TI");
        NAME_REPLACEMENTS.put("TI", "Time_Info");
        NAME_REPLACEMENTS.put("Quality_Information", "QI");
        NAME_REPLACEMENTS.put("QI", "Quality_Information");
        NAME_REPLACEMENTS.put("List_of_Retrieval_Schemes", "LORS");
        NAME_REPLACEMENTS.put("LORS", "List_of_Retrieval_Schemes");
        NAME_REPLACEMENTS.put("List_of_Quality_Classes", "LOQC");
        NAME_REPLACEMENTS.put("LOQC", "List_of_Quality_Classes");
        NAME_REPLACEMENTS.put("Near_Coast_Quality", "NCQ");
        NAME_REPLACEMENTS.put("NCQ", "Near_Coast_Quality");
        NAME_REPLACEMENTS.put("Sea_Ice_Quality", "SIQ");
        NAME_REPLACEMENTS.put("SIQ", "Sea_Ice_Quality");
        NAME_REPLACEMENTS.put("Sea_Quality", "SQ");
        NAME_REPLACEMENTS.put("SQ", "Sea_Quality");
    }


    static void extractAttributes(MetadataElement root, List<AttributeEntry> properties, String prefix) {
        final MetadataAttribute[] attributes = root.getAttributes();
        for (MetadataAttribute attribute : attributes) {
            addAttributeTo(properties, prefix, attribute);
        }

        final MetadataElement[] elements = root.getElements();
        final HashMap<String, List<MetadataElement>> uniqueNamedElements = getListWithUniqueNamedElements(elements);

        final Set<String> nameSet = uniqueNamedElements.keySet();
        for (final String elementName : nameSet) {
            final List<MetadataElement> elementsWithSameName = uniqueNamedElements.get(elementName);
            if (elementsWithSameName.size() == 1) {
                final MetadataElement metadataElement = elementsWithSameName.get(0);
                final String nextRecursionPrefix = prefix + metadataElement.getName() + SEPARATOR;
                extractAttributes(metadataElement, properties, nextRecursionPrefix);
            } else {
                int index = 0;
                for (final MetadataElement metadataElement : elementsWithSameName) {
                    final String nextRecursionPrefix = prefix + metadataElement.getName() + "_" + Integer.toString(index) + SEPARATOR;
                    extractAttributes(metadataElement, properties, nextRecursionPrefix);
                    ++index;
                }
            }
        }
    }

    private static void addAttributeTo(List<AttributeEntry> properties, String prefix, MetadataAttribute attribute) {
        properties.add(new AttributeEntry(prefix + attribute.getName(), attribute.getData().getElemString()));
    }

    private static HashMap<String, List<MetadataElement>> getListWithUniqueNamedElements(MetadataElement[] elements) {
        final HashMap<String, List<MetadataElement>> uniqueNamedElements = new HashMap<>(elements.length);
        for (final MetadataElement element : elements) {
            final String elementName = element.getName();
            final List<MetadataElement> elementList = uniqueNamedElements.get(elementName);
            if (elementList == null) {
                final ArrayList<MetadataElement> uniqueNamedElementsList = new ArrayList<>();
                uniqueNamedElementsList.add(element);
                uniqueNamedElements.put(elementName, uniqueNamedElementsList);
            } else {
                elementList.add(element);
            }
        }
        return uniqueNamedElements;
    }

    public static List<AttributeEntry> convertNetcdfAttributes(List<Attribute> ncAttributes, boolean expandNames) {
        final ArrayList<AttributeEntry> attributeList = new ArrayList<>(ncAttributes.size());

        for (final Attribute attribute : ncAttributes) {
            String name = attribute.getFullName();
            if (expandNames) {
                final String replacedName = MetadataUtils.getReplacement(name);
                if (StringUtils.isNotNullAndNotEmpty(replacedName)) {
                    name = replacedName;
                }
            }
            final String value = attribute.getStringValue();
            final AttributeEntry attributeEntry = new AttributeEntry(name, value);
            attributeList.add(attributeEntry);
        }
        return attributeList;
    }

    public static void parseMetadata(List<AttributeEntry> metaDataElements, MetadataElement metadataRoot) {
        for (final AttributeEntry attribute : metaDataElements) {
            final String name = attribute.getName();
            final String value = attribute.getValue();

            final String[] tokens = StringUtils.split(name, new char[]{SEPARATOR}, true);
            if (tokens.length == 1) {
                final MetadataElement globalAttributesElement = ensureElement(metadataRoot, GLOBAL_ATTRIBUTES);
                final MetadataAttribute metadataAttribute = new MetadataAttribute(name, ProductData.createInstance(value), false);
                globalAttributesElement.addAttribute(metadataAttribute);
            } else {
                MetadataElement element = metadataRoot;
                final int elementCount = tokens.length - 1;
                for (int i = 0; i < elementCount; i++) {
                    element = ensureElement(element, tokens[i]);
                }

                final MetadataAttribute metadataAttribute = new MetadataAttribute(tokens[(elementCount)], ProductData.createInstance(value), false);
                element.addAttribute(metadataAttribute);
            }
        }
    }

    private static MetadataElement ensureElement(MetadataElement metadataElement, String token) {
        MetadataElement element = metadataElement.getElement(token);
        if (element == null) {
            element = new MetadataElement(token);
            metadataElement.addElement(element);
        }

        return element;
    }

    static void shrinkNames(List<AttributeEntry> metaDataElements) {
        shrinkOrExpandNames(metaDataElements);
    }

    static void expandNames(List<AttributeEntry> metaDataElements) {
        shrinkOrExpandNames(metaDataElements);
    }

    private static void shrinkOrExpandNames(List<AttributeEntry> metaDataElements) {
        final ArrayList<AttributeEntry> resultList = new ArrayList<>(metaDataElements.size());
        for (final AttributeEntry entry : metaDataElements) {
            final String name = entry.getName();

            final String replacedName = getReplacement(name);
            if (StringUtils.isNotNullAndNotEmpty(replacedName)) {
                resultList.add(new AttributeEntry(replacedName, entry.getValue()));
            } else {
                resultList.add(entry);
            }
        }

        metaDataElements.clear();
        metaDataElements.addAll(resultList);
    }

    public static String getReplacement(String name) {
        final String[] tokens = StringUtils.split(name, new char[]{SEPARATOR}, true);
        final StringBuilder result = new StringBuilder();

        boolean replaced = false;
        for (final String token : tokens) {
            final String replaceToken = NAME_REPLACEMENTS.get(token);
            if (StringUtils.isNotNullAndNotEmpty(replaceToken)) {
                result.append(replaceToken);
                result.append(SEPARATOR);
                replaced = true;
            } else {
                result.append(token);
                result.append(SEPARATOR);
            }
        }
        if (replaced) {
            final String resultString = result.toString();
            return resultString.substring(0, resultString.length() - 1);    // skip trailing colon
        }
        return null;    // nothing replaced
    }

    public static boolean hasShrinkedAttributes(List<Attribute> metaDataElements) {
        for (final Attribute attribute : metaDataElements) {
            final String name = attribute.getShortName();
            if (name.startsWith("FH") || name.startsWith("VH")) {
                return true;
            }
        }
        return false;
    }
}
