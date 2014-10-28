package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GPToNetCDFExporterOpTest {

    @Test
    public void testOperatorAnnotations() {
        final Annotation[] declaredAnnotations = GPToNetCDFExporterOp.class.getDeclaredAnnotations();

        assertEquals(1, declaredAnnotations.length);
        final OperatorMetadata operatorMetadata = (OperatorMetadata) declaredAnnotations[0];
        assertEquals("SmosGP2NetCDF", operatorMetadata.alias());
    }

    @Test
    public void testParameterAnnotations_Institution() throws NoSuchFieldException {
        final Field institutionField = GPToNetCDFExporterOp.class.getDeclaredField("institution");
        final Parameter institutionFieldAnnotation = institutionField.getAnnotation(Parameter.class);
        assertEquals("", institutionFieldAnnotation.defaultValue());
    }

    @Test
    public void testParameterAnnotations_Contact() throws NoSuchFieldException {
        final Field contactField = GPToNetCDFExporterOp.class.getDeclaredField("contact");
        final Parameter contactFieldAnnotation = contactField.getAnnotation(Parameter.class);
        assertEquals("", contactFieldAnnotation.defaultValue());
    }

    @Test
    public void testParameterAnnotations_outputBandNames() throws NoSuchFieldException {
        final Field bandNamesField = GPToNetCDFExporterOp.class.getDeclaredField("outputBandNames");
        final Parameter bandNamesFieldAnnotation = bandNamesField.getAnnotation(Parameter.class);
        assertEquals("", bandNamesFieldAnnotation.defaultValue());
    }

    @Test
    public void testParameterAnnotations_compressionLevel() throws NoSuchFieldException {
        final Field compressionLevelField = GPToNetCDFExporterOp.class.getDeclaredField("compressionLevel");
        final Parameter compressionLevelFieldAnnotation = compressionLevelField.getAnnotation(Parameter.class);
        assertEquals("6", compressionLevelFieldAnnotation.defaultValue());
        final String[] valueSet = compressionLevelFieldAnnotation.valueSet();
        assertArrayEquals(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}, valueSet);
    }
}
