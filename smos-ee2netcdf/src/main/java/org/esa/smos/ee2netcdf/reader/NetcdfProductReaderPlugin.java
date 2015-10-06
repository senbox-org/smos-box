package org.esa.smos.ee2netcdf.reader;


import org.esa.smos.SmosUtils;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.snap.util.io.FileUtils;
import org.esa.snap.util.io.SnapFileFilter;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NetcdfProductReaderPlugin implements ProductReaderPlugIn {

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
        } else {
            if (isValidSmosNetCDF(file)) {
                return DecodeQualification.INTENDED;
            }
        }

        return DecodeQualification.UNABLE;
    }

    @Override
    public Class[] getInputTypes() {
        return new Class[]{File.class, String.class};
    }

    @Override
    public ProductReader createReaderInstance() {
        return new NetcdfProductReader(this);
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

    private boolean isValidSmosNetCDF(File file) {
        NetcdfFile netcdfFile = null;
        try {
            netcdfFile = NetcdfFileOpener.open(file.getAbsolutePath());
            if (netcdfFile == null) {
                return false;
            }

            final List<Attribute> globalAttributes = netcdfFile.getGlobalAttributes();
            for (final Attribute attribute : globalAttributes) {
                final String fullName = attribute.getFullName();
                if ("total_number_of_grid_points".equalsIgnoreCase(fullName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (netcdfFile != null) {
                try {
                    netcdfFile.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return false;
    }
}
