package org.esa.beam.dataio.smos.bufr;

import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import org.esa.beam.dataio.smos.CellGridOpImage;
import org.esa.beam.dataio.smos.CellValueProvider;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.jai.ResolutionLevel;

import java.awt.image.RenderedImage;

public class LightBufrMultiLevelSource extends AbstractMultiLevelSource {

    private final CellValueProvider valueProvider;
    private final RasterDataNode rasterDataNode;

    public LightBufrMultiLevelSource(MultiLevelModel multiLevelModel, CellValueProvider valueProvider,
                                     RasterDataNode rasterDataNode) {
        super(multiLevelModel);
        this.valueProvider = valueProvider;
        this.rasterDataNode = rasterDataNode;
    }

    public CellValueProvider getValueProvider() {
        return valueProvider;
    }

    @Override
    protected RenderedImage createImage(int level) {
        return new CellGridOpImage(valueProvider, rasterDataNode, getModel(), ResolutionLevel.create(getModel(), level));
    }
}
