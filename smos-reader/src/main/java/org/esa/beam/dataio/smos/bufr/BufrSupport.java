package org.esa.beam.dataio.smos.bufr;

import org.esa.beam.dataio.netcdf.util.MetadataUtils;
import org.esa.beam.dataio.smos.ProductHelper;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;
import ucar.nc2.Attribute;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;

class BufrSupport {

    private SmosBufrFile smosBufrFile;

    void open(String location) throws IOException {
        smosBufrFile = SmosBufrFile.open(location);
    }

    void close() throws IOException {
        if (smosBufrFile != null) {
            smosBufrFile.close();
            smosBufrFile = null;
        }
    }

    SmosBufrFile getSmosBufrFile() {
        return smosBufrFile;
    }

    Product createProduct(File inputFile, String productType) {
        final String productName = FileUtils.getFilenameWithoutExtension(inputFile);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(new File(smosBufrFile.getLocation()));
        product.setPreferredTileSize(512, 512);

        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));

        return product;
    }

    void extractMetaData(Product product) {
        final java.util.List<Attribute> globalAttributes = smosBufrFile.getGlobalAttributes();
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(MetadataUtils.readAttributeList(globalAttributes, "Global_Attributes"));
        final Sequence sequence = smosBufrFile.getObservationStructure();
        final java.util.List<Variable> variables = sequence.getVariables();
        metadataRoot.addElement(MetadataUtils.readVariableDescriptions(variables, "Variable_Attributes", 100));
    }
}
