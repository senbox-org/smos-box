package org.esa.smos.dataio.smos;

import com.bc.ceres.binio.DataContext;
import org.esa.smos.EEFilePair;
import org.esa.smos.dataio.smos.dddb.BandDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.util.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;

/**
 * @author Ralf Quast
 */
class SmUserSmosFile extends SmosFile {

    private final double chi2Scale;

    SmUserSmosFile(EEFilePair eeFilePair, DataContext dataContext) throws IOException {
        super(eeFilePair, dataContext);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element specificProductHeader = getElement(document.getRootElement(), TAG_SPECIFIC_PRODUCT_HEADER);

        final String chi_2_scale = specificProductHeader.getChildText("Chi_2_Scale", namespace);
        if (StringUtils.isNotNullAndNotEmpty(chi_2_scale)) {
            chi2Scale = Double.valueOf(chi_2_scale);
        } else {
            chi2Scale = 1.0;
        }
    }

    @Override
    protected void setScaling(Band band, BandDescriptor descriptor) {
        final String memberName = descriptor.getMemberName();
        if ("Chi_2".equals(memberName)) {
            band.setScalingFactor(descriptor.getScalingFactor() * chi2Scale);
        } else {
            super.setScaling(band, descriptor);
        }
    }
}
