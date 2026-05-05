package org.esa.smos.dataio.smos;

import org.jdom2.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class L1CSmosFileTest {

    @Test
    public void testGetRadimetricAccuracyScale() {
        final Element specificProductHeader = new Element("specificProductHeader");
        Element scaleElement = new Element("Radiometric_Accuracy_Scale");
        scaleElement.setText("1.87");
        specificProductHeader.addContent(scaleElement);

        Double accuracyScale = L1cSmosFile.getRadiometricAccuracyScale(specificProductHeader, null);
        assertEquals(1.87, accuracyScale, 1e-8);
        assertTrue(specificProductHeader.removeContent(scaleElement));

        scaleElement = new Element("Radiometric_Resolution_Scale");
        scaleElement.setText("0.34");
        specificProductHeader.addContent(scaleElement);

        accuracyScale = L1cSmosFile.getRadiometricAccuracyScale(specificProductHeader, null);
        assertEquals(0.34, accuracyScale, 1e-8);
    }
}
