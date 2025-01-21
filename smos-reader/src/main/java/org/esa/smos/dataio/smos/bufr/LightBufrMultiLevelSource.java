package org.esa.smos.dataio.smos.bufr;

import com.bc.ceres.multilevel.MultiLevelModel;
import com.bc.ceres.multilevel.support.AbstractMultiLevelSource;
import org.esa.smos.dataio.smos.CellGridOpImage;
import org.esa.smos.dataio.smos.CellValueProvider;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.image.ResolutionLevel;

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
