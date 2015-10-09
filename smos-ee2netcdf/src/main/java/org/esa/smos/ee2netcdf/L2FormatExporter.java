package org.esa.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import org.esa.smos.SmosUtils;
import org.esa.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.smos.ee2netcdf.variable.VariableWriter;
import org.esa.smos.ee2netcdf.variable.VariableWriterFactory;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.dataio.netcdf.nc.NFileWriteable;
import org.esa.snap.dataio.netcdf.nc.NVariable;

import java.io.IOException;
import java.util.Set;

class L2FormatExporter extends AbstractFormatExporter {


    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);

        applyChiSquareScalingIfNecessary(product);
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final VariableWriter[] variableWriters = createVariableWriters(nFileWriteable);

        if (gpIndexList == null) {
            for (int i = 0; i < gridPointCount; i++) {
                writeCompound(i, i, variableWriters);
            }
        } else {
            int writeIndex = 0;
            for (int index : gpIndexList) {
                writeCompound(index, writeIndex, variableWriters);
                ++writeIndex;
            }
        }

        for (VariableWriter writer : variableWriters) {
            writer.close();
        }
    }

    private void applyChiSquareScalingIfNecessary(Product product) {
        final String productType = product.getProductType();
        if (SmosUtils.isSmUserFormat(productType)) {
            final MetadataElement specificProductHeader = ExporterUtils.getSpecificProductHeader(product);
            if (specificProductHeader == null) return;

            final MetadataAttribute chi2ScaleAttribute = specificProductHeader.getAttribute("Chi_2_Scale");
            if (chi2ScaleAttribute == null) {
                return;
            }

            final double scaleFactor = chi2ScaleAttribute.getData().getElemDouble();
            if (scaleFactor != 1.0) {
                ExporterUtils.correctScaleFactor(variableDescriptors, "Chi_2", scaleFactor);
            }
        }
    }

    private void writeCompound(int readIndex, int writeIndex, VariableWriter[] variableWriters) throws IOException {
        final CompoundData gridPointData = explorerFile.getGridPointData(readIndex);
        for (VariableWriter writer : variableWriters) {
            writer.write(gridPointData, null, writeIndex);
        }
    }

    private VariableWriter[] createVariableWriters(NFileWriteable nFileWriteable) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final VariableWriter[] variableWriters = new VariableWriter[variableNameKeys.size()];
        int index = 0;
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);

            variableWriters[index] = VariableWriterFactory.create(nVariable, variableDescriptor, gridPointCount, -1);
            index++;
        }
        return variableWriters;
    }
}
