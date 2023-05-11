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

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.StringUtils;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

public abstract class ExplorerFile implements ProductFile {

    protected static final String TAG_SPECIFIC_PRODUCT_HEADER = "Specific_Product_Header";

    private final EEFilePair eeFilePair;
    private final DataFormat dataFormat;
    private final DataContext dataContext;

    protected ExplorerFile(EEFilePair eeFilePair, DataContext dataContext) throws IOException {
        this.eeFilePair = eeFilePair;
        this.dataFormat = Dddb.getInstance().getDataFormat(eeFilePair.getHdrFile());
        this.dataContext = dataContext;
    }

    public final File getHeaderFile() {
        return eeFilePair.getHdrFile();
    }

    @Override
    public final File getDataFile() {
        return eeFilePair.getDblFile();
    }

    public final DataFormat getDataFormat() {
        return dataFormat;
    }

    public final CompoundData getDataBlock() {
        return dataContext.getData();
    }

    @Override
    public void close() {
        dataContext.dispose();
    }

    protected String getProductType() {
        return dataFormat.getName().substring(12, 22);
    }

    protected final Document getDocument() throws IOException {
        final Document document;
        try {
            document = new SAXBuilder().build(eeFilePair.getHdrFile());
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format("File ''{0}'': Invalid document", eeFilePair.getHdrFile().getPath()), e);
        }
        return document;
    }

    protected Element getElement(Element parent, final String name) throws IOException {
        final Iterator<Content> descendants = parent.getDescendants();

        while(descendants.hasNext()) {
            Object o = descendants.next();
            if (o instanceof Element) {
                Element e = (Element) o;

                if (name.equals(e.getName())) {
                    return e;
                }
            }
        }
        throw new IOException(MessageFormat.format("File ''{0}'': Missing element ''{1}''.", getHeaderFile().getPath(), name));
    }

    protected void addAncilliaryBands(Product product) {
        final String formatName = getDataFormat().getName();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(formatName);

        if (descriptors == null) {
            return;
        }

        for (final BandDescriptor descriptor : descriptors.asList()) {
            final String ancilliaryBandName = descriptor.getAncilliaryBandName();
            if (StringUtils.isNotNullAndNotEmpty(ancilliaryBandName)) {
                final Band dataBand = product.getBand(descriptor.getBandName());
                final Band ancilliaryBand = product.getBand(ancilliaryBandName);

                final String bandRole = getAncilliaryBandRole(ancilliaryBandName);
                dataBand.addAncillaryVariable(ancilliaryBand, bandRole);
            }
        }
    }

    static String getAncilliaryBandRole(String ancilliaryBandName) {
        if (ancilliaryBandName.contains("DQX")) {
            return "standard_deviation";
        }
        if (ancilliaryBandName.contains("Accuracy")) {
            return "uncertainty";
        }

        return "error";
    }
}
