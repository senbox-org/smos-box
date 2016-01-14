package org.esa.smos.ee2netcdf.reader;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.smos.ObservationPointList;
import org.esa.smos.Point;
import org.esa.smos.dataio.smos.*;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import org.esa.smos.dgg.SmosDgg;
import org.esa.smos.ee2netcdf.AttributeEntry;
import org.esa.smos.ee2netcdf.ExporterUtils;
import org.esa.smos.ee2netcdf.MetadataUtils;
import org.esa.smos.lsmask.SmosLsMask;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"SimplifiableIfStatement", "SynchronizeOnNonFinalField"})
public class NetcdfProductReader extends SmosReader {

    private static final String SENSING_TIMES_PATTERN = "'UTC='yyyy-MM-dd'T'HH:mm:ss";
    private static final String LSMASK_SCHEMA_NAME = "DBL_SM_XXXX_AUX_LSMASK_0200";

    private NetcdfFile netcdfFile;
    private ProductTypeSupport typeSupport;
    private GridPointInfo gridPointInfo;
    private final HashMap<String, AbstractValueProvider> valueProviderMap;

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be <code>null</code> for internal reader
     *                     implementations
     */
    protected NetcdfProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);

        valueProviderMap = new HashMap<>();
    }

    @Override
    public boolean canSupplyGridPointBtData() {
        if (typeSupport == null) {
            return false;
        }
        return typeSupport.canSupplyGridPointBtData();
    }

    @Override
    public boolean canSupplyFullPolData() {
        if (typeSupport == null) {
            return false;
        }
        return typeSupport.canSupplyFullPolData();
    }

    @Override
    public GridPointBtDataset getBtData(int gridPointIndex) throws IOException {
        if (typeSupport == null) {
            return null;
        }
        return typeSupport.getBtData(gridPointIndex);
    }

    @Override
    public int getGridPointIndex(int gridPointId) {
        if (gridPointInfo == null) {
            return -1;
        }
        return gridPointInfo.getGridPointIndex(gridPointId);
    }

    @Override
    public int getGridPointId(int levelPixelX, int levelPixelY, int currentLevel) {
        // @todo 3 tb/tb duplicated from SmosProductReader - refactor 2015-06-30
        final MultiLevelImage levelImage = SmosDgg.getInstance().getMultiLevelImage();
        final RenderedImage image = levelImage.getImage(currentLevel);
        final Raster data = image.getData(new Rectangle(levelPixelX, levelPixelY, 1, 1));
        return data.getSample(levelPixelX, levelPixelY, 0);
    }

    @Override
    public String[] getRawDataTableNames() {
        if (typeSupport == null) {
            return null;
        }

        return typeSupport.getRawDataTableNames();
    }

    @Override
    public FlagDescriptor[] getBtFlagDescriptors() {
        if (typeSupport == null) {
            return null;
        }

        return typeSupport.getBtFlagDescriptors();
    }

    @Override
    public PolarisationModel getPolarisationModel() {
        if (typeSupport == null) {
            return null;
        }

        return typeSupport.getPolarisationModel();
    }

    @Override
    public boolean canSupplySnapshotData() {
        if (typeSupport == null) {
            return false;
        }

        return typeSupport.canSupplySnapshotData();
    }

    @Override
    public boolean hasSnapshotInfo() {
        if (typeSupport == null) {
            return false;
        }

        return typeSupport.hasSnapshotInfo();
    }

    @Override
    public SnapshotInfo getSnapshotInfo() {
        if (typeSupport == null) {
            return null;
        }

        try {
            return typeSupport.getSnapshotInfo();
        } catch (IOException e) {
            // @todo 2 tb/tb handle this 2015-10-13
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object[][] getSnapshotData(int snapshotIndex) throws IOException {
        if (typeSupport == null) {
            return new Object[0][];
        }

        return typeSupport.getSnapshotData(snapshotIndex);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        Product product;

        final File inputFile = getInputFile();
        netcdfFile = NetcdfFileOpener.open(inputFile.getAbsolutePath());
        if (netcdfFile == null) {
            throw new IOException("Unable to read file");
        }

        synchronized (netcdfFile) {
            final ArrayCache arrayCache = new ArrayCache(netcdfFile);

            final String productType = getProductTypeString();

            typeSupport = ProductTypeSupportFactory.get(productType, netcdfFile);

            product = ProductHelper.createProduct(inputFile, productType);
            product.setProductReader(this);
            addSensingTimes(product);
            addMetadata(product);

            final Area area = calculateArea(typeSupport);
            gridPointInfo = calculateGridPointInfo();

            final String schemaDescription = getSchemaDescription(netcdfFile);
            final Dddb dddb = Dddb.getInstance();
            final Family<BandDescriptor> bandDescriptors = dddb.getBandDescriptors(schemaDescription);
            if (bandDescriptors == null) {
                throw new IOException("Unsupported file schema: '" + schemaDescription + "`");
            }

            typeSupport.initialize(bandDescriptors);

            for (final BandDescriptor descriptor : bandDescriptors.asList()) {
                if (!descriptor.isVisible()) {
                    continue;
                }

                final String eeVariableName = dddb.getEEVariableName(descriptor.getMemberName(), schemaDescription);
                final String ncVariableName = ExporterUtils.ensureNetCDFName(eeVariableName);
                final Variable variable = netcdfFile.findVariable(null, ncVariableName);
                if (variable == null) {
                    continue;
                }

                final int rasterDataType = DataTypeUtils.getRasterDataType(variable);
                final Band band = product.addBand(descriptor.getBandName(), rasterDataType);

                typeSupport.setScalingAndOffset(band, descriptor);
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
                    ProductHelper.addFlagsAndMasks(product, band,
                            descriptor.getFlagCodingName(),
                            descriptor.getFlagDescriptors());
                }

                final AbstractValueProvider valueProvider = typeSupport.createValueProvider(arrayCache, ncVariableName, descriptor, area, gridPointInfo);
                final SmosMultiLevelSource smosMultiLevelSource = new SmosMultiLevelSource(band, valueProvider);
                final DefaultMultiLevelImage defaultMultiLevelImage = new DefaultMultiLevelImage(smosMultiLevelSource);
                band.setSourceImage(defaultMultiLevelImage);
                band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));

                valueProviderMap.put(descriptor.getBandName(), valueProvider);
            }

            addLandSeaMask(product);

            typeSupport.createAdditionalBands(product, area, bandDescriptors, schemaDescription, valueProviderMap);
            typeSupport.setArrayCache(arrayCache);
            typeSupport.setGridPointInfo(gridPointInfo);
        }

        return product;
    }

    private String getProductTypeString() throws IOException {
        final Attribute fileTypeAttrbute = netcdfFile.findGlobalAttribute("Fixed_Header:File_Type");
        if (fileTypeAttrbute == null) {
            throw new IOException("Required attribute `Fixed_Header:File_Type` not found");
        }
        return fileTypeAttrbute.getStringValue();
    }

    private void addSensingTimes(Product product) throws IOException {
        final Attribute startAttribute = netcdfFile.findGlobalAttribute("Fixed_Header:Validity_Period:Validity_Start");
        final Attribute stopAttribute = netcdfFile.findGlobalAttribute("Fixed_Header:Validity_Period:Validity_Stop");
        if (startAttribute == null || stopAttribute == null) {
            throw new IOException("Sensing times metadata not present");
        }

        final String sensingStartUTC = startAttribute.getStringValue();
        final String sensingStopUTC = stopAttribute.getStringValue();

        try {
            product.setStartTime(ProductData.UTC.parse(sensingStartUTC, SENSING_TIMES_PATTERN));
            product.setEndTime(ProductData.UTC.parse(sensingStopUTC, SENSING_TIMES_PATTERN));
        } catch (ParseException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            e.printStackTrace();
        }

    }

    private GridPointInfo calculateGridPointInfo() throws IOException {
        final Variable gridPointIdVariable = netcdfFile.findVariable(null, "Grid_Point_ID");
        if (gridPointIdVariable == null) {
            throw new IOException("Required variable 'Grid_Point_ID' missing, Unable to open product");
        }
        final Array gridPointIdArray = gridPointIdVariable.read();
        final int[] shape = gridPointIdArray.getShape();

        int minSeqNum = Integer.MAX_VALUE;
        int maxSeqNum = Integer.MIN_VALUE;
        final int[] seqNumbers = new int[shape[0]];
        for (int i = 0; i < shape[0]; i++) {
            final int gridPointId = gridPointIdArray.getInt(i);
            final int seqnum = SmosDgg.gridPointIdToSeqnum(gridPointId);
            seqNumbers[i] = seqnum;
            if (seqnum < minSeqNum) {
                minSeqNum = seqnum;
            } else if (seqnum > maxSeqNum) {
                maxSeqNum = seqnum;
            }
        }

        final GridPointInfo gridPointInfo = new GridPointInfo(minSeqNum, maxSeqNum);
        gridPointInfo.setSequenceNumbers(seqNumbers);
        return gridPointInfo;
    }

    private Area calculateArea(ProductTypeSupport productTypeSupport) throws IOException {
        final Variable latitude = netcdfFile.findVariable(null, productTypeSupport.getLatitudeBandName());
        final Variable longitude = netcdfFile.findVariable(null, productTypeSupport.getLongitudeBandName());
        if (latitude == null || longitude == null) {
            throw new IOException("Missing geo location variables");
        }

        final Array latitudeArray = latitude.read();
        final Array longitudeArray = longitude.read();

        final int[] shape = longitudeArray.getShape();
        final Point[] pointArray = new Point[shape[0]];
        for (int i = 0; i < shape[0]; i++) {
            pointArray[i] = new Point(longitudeArray.getDouble(i), latitudeArray.getDouble(i));
        }

        return DggUtils.computeArea(new ObservationPointList(pointArray));
    }

    private void addMetadata(Product product) {
        final List<Attribute> globalAttributes = netcdfFile.getGlobalAttributes();
        final List<AttributeEntry> attributeEntries = MetadataUtils.convertNetcdfAttributes(globalAttributes);
        MetadataUtils.parseMetadata(attributeEntries, product.getMetadataRoot());
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
        synchronized (netcdfFile) {
            final RenderedImage image = targetBand.getSourceImage();
            final Raster data = image.getData(new Rectangle(targetOffsetX, targetOffsetY, targetWidth, targetHeight));

            data.getDataElements(targetOffsetX, targetOffsetY, targetWidth, targetHeight, targetBuffer.getElems());
        }
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    private void addLandSeaMask(Product product) {
        final BandDescriptor descriptor = Dddb.getInstance().getBandDescriptors(LSMASK_SCHEMA_NAME).getMember(SmosConstants.LAND_SEA_MASK_NAME);

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

    static String getSchemaDescription(NetcdfFile netcdfFile) throws IOException {
        final Attribute schemaAttribute = netcdfFile.findGlobalAttribute("Variable_Header:Specific_Product_Header:Main_Info:Datablock_Schema");
        if (schemaAttribute == null) {
            throw new IOException("Schema attribuite not found.");
        }

        return schemaAttribute.getStringValue().substring(0, 27);
    }
}
