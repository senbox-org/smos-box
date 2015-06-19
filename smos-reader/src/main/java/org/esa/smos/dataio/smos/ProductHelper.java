/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.smos.dataio.smos;

import com.bc.ceres.binio.SimpleType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.jexp.ParseException;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dgg.SmosDgg;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.ColorPaletteDef;
import org.esa.snap.framework.datamodel.CrsGeoCoding;
import org.esa.snap.framework.datamodel.FlagCoding;
import org.esa.snap.framework.datamodel.GeoCoding;
import org.esa.snap.framework.datamodel.ImageInfo;
import org.esa.snap.framework.datamodel.Mask;
import org.esa.snap.framework.datamodel.MetadataAttribute;
import org.esa.snap.framework.datamodel.MetadataElement;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductData;
import org.esa.snap.framework.datamodel.VirtualBand;
import org.esa.snap.framework.dataop.barithm.BandArithmetic;
import org.esa.snap.util.io.FileUtils;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Random;

public class ProductHelper {

    private ProductHelper() {
    }

    public static ImageInfo createImageInfo(Band band, BandDescriptor descriptor) {
        final Color[] colors;

        if (descriptor.getBandName().equalsIgnoreCase("Soil_Moisture")) {
            return createSoilMoistureImageInfo();
        }

        if (descriptor.getBandName().contains("Tau_Nad_")) {
            return createTauNadImageInfo();
        }

        if (descriptor.isCyclic()) {
            colors = new Color[]{
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0),
                    new Color(255, 140, 0),
                    new Color(255, 255, 0),
                    new Color(0, 255, 0),
                    new Color(0, 255, 255),
                    new Color(0, 0, 255),
                    new Color(85, 0, 136),
            };
        } else {
            colors = new Color[]{
                    new Color(0, 0, 0),
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0)
            };
        }

        final double min;
        final double max;
        if (descriptor.hasTypicalMin()) {
            min = descriptor.getTypicalMin();
        } else {
            min = band.getStx().getMinimum();
        }
        if (descriptor.hasTypicalMax()) {
            max = descriptor.getTypicalMax();
        } else {
            max = band.getStx().getMaximum();
        }

        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[colors.length];
        for (int i = 0; i < colors.length; i++) {
            final double sample = min + ((max - min) * i / (colors.length - 1));
            points[i] = new ColorPaletteDef.Point(sample, colors[i]);
        }

        return new ImageInfo(new ColorPaletteDef(points));
    }

    private static ImageInfo createSoilMoistureImageInfo() {
        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[5];
        points[0] = new ColorPaletteDef.Point(0.00, new Color(255,136,0));
        points[1] = new ColorPaletteDef.Point(0.05, new Color(237,190,0));
        points[2] = new ColorPaletteDef.Point(0.10, new Color(225,221,0));
        points[3] = new ColorPaletteDef.Point(0.20, new Color(106,193,0));
        points[4] = new ColorPaletteDef.Point(1.00, new Color(0,0,153));

        return new ImageInfo(new ColorPaletteDef(points));
    }

    private static ImageInfo createTauNadImageInfo() {
        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[3];
        points[0] = new ColorPaletteDef.Point(0.00, new Color(213,255,135));
        points[1] = new ColorPaletteDef.Point(0.15, new Color(135,181,0));
        points[2] = new ColorPaletteDef.Point(1.20, new Color(0,45,0));

        return new ImageInfo(new ColorPaletteDef(points));
    }


    public static GeoCoding createGeoCoding(Dimension dimension) {
        final AffineTransform transform = SmosDgg.getInstance().getImageToMapTransform();
        try {
            return new CrsGeoCoding(DefaultGeographicCRS.WGS84, new Rectangle(dimension), transform);
        } catch (FactoryException | TransformException e) {
            throw new IllegalArgumentException("dimension");
        }
    }

    public static Dimension getSceneRasterDimension() {
        final MultiLevelImage dggMultiLevelImage = SmosDgg.getInstance().getMultiLevelImage();
        final int w = dggMultiLevelImage.getWidth();
        final int h = dggMultiLevelImage.getHeight();

        return new Dimension(w, h);
    }

    public static Product createProduct(File inputFile, String productType) {
        final String productName = FileUtils.getFilenameWithoutExtension(inputFile);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(inputFile);
        product.setPreferredTileSize(512, 512);

        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));

        return product;
    }

    static int getDataType(Type memberType) {
        if (memberType.equals(SimpleType.BYTE)) {
            return ProductData.TYPE_INT8;
        }
        if (memberType.equals(SimpleType.UBYTE)) {
            return ProductData.TYPE_UINT8;
        }
        if (memberType.equals(SimpleType.SHORT)) {
            return ProductData.TYPE_INT16;
        }
        if (memberType.equals(SimpleType.USHORT)) {
            return ProductData.TYPE_UINT16;
        }
        if (memberType.equals(SimpleType.INT)) {
            return ProductData.TYPE_INT32;
        }
        if (memberType.equals(SimpleType.UINT)) {
            return ProductData.TYPE_UINT32;
        }
        if (memberType.equals(SimpleType.FLOAT)) {
            return ProductData.TYPE_FLOAT32;
        }
        if (memberType.equals(SimpleType.DOUBLE)) {
            return ProductData.TYPE_FLOAT64;
        }
        if (memberType.equals(SimpleType.ULONG)) {
            return ProductData.TYPE_UINT32;
        }

        throw new IllegalArgumentException("Illegal member type:" + memberType.getName());
    }

    static void addMetadata(MetadataElement metadataElement, ExplorerFile explorerFile) throws IOException {
        final Document document;

        try {
            document = new SAXBuilder().build(explorerFile.getHeaderFile());
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Invalid document", explorerFile.getHeaderFile().getPath()), e);
        }

        final Namespace namespace = document.getRootElement().getNamespace();
        if (namespace == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing namespace", explorerFile.getHeaderFile().getPath()));
        }

        addMetadata(metadataElement, document.getRootElement(), namespace);
    }

    static void addMetadata(MetadataElement metadataElement, Element xmlElement, Namespace namespace) {
        for (final Object o : xmlElement.getChildren()) {
            final Element xmlChild = (Element) o;

            if (xmlChild.getChildren(null, namespace).size() == 0) {
                final String s = xmlChild.getTextNormalize();
                if (s != null && !s.isEmpty()) {
                    metadataElement.addAttribute(
                            new MetadataAttribute(xmlChild.getName(), ProductData.createInstance(s), true));
                }
            } else {
                final MetadataElement metadataChild = new MetadataElement(xmlChild.getName());
                metadataElement.addElement(metadataChild);
                addMetadata(metadataChild, xmlChild, namespace);
            }
        }
    }

    public static void addFlagsAndMasks(Product product, Band band,
                                 String flagCodingName, Family<FlagDescriptor> flagDescriptors) {
        FlagCoding flagCoding = product.getFlagCodingGroup().get(flagCodingName);
        if (flagCoding == null) {
            flagCoding = new FlagCoding(flagCodingName);
            for (final FlagDescriptor flagDescriptor : flagDescriptors.asList()) {
                flagCoding.addFlag(flagDescriptor.getFlagName(),
                                   flagDescriptor.getMask(),
                                   flagDescriptor.getDescription());
            }
            product.getFlagCodingGroup().add(flagCoding);
        }
        band.setSampleCoding(flagCoding);

        final Random random = new Random(5489); // for creating random colours
        for (final FlagDescriptor flagDescriptor : flagDescriptors.asList()) {
            final String maskName = band.getName() + "_" + flagDescriptor.getFlagName();
            if (!product.getMaskGroup().contains(maskName)) {
                Color color = flagDescriptor.getColor();
                if (color == null) {
                    color = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
                }
                final String expression = band.getName() + "." + flagDescriptor.getFlagName();
                Mask mask = Mask.BandMathsType.create(maskName, flagDescriptor.getDescription(),
                                                      product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                                                      expression, color, flagDescriptor.getTransparency());
                product.getMaskGroup().add(mask);
            }
        }
    }

    static void addVirtualBand(Product product, BandDescriptor descriptor, String bandExpression) {
        final VirtualBand virtualBand = new VirtualBand(descriptor.getBandName(), ProductData.TYPE_FLOAT32,
                                                 product.getSceneRasterWidth(),
                                                 product.getSceneRasterHeight(),
                                                 bandExpression);

        final String validPixelExpression = createValidPixelExpression(product, bandExpression);
        virtualBand.setValidPixelExpression(validPixelExpression);
        virtualBand.setImageInfo(createImageInfo(virtualBand, descriptor));
        virtualBand.setDescription(descriptor.getDescription());
        virtualBand.setUnit(descriptor.getUnit());
        virtualBand.setGeophysicalNoDataValue(descriptor.getFillValue());
        virtualBand.setNoDataValueUsed(descriptor.hasFillValue());
        product.addBand(virtualBand);
    }

    static String createValidPixelExpression(Product product, String expression) {
        try {
            return BandArithmetic.getValidMaskExpression(expression, new Product[]{product}, 0, null);
        } catch (ParseException e) {
            return null;
        }
    }
}
