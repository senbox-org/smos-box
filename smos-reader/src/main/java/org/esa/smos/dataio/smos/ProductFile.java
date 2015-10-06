package org.esa.smos.dataio.smos;

import org.esa.snap.core.datamodel.Product;

import java.awt.geom.Area;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * @author Ralf Quast
 */
public interface ProductFile extends Closeable {

    @Override
    void close() throws IOException;

    Product createProduct() throws IOException;

    Area getArea();

    File getDataFile();
}
