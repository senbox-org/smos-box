package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Coordinate;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.util.converters.JtsGeometryConverter;
import org.junit.Test;

import java.awt.Rectangle;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SmosEEToNetCDFExportOpTest {

    @Test
    public void testParameterAnnotations_SourceProducts() throws NoSuchFieldException {
        final Field sourceProductsField = SmosEEToNetCDFExportOp.class.getDeclaredField("sourceProducts");
        final SourceProducts sourceProducts = sourceProductsField.getAnnotation(SourceProducts.class);
        assertEquals(0, sourceProducts.count());
        assertEquals("MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2", sourceProducts.type());
        assertEquals(0, sourceProducts.bands().length);
    }

    @Test
    public void testParameterAnnotation_targetDirectory() throws NoSuchFieldException {
        final Field targetDirectoryField = SmosEEToNetCDFExportOp.class.getDeclaredField("targetDirectory");
        final Parameter targetDirectory = targetDirectoryField.getAnnotation(Parameter.class);
        assertEquals(".", targetDirectory.defaultValue());
        assertTrue(targetDirectory.notEmpty());
        assertTrue(targetDirectory.notNull());
    }

    @Test
    public void testParameterAnnotations_OverwriteTarget() throws NoSuchFieldException {
        final Field regionField = SmosEEToNetCDFExportOp.class.getDeclaredField("overwriteTarget");
        final Parameter overwriteTargetFieldAnnotation = regionField.getAnnotation(Parameter.class);
        assertEquals("false", overwriteTargetFieldAnnotation.defaultValue());
    }

    @Test
    public void testParameterAnnotations_Region() throws NoSuchFieldException {
        final Field regionField = SmosEEToNetCDFExportOp.class.getDeclaredField("region");
        final Parameter regionFieldAnnotation = regionField.getAnnotation(Parameter.class);
        assertEquals("", regionFieldAnnotation.defaultValue());
        assertEquals(JtsGeometryConverter.class, regionFieldAnnotation.converter());
        assertFalse(regionFieldAnnotation.notEmpty());
        assertFalse(regionFieldAnnotation.notNull());
    }

    @Test
    public void testParameterAnnotations_Institution() throws NoSuchFieldException {
        final Field institutionField = SmosEEToNetCDFExportOp.class.getDeclaredField("institution");
        final Parameter institutionFieldAnnotation = institutionField.getAnnotation(Parameter.class);
        assertEquals("", institutionFieldAnnotation.defaultValue());
    }

    @Test
    public void testParameterAnnotations_Contact() throws NoSuchFieldException {
        final Field contactField = SmosEEToNetCDFExportOp.class.getDeclaredField("contact");
        final Parameter contactFieldAnnotation = contactField.getAnnotation(Parameter.class);
        assertEquals("", contactFieldAnnotation.defaultValue());
    }

    @Test
    public void testParameterAnnotations_outputBandNames() throws NoSuchFieldException {
        final Field bandNamesField = SmosEEToNetCDFExportOp.class.getDeclaredField("variableNames");
        final Parameter bandNamesFieldAnnotation = bandNamesField.getAnnotation(Parameter.class);
        assertEquals("", bandNamesFieldAnnotation.defaultValue());
    }

    @Test
    public void testParameterAnnotations_compressionLevel() throws NoSuchFieldException {
        final Field compressionLevelField = SmosEEToNetCDFExportOp.class.getDeclaredField("compressionLevel");
        final Parameter compressionLevelFieldAnnotation = compressionLevelField.getAnnotation(Parameter.class);
        assertEquals("6", compressionLevelFieldAnnotation.defaultValue());
        final String[] valueSet = compressionLevelFieldAnnotation.valueSet();
        assertArrayEquals(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}, valueSet);
    }

    @Test
    public void testCreateOperator() {
        final SmosEEToNetCDFExportOp.Spi spi = new SmosEEToNetCDFExportOp.Spi();

        final Operator operator = spi.createOperator();
        assertNotNull(operator);
        assertTrue(operator instanceof SmosEEToNetCDFExportOp);
    }
}
