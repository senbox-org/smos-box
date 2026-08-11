package org.esa.smos.dataio.smos;

import com.bc.ceres.binio.*;
import com.bc.ceres.multilevel.MultiLevelImage;
import com.bc.ceres.multilevel.MultiLevelSource;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.Family;
import org.esa.smos.dataio.smos.provider.AbstractValueProvider;
import org.esa.smos.dataio.smos.provider.ValueProvider;
import org.esa.smos.dgg.SmosDgg;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.awt.*;
import java.awt.geom.Area;
import java.io.IOException;

public class AuxWindsFile extends ExplorerFile {

    private final DggFile.PlainGridPointList windsGridPointList;
    private final DggFile.PlainGridPointList windsMapPointList;

    private Area area;
    private GridPointInfo gridPointInfo;

    public AuxWindsFile(EEFilePair eeFilePair, DataContext dataContext) throws IOException {
        super(eeFilePair, dataContext);

        final CompoundData dataBlock = getDataBlock();
        final SequenceData windsGlobalSequence = dataBlock.getSequence("Winds_Global");
        final CompoundData windsGlobal = windsGlobalSequence.getCompound(0);

        // @todo tb - do we need this? 2026-05-08
        //long gridType = windsGlobal.getUInt("Grid_Type");

        final SequenceData windsGridSequence = windsGlobal.getSequence("Winds_Grid");
        windsGridPointList = new DggFile.PlainGridPointList(windsGridSequence);

        final SequenceData windsMapSequence = windsGlobal.getSequence("Winds_Map_List");
        windsMapPointList = new DggFile.PlainGridPointList(windsMapSequence);

        area = null;
        gridPointInfo = null;
    }

    @Override
    public Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getDataFile());
        final String productType = getProductType();
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDataFile());
        product.setPreferredTileSize(512, 504);

        ProductHelper.addMetadata(product.getMetadataRoot(), this);
        product.setSceneGeoCoding(ProductHelper.createGeoCoding(dimension));
        addBands(product);
        addAncilliaryBands(product);
        setTimes(product);

        return product;
    }

    @Override
    public Area getArea() {
        if (area == null) {
            try {
                area = DggUtils.computeArea(windsGridPointList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return area;
    }

    protected void addBands(Product product) {
        final String formatName = getDataFormat().getName();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(formatName);

        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor);
            }
        }
    }

    protected void addBand(Product product, BandDescriptor descriptor) {
        final CompoundType compoundType = getCompoundType(descriptor);
        addBand(product, descriptor, compoundType);
    }

    private CompoundType getCompoundType(BandDescriptor descriptor) {
        CompoundType compoundType;
        if (descriptor.isGridPointData()) {
            compoundType = windsGridPointList.getCompoundType();
        } else {
            compoundType = windsMapPointList.getCompoundType();
        }
        return compoundType;
    }

    protected final void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
        if (!descriptor.isVisible()) {
            return;
        }

        final int memberIndex = compoundType.getMemberIndex(descriptor.getMemberName());
        if (memberIndex >= 0) {
            final CompoundMember member = compoundType.getMember(memberIndex);

            final int dataType = ProductHelper.getDataType(member.getType());
            final Band band = product.addBand(descriptor.getBandName(), dataType);

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

            final ValueProvider valueProvider = createValueProvider(descriptor);
            band.setSourceImage(createSourceImage(band, valueProvider));
            band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
        }
    }

    protected AbstractValueProvider createValueProvider(BandDescriptor descriptor) {
        final CompoundType compoundType = getCompoundType(descriptor);
        final int memberIndex = compoundType.getMemberIndex(descriptor.getMemberName());

        return new WindsValueProvider(this, memberIndex, !descriptor.isGridPointData());
    }

    protected MultiLevelImage createSourceImage(final Band band, final ValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(Band band, ValueProvider valueProvider) {
        return new SmosMultiLevelSource(band, valueProvider);
    }

    private void setTimes(Product product) {
        final String pattern = "'UTC='yyyy-MM-dd'T'HH:mm:ss";
        try {
            final Document document = getDocument();
            final Namespace namespace = document.getRootElement().getNamespace();
            final Element validityPeriod = getElement(document.getRootElement(), "Validity_Period");
            final String validityStart = validityPeriod.getChildText("Validity_Start", namespace);
            final String validityStop = validityPeriod.getChildText("Validity_Stop", namespace);
            product.setStartTime(ProductData.UTC.parse(validityStart, pattern));
            product.setEndTime(ProductData.UTC.parse(validityStop, pattern));
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getGridPointIndex(int seqnum) {
        if (gridPointInfo == null) {
            try {
                gridPointInfo = createGridPointInfo();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return gridPointInfo.getGridPointIndex(seqnum);
    }

    private GridPointInfo createGridPointInfo() throws IOException {
        final SmosDgg smosDgg = SmosDgg.getInstance();
        final int elementCount = windsGridPointList.getElementCount();
        int[] seqNumArray = new int[elementCount];

        int seqnumMin = Integer.MAX_VALUE;
        int seqnumMax = Integer.MIN_VALUE;
        for  (int i = 0; i < elementCount; i++) {
            final CompoundData windsCommon = windsGridPointList.getCompound(i);
            final float longitude = windsCommon.getFloat("Longitude");
            final float latitude = windsCommon.getFloat("Latitude");

            int seqnum = smosDgg.getSeqnum(latitude, longitude);
            if (seqnum < seqnumMin) {
                seqnumMin = seqnum;
            }
            if (seqnum > seqnumMax) {
                seqnumMax = seqnum;
            }
            seqNumArray[i] = seqnum;
        }
        final GridPointInfo gpInfpo = new GridPointInfo(seqnumMin, seqnumMax);
        gpInfpo.setSequenceNumbers(seqNumArray);
        return gpInfpo;
    }

    public CompoundData getCompound(int gridPointIndex, boolean mapPoint) {
        try {
            if (mapPoint) {
                return windsMapPointList.getCompound(gridPointIndex);
            } else {
                return windsGridPointList.getCompound(gridPointIndex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
