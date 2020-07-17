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
import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.Type;
import com.bc.ceres.binio.util.NumberUtils;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.smos.DateTimeUtils;
import org.esa.smos.EEFilePair;
import org.esa.smos.SmosUtils;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dataio.smos.dffg.DffgLaiFile;
import org.esa.smos.dataio.smos.dffg.DffgSnoFile;
import org.esa.smos.dgg.SmosDgg;
import org.esa.smos.lsmask.SmosLsMask;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SmosProductReader extends SmosReader {

    private static final String LSMASK_SCHEMA_NAME = "DBL_SM_XXXX_AUX_LSMASK_0200";

    private ProductFile productFile;
    private VirtualDir virtualDir;

    public ProductFile getProductFile() {
        return productFile;
    }

    public static ProductFile createProductFile(File file) throws IOException {
        if (file.isDirectory()) {
            final File[] files = file.listFiles(new ExplorerFilenameFilter());
            if (files != null && files.length == 2) {
                file = files[0];
            }
        }

        final ProductFile productFile = createProductFileImplementation(file);
        if (productFile == null) {
            throw new IOException(MessageFormat.format("File ''{0}'': unknown/unsupported SMOS data format.", file));
        }
        return productFile;
    }

    @Override
    public GridPointBtDataset getBtData(int gridPointIndex) throws IOException {
        if (productFile instanceof L1cSmosFile) {
            return readBtData(gridPointIndex);
        }
        return null;
    }

    @Override
    public boolean canSupplyGridPointBtData() {
        return productFile instanceof L1cSmosFile;
    }

    @Override
    public boolean canSupplyFullPolData() {
        return SmosUtils.isFullPolScienceFormat(productFile.getDataFile().getName());
    }

    @Override
    public int getGridPointIndex(int gridPointId) {
        if (productFile instanceof L1cSmosFile) {
            return ((L1cSmosFile) productFile).getGridPointIndex(gridPointId);
        }
        return -1;
    }

    @Override
    public int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel) {
        final MultiLevelImage levelImage = SmosDgg.getInstance().getMultiLevelImage();
        final RenderedImage image = levelImage.getImage(currentLevel);
        final Raster data = image.getData(new Rectangle(levelPixelX, levelPixelY, 1, 1));
        return data.getSample(levelPixelX, levelPixelY, 0);
    }

    private GridPointBtDataset readBtData(int gridPointIndex) throws IOException {
        final L1cSmosFile smosFile = (L1cSmosFile) productFile;
        final SequenceData btDataList = smosFile.getBtDataList(gridPointIndex);

        final CompoundType type = (CompoundType) btDataList.getType().getElementType();
        final int memberCount = type.getMemberCount();

        final int btDataListCount = btDataList.getElementCount();

        final Class[] columnClasses = new Class[memberCount];
        final BandDescriptor[] descriptors = new BandDescriptor[memberCount];

        final Dddb dddb = Dddb.getInstance();
        final String formatName = smosFile.getDataFormat().getName();
        for (int j = 0; j < memberCount; j++) {
            final String memberName = type.getMemberName(j);
            final BandDescriptor descriptor = dddb.findBandDescriptorForMember(formatName, memberName);
            if (descriptor == null || descriptor.getScalingFactor() == 1.0 && descriptor.getScalingOffset() == 0.0) {
                columnClasses[j] = NumberUtils.getNumericMemberType(type, j);
            } else {
                columnClasses[j] = Double.class;
            }
            descriptors[j] = descriptor;
        }

        final Number[][] tableData = new Number[btDataListCount][memberCount];
        for (int i = 0; i < btDataListCount; i++) {
            CompoundData btData = btDataList.getCompound(i);
            for (int j = 0; j < memberCount; j++) {
                final Number member = NumberUtils.getNumericMember(btData, j);
                final BandDescriptor descriptor = descriptors[j];
                if (descriptor == null || descriptor.getScalingFactor() == 1.0 && descriptor.getScalingOffset() == 0.0) {
                    tableData[i][j] = member;
                } else {
                    tableData[i][j] = member.doubleValue() * descriptor.getScalingFactor() + descriptor.getScalingOffset();
                }
            }
        }

        final HashMap<String, Integer> memberNamesMap = getRawDataMemberNamesMap(smosFile);

        final GridPointBtDataset btDataset = new GridPointBtDataset(memberNamesMap, columnClasses, tableData);
        for (int i = 0; i < memberCount; i++) {
            final String memberName = type.getMemberName(i);
            final BandDescriptor descriptor = dddb.findBandDescriptorForMember(formatName, memberName);
            if (StringUtils.isNotNullAndNotEmpty(descriptor.getFlagCodingName())) {
                btDataset.setFlagBandIndex(i);
                btDataset.setPolarisationFlagBandIndex(i);
                break;
            }
        }
        final Integer incidence_angle = memberNamesMap.get(SmosConstants.INCIDENCE_ANGLE);
        if (incidence_angle != null) {
            btDataset.setIncidenceAngleBandIndex(incidence_angle);
        }
        final Integer pixel_radiometric_accuracy = memberNamesMap.get("Pixel_Radiometric_Accuracy");
        if (pixel_radiometric_accuracy != null) {
            btDataset.setRadiometricAccuracyBandIndex(pixel_radiometric_accuracy);
        }

        final Integer bt_value_real = memberNamesMap.get("BT_Value_Real");
        if (bt_value_real != null) {
            btDataset.setBTValueRealBandIndex(bt_value_real);
        }
        final Integer bt_value_imag = memberNamesMap.get("BT_Value_Imag");
        if (bt_value_imag != null) {
            btDataset.setBTValueImaginaryBandIndex(bt_value_imag);
        }
        return btDataset;
    }

    @Override
    public String[] getRawDataTableNames() {
        if (productFile instanceof L1cSmosFile) {
            final L1cSmosFile smosFile = (L1cSmosFile) productFile;
            final CompoundType btDataType = smosFile.getBtDataType();
            final CompoundMember[] members = btDataType.getMembers();
            final String[] names = new String[members.length];
            for (int i = 0; i < names.length; i++) {
                CompoundMember member = members[i];
                names[i] = member.getName();
            }
            return names;
        }

        return new String[0];
    }

    @Override
    public FlagDescriptor[] getBtFlagDescriptors() {
        if (productFile instanceof L1cSmosFile) {
            final L1cSmosFile smosFile = (L1cSmosFile) productFile;
            final String dataFormatName = smosFile.getDataFormat().getName();
            final Family<BandDescriptor> bandDescriptors = Dddb.getInstance().getBandDescriptors(dataFormatName);
            final List<BandDescriptor> bandDescriptorsList = bandDescriptors.asList();
            for (final BandDescriptor descriptor : bandDescriptorsList) {
                final Family<FlagDescriptor> flagDescriptors = descriptor.getFlagDescriptors();
                if (flagDescriptors != null) {
                    final List<FlagDescriptor> flagDescriptorList = descriptor.getFlagDescriptors().asList();
                    return flagDescriptorList.toArray(new FlagDescriptor[flagDescriptorList.size()]);
                }
            }
        }

        return new FlagDescriptor[0];
    }

    @Override
    public PolarisationModel getPolarisationModel() {
        return new L1cPolarisationModel();
    }

    @Override
    public boolean canSupplySnapshotData() {
        return productFile instanceof L1cScienceSmosFile;
    }

    @Override
    public boolean hasSnapshotInfo() {
        if (productFile instanceof L1cScienceSmosFile) {
            return ((L1cScienceSmosFile) productFile).hasSnapshotInfo();
        }
        return false;
    }

    @Override
    public SnapshotInfo getSnapshotInfo() {
        if (productFile instanceof L1cScienceSmosFile) {
            return ((L1cScienceSmosFile) productFile).getSnapshotInfo();
        }
        return null;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) throws IOException {
        if (productFile instanceof L1cScienceSmosFile) {
            final CompoundData data = ((L1cScienceSmosFile) productFile).getSnapshotData(snapshotIndex);
            final CompoundType compoundType = data.getType();
            final int memberCount = data.getMemberCount();
            final ArrayList<Object[]> list = new ArrayList<>(memberCount);

            for (int i = 0; i < memberCount; i++) {
                final Object[] entry = new Object[2];
                entry[0] = compoundType.getMemberName(i);

                final Type memberType = compoundType.getMemberType(i);
                if (memberType.isSimpleType()) {
                    try {
                        entry[1] = NumberUtils.getNumericMember(data, i);
                    } catch (IOException e) {
                        entry[1] = "Failed reading data";
                    }
                    list.add(entry);
                } else {
                    if ("Snapshot_Time".equals(compoundType.getMemberName(i))) {
                        try {
                            final Date date = DateTimeUtils.cfiDateToUtc(data);
                            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz",
                                                                                     Locale.ENGLISH);
                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                            entry[1] = dateFormat.format(date);
                        } catch (IOException e) {
                            entry[1] = "Failed reading data";
                        }
                        list.add(entry);
                    }
                }
            }

            return list.toArray(new Object[2][list.size()]);
        }
        return new Object[0][];
    }

    private HashMap<String, Integer> getRawDataMemberNamesMap(L1cSmosFile smosFile) {
        final CompoundType btDataType = smosFile.getBtDataType();
        final CompoundMember[] members = btDataType.getMembers();
        final HashMap<String, Integer> memberNamesMap = new HashMap<>();
        for (int i = 0; i < members.length; i++) {
            CompoundMember member = members[i];
            memberNamesMap.put(member.getName(), i);
        }
        return memberNamesMap;
    }

    private static ProductFile createProductFile(VirtualDir virtualDir) throws IOException {
        String listPath = "";
        String[] list = virtualDir.list(listPath);
        if (list.length == 1) {
            listPath = list[0] + "/";
        }
        list = virtualDir.list(listPath);

        String fileName = null;
        for (String listEntry : list) {
            if (listEntry.contains(".hdr") || listEntry.contains(".HDR")) {
                fileName = listEntry;
                break;
            }
        }

        if (fileName == null) {
            return null;
        }

        final File hdrFile = virtualDir.getFile(listPath + fileName);
        File dblFile = FileUtils.exchangeExtension(hdrFile, ".DBL");
        dblFile = virtualDir.getFile(listPath + dblFile.getName());

        return createProductFileImplementation(dblFile);
    }

    SmosProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected final Product readProductNodesImpl() throws IOException {
        synchronized (this) {
            final File inputFile = getInputFile();
            final String inputFileName = inputFile.getName();
            if (SmosUtils.isDblFileName(inputFileName)) {
                productFile = createProductFile(inputFile);
            } else {
                productFile = createProductFile(getInputVirtualDir());
            }
            if (productFile == null) {
                throw new IOException(
                        MessageFormat.format("File ''{0}'': unknown/unsupported SMOS data format.", inputFile));
            }
            final Product product = productFile.createProduct();
            if (virtualDir != null && virtualDir.isCompressed()) {
                final String path = virtualDir.getBasePath();
                product.setFileLocation(new File(path));
            } else {
                product.setFileLocation(productFile.getDataFile());
            }
            if (productFile instanceof SmosFile) {
                addLandSeaMask(product);
            }
            return product;
        }
    }


    @Override
    protected final void readBandRasterDataImpl(int sourceOffsetX,
                                                int sourceOffsetY,
                                                int sourceWidth,
                                                int sourceHeight,
                                                int sourceStepX,
                                                int sourceStepY,
                                                Band targetBand,
                                                int targetOffsetX,
                                                int targetOffsetY,
                                                int targetWidth,
                                                int targetHeight,
                                                ProductData targetBuffer,
                                                ProgressMonitor pm) {
        synchronized (this) {
            final RenderedImage image = targetBand.getSourceImage();
            final Raster data = image.getData(new Rectangle(targetOffsetX, targetOffsetY, targetWidth, targetHeight));

            data.getDataElements(targetOffsetX, targetOffsetY, targetWidth, targetHeight, targetBuffer.getElems());
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            productFile.close();
            if (virtualDir != null) {
                virtualDir.close();
            }
            super.close();
        }
    }

    private VirtualDir getInputVirtualDir() {
        File inputFile = getInputFile();

        if (!SmosUtils.isCompressedFile(inputFile)) {
            inputFile = inputFile.getParentFile();
        }

        virtualDir = VirtualDir.create(inputFile);
        if (virtualDir == null) {
            throw new IllegalArgumentException(MessageFormat.format("Illegal input: {0}", inputFile));
        }

        return virtualDir;
    }

    private void addLandSeaMask(Product product) {
        final BandDescriptor descriptor = Dddb.getInstance().getBandDescriptors(
                LSMASK_SCHEMA_NAME).getMember(SmosConstants.LAND_SEA_MASK_NAME);

        final Band band = product.addBand(descriptor.getBandName(), ProductData.TYPE_UINT8);

        band.setScalingOffset(descriptor.getScalingOffset());
        band.setScalingFactor(descriptor.getScalingFactor());
        if (descriptor.hasFillValue()) {
            band.setNoDataValueUsed(true);
            band.setNoDataValue(descriptor.getFillValue());
        }
        if (!descriptor.getValidPixelExpression().isEmpty()) {
            band.setValidPixelExpression(descriptor.getValidPixelExpression());
        }
        if (!descriptor.getUnit().isEmpty()) {
            band.setUnit(descriptor.getUnit());
        }
        if (!descriptor.getDescription().isEmpty()) {
            band.setDescription(descriptor.getDescription());
        }
        if (descriptor.getFlagDescriptors() != null) {
            ProductHelper.addFlagsAndMasks(product, band, descriptor.getFlagCodingName(),
                                           descriptor.getFlagDescriptors());
        }

        band.setSourceImage(SmosLsMask.getInstance().getMultiLevelImage());
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }

    private static ProductFile createProductFileImplementation(File file) throws IOException {
        final File hdrFile = FileUtils.exchangeExtension(file, ".HDR");
        final File dblFile = FileUtils.exchangeExtension(file, ".DBL");

        final DataFormat format = Dddb.getInstance().getDataFormat(hdrFile);
        if (format == null) {
            return null;
        }

        final EEFilePair eeFilePair = new EEFilePair(hdrFile, dblFile);
        final String formatName = format.getName();
        final DataContext context = format.createContext(dblFile, "r");

        if (SmosUtils.isBrowseFormat(formatName)) {
            return new L1cBrowseSmosFile(eeFilePair, context);
        } else if (SmosUtils.isDualPolScienceFormat(formatName) ||
                SmosUtils.isFullPolScienceFormat(formatName)) {
            return new L1cScienceSmosFile(eeFilePair, context);
        } else if (SmosUtils.isSmUserFormat(formatName)) {
            return new SmUserSmosFile(eeFilePair, context);
        } else if (SmosUtils.isOsUserFormat(formatName) ||
                SmosUtils.isOsAnalysisFormat(formatName) ||
                SmosUtils.isSmAnalysisFormat(formatName) ||
                SmosUtils.isAuxECMWFType(formatName)) {
            return new SmosFile(eeFilePair, context);
        } else if (SmosUtils.isDffLaiFormat(formatName)) {
            return new DffgLaiFile(eeFilePair, context);
        } else if (SmosUtils.isDffSnoFormat(formatName)) {
            return new DffgSnoFile(eeFilePair, context);
        } else if (SmosUtils.isVTecFormat(formatName)) {
            return new VTecFile(eeFilePair, context);
        } else if (SmosUtils.isLsMaskFormat(formatName)) {
            return new GlobalSmosFile(eeFilePair, context);
        } else if (SmosUtils.isDggFloFormat(formatName) ||
                SmosUtils.isDggRfiFormat(formatName) ||
                SmosUtils.isDggRouFormat(formatName) ||
                SmosUtils.isDggTfoFormat(formatName) ||
                SmosUtils.isDggTlvFormat(formatName)) {
            return new AuxiliaryFile(eeFilePair, context);
        }

        return null;
    }
}
