package org.esa.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import org.apache.commons.lang.StringUtils;
import org.esa.smos.DateTimeUtils;
import org.esa.smos.dataio.smos.SmosFile;
import org.esa.smos.dataio.smos.SmosProductReader;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.MemberDescriptor;
import org.esa.smos.ee2netcdf.geometry.GeometryFilter;
import org.esa.smos.ee2netcdf.geometry.GeometryFilterFactory;
import org.esa.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.dataio.netcdf.nc.NFileWriteable;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractFormatExporter implements FormatExporter {

    protected int gridPointCount;
    protected SmosFile explorerFile;
    protected Map<String, VariableDescriptor> variableDescriptors;
    protected ArrayList<Integer> gpIndexList;
    private Family<MemberDescriptor> memberDescriptors;

    // package access for testing only tb 2014-07-01
    static List<AttributeEntry> extractMetadata(MetadataElement root) {
        final List<AttributeEntry> properties = new ArrayList<>();
        MetadataUtils.extractAttributes(root, properties, "");

        return properties;
    }

    // package access for testing only tb 2014-08-01
    static boolean mustExport(String bandName, String[] outputBandNames) {
        if (outputBandNames.length == 0) {
            return true;
        }

        for (final String outputBandName : outputBandNames) {
            if (outputBandName.equalsIgnoreCase(bandName)) {
                return true;
            }
        }
        return false;
    }

    private static SmosFile getSmosFile(Product product) {
        final SmosProductReader smosReader = (SmosProductReader) product.getProductReader();
        return (SmosFile) smosReader.getProductFile();
    }

    // package access for testing only tb 2014-07-30
    static void setDataType(VariableDescriptor variableDescriptor, String dataTypeName) {
        if (StringUtils.isBlank(dataTypeName)) {
            throw new IllegalStateException("datatype not set for '" + variableDescriptor.getName() + "'");
        }

        if ("uint".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.INT);
            variableDescriptor.setUnsigned(true);
        } else if ("int".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.INT);
        } else if ("float".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.FLOAT);
        } else if ("ubyte".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.BYTE);
            variableDescriptor.setUnsigned(true);
        } else if ("ushort".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.SHORT);
            variableDescriptor.setUnsigned(true);
        } else if ("short".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.SHORT);
        } else if ("ulong".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.LONG);
            variableDescriptor.setUnsigned(true);
        } else if ("double".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.DOUBLE);
        } else {
            throw new IllegalArgumentException("unsupported datatype: '" + dataTypeName + "'");
        }
    }

    // package access for testing only tb 2014-07-30
    static int getNumDimensions(String dimensionNames) {
        if (StringUtils.isBlank(dimensionNames)) {
            throw new IllegalArgumentException("empty dimension names");
        }
        final String[] splittednames = StringUtils.split(dimensionNames, ' ');
        return splittednames.length;
    }

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        explorerFile = getSmosFile(product);
        gridPointCount = explorerFile.getGridPointCount();

        memberDescriptors = Dddb.getInstance().getMemberDescriptors(explorerFile.getHeaderFile());
        createVariableDescriptors(exportParameter);
    }

    @Override
    public int prepareGeographicSubset(ExportParameter exportParameter) throws IOException {
        if (exportParameter.getGeometry() != null) {
            final GeometryFilter geometryFilter = GeometryFilterFactory.create(exportParameter.getGeometry());
            gpIndexList = new ArrayList<>(gridPointCount);
            for (int i = 0; i < gridPointCount; i++) {
                final CompoundData gridPointData = explorerFile.getGridPointData(i);
                if (geometryFilter.accept(gridPointData)) {
                    gpIndexList.add(i);
                }
            }

            gridPointCount = gpIndexList.size();
            return gridPointCount;
        }

        return -1;
    }

    @Override
    public void addGlobalAttributes(NFileWriteable nFileWriteable, MetadataElement metadataRoot,
                                    ExportParameter exportParameter) throws IOException {
        final String institution = exportParameter.getInstitution();
        if (StringUtils.isNotBlank(institution)) {
            nFileWriteable.addGlobalAttribute("institution", institution);
        }
        final String contact = exportParameter.getContact();
        if (StringUtils.isNotBlank(contact)) {
            nFileWriteable.addGlobalAttribute("contact", contact);
        }
        nFileWriteable.addGlobalAttribute("creation_date", DateTimeUtils.toFixedHeaderFormat(new Date()));
        nFileWriteable.addGlobalAttribute("total_number_of_grid_points", Integer.toString(gridPointCount));

        final List<AttributeEntry> attributeList = extractMetadata(metadataRoot);
        for (final AttributeEntry entry : attributeList) {
            nFileWriteable.addGlobalAttribute(entry.getName(), entry.getValue());
        }
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable, ExportParameter exportParameter) throws IOException {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final String[] outputBandNames = exportParameter.getVariableNames();
        for (final String ncVariableName : variableNameKeys) {
            if (!mustExport(ncVariableName, outputBandNames)) {
                continue;
            }
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            final NVariable nVariable = nFileWriteable.addVariable(ncVariableName,
                    variableDescriptor.getDataType(),
                    true,
                    null,
                    variableDescriptor.getDimensionNames(),
                    exportParameter.getCompressionLevel());
            final String unitValue = variableDescriptor.getUnit();
            if (StringUtils.isNotBlank(unitValue)) {
                nVariable.addAttribute("units", unitValue);
            }
            if (variableDescriptor.isFillValuePresent()) {
                nVariable.addAttribute("_FillValue", variableDescriptor.getFillValue());
            }
            if (variableDescriptor.isValidMinPresent()) {
                nVariable.addAttribute("valid_min", variableDescriptor.getValidMin());
            }
            if (variableDescriptor.isValidMaxPresent()) {
                nVariable.addAttribute("valid_max", variableDescriptor.getValidMax());
            }
            final short[] flagMasks = variableDescriptor.getFlagMasks();
            if (flagMasks != null) {
                nVariable.addAttribute("flag_masks", Array.factory(DataType.SHORT, new int[]{flagMasks.length}, flagMasks));
            }
            final short[] flagValues = variableDescriptor.getFlagValues();
            if (flagValues != null) {
                nVariable.addAttribute("flag_values", Array.factory(DataType.SHORT, new int[]{flagValues.length}, flagValues));
            }
            final String flagMeanings = variableDescriptor.getFlagMeanings();
            if (StringUtils.isNotBlank(flagMeanings)) {
                nVariable.addAttribute("flag_meanings", flagMeanings);
            }
            if (variableDescriptor.isScaleFactorPresent() || variableDescriptor.isScaleOffsetPresent()) {
                nVariable.addAttribute("scale_factor", variableDescriptor.getScaleFactor());
                nVariable.addAttribute("scale_offset", variableDescriptor.getScaleOffset());
            }
            if (variableDescriptor.isUnsigned()) {
                nVariable.addAttribute("_Unsigned", "true");
            }
        }
    }

    @Override
    abstract public void addDimensions(NFileWriteable nFileWriteable) throws IOException;

    void createVariableDescriptors(ExportParameter exportParameter) {
        variableDescriptors = new HashMap<>();

        final String[] outputBandNames = exportParameter.getVariableNames();

        final List<MemberDescriptor> memberDescriptorList = memberDescriptors.asList();
        for (final MemberDescriptor memberDescriptor : memberDescriptorList) {
            final String memberDescriptorName = memberDescriptor.getName();
            if (mustExport(memberDescriptorName, outputBandNames)) {
                final String dimensionNames = memberDescriptor.getDimensionNames();
                final int numDimensions = getNumDimensions(dimensionNames);
                final String variableName = ExporterUtils.ensureNetCDFName(memberDescriptorName);
                final VariableDescriptor variableDescriptor = new VariableDescriptor(variableName,
                        memberDescriptor.isGridPointData(),
                        DataType.OBJECT,
                        dimensionNames,
                        numDimensions == 2,
                        memberDescriptor.getMemberIndex());

                setDataType(variableDescriptor, memberDescriptor.getDataTypeName());

                variableDescriptor.setUnit(memberDescriptor.getUnit());
                variableDescriptor.setFillValue(memberDescriptor.getFillValue());

                final float scalingFactor = memberDescriptor.getScalingFactor();
                if (scalingFactor != 1.0) {
                    variableDescriptor.setScaleFactor(scalingFactor);
                }

                final float scalingOffset = memberDescriptor.getScalingOffset();
                if (scalingOffset != 0.0) {
                    variableDescriptor.setScaleOffset(memberDescriptor.getScalingOffset());
                }

                final short[] flagMasks = memberDescriptor.getFlagMasks();
                if (flagMasks != null) {
                    variableDescriptor.setFlagMasks(memberDescriptor.getFlagMasks());
                    variableDescriptor.setFlagValues(memberDescriptor.getFlagValues());
                    variableDescriptor.setFlagMeanings(memberDescriptor.getFlagMeanings());
                }

                variableDescriptors.put(variableName, variableDescriptor);
            }
        }
    }
}
