package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.SmosUtils;
import org.esa.snap.framework.dataio.DecodeQualification;
import org.esa.snap.framework.dataio.ProductReader;
import org.esa.snap.framework.dataio.ProductReaderPlugIn;
import org.esa.snap.util.io.FileUtils;
import org.esa.snap.util.io.SnapFileFilter;

import java.io.File;
import java.util.Locale;

public class NetcdfReaderPlugin implements ProductReaderPlugIn {

    private static final String EXTENSION = ".nc";

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final File file = input instanceof File ? (File) input : new File(input.toString());
        final String fileName = file.getName();

        final String extension = FileUtils.getExtension(fileName);
        if (!EXTENSION.equals(extension)) {
            return DecodeQualification.UNABLE;
        }

        final String fileNameWithHDRExtension = FileUtils.exchangeExtension(fileName, ".HDR");
        if (SmosUtils.isL1cType(fileNameWithHDRExtension) ||
                SmosUtils.isL2Type(fileNameWithHDRExtension)) {
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
        return null;
    }

    @Override
    public String[] getFormatNames() {
        return new String[]{"SMOS-NC"};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[]{EXTENSION};
    }

    @Override
    public String getDescription(Locale locale) {
        return "SMOS Data Products in NetCDF Format";
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null));
    }
}
