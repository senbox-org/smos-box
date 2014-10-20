package org.esa.beam.dataio.smos.bufr;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.smos.SmosUtils;
import org.esa.beam.util.io.BeamFileFilter;

import java.io.File;
import java.util.Locale;

public class SmosBufrReaderPlugIn implements ProductReaderPlugIn {

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final File file = input instanceof File ? (File) input : new File(input.toString());
        final String fileName = file.getName();

        if (SmosUtils.isLightBufrType(fileName)) {
            return DecodeQualification.INTENDED;
        }

        return DecodeQualification.UNABLE;
    }

    @Override
    public Class[] getInputTypes() {
        return new Class[]{File.class, String.class};
    }

    @Override
    public ProductReader createReaderInstance() {
        return new SmosBufrReader(this);
    }

    @Override
    public String[] getFormatNames() {
        return new String[] {"SMOS Light-BUFR"};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[] {".bin"};
    }

    @Override
    public String getDescription(Locale locale) {
        return "SMOS BUFR light data products";
    }

    @Override
    public BeamFileFilter getProductFileFilter() {
        return new BeamFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null));
    }
}
