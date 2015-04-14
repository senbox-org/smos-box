package org.esa.smos.dataio.smos.bufr;

import org.esa.smos.SmosUtils;
import org.esa.snap.framework.dataio.DecodeQualification;
import org.esa.snap.framework.dataio.ProductReader;
import org.esa.snap.framework.dataio.ProductReaderPlugIn;
import org.esa.snap.util.io.BeamFileFilter;

import java.io.File;
import java.util.Locale;

public class SmosLightBufrReaderPlugIn implements ProductReaderPlugIn {

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final File file;
        if (input instanceof File) {
            file = (File) input;
        } else if (input instanceof String) {
            file = new File((String) input);
        } else {
            return DecodeQualification.UNABLE;
        }
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
        return new SmosLightBufrReader(this);
    }

    @Override
    public String[] getFormatNames() {
        return new String[] {"SMOS Light-BUFR"};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[] {".bin", ".bin.bz2"};
    }

    @Override
    public String getDescription(Locale locale) {
        return "SMOS Light-BUFR data products";
    }

    @Override
    public BeamFileFilter getProductFileFilter() {
        return new BeamFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null));
    }
}
