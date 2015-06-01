package org.esa.smos.gui.snapshot;

import org.esa.smos.gui.snapshot.SnapshotInfoTopComponent;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.*;

public class SnapshotInfoToolViewTest {

    @Test
    public void testIsXPolarized() {
        Band band = createband("BT_Value_X");
        assertTrue(SnapshotInfoTopComponent.isXPolarized(band));

        band = createband("Faraday_Rotation_Angle_X");
        assertTrue(SnapshotInfoTopComponent.isXPolarized(band));

        band = createband("Faraday_Rotation_Angle_Y");
        assertFalse(SnapshotInfoTopComponent.isXPolarized(band));

        band = createband("BT_Value_V");
        assertFalse(SnapshotInfoTopComponent.isXPolarized(band));
    }

    @Test
    public void testIsYPolarized() {
        Band band = createband("Geometric_Rotation_Angle_Y");
        assertTrue(SnapshotInfoTopComponent.isYPolarized(band));

        band = createband("Footprint_Axis_1_Y");
        assertTrue(SnapshotInfoTopComponent.isYPolarized(band));

        band = createband("Footprint_Axis_2_XY");
        assertFalse(SnapshotInfoTopComponent.isYPolarized(band));

        band = createband("BT_Value_HV_Real");
        assertFalse(SnapshotInfoTopComponent.isYPolarized(band));
    }

    @Test
    public void testIsXYPolarized() {
        Band band = createband("BT_Value_XY_Real");
        assertTrue(SnapshotInfoTopComponent.isXYPolarized(band));

        band = createband("Pixel_Radiometric_accuracy_XY");
        assertTrue(SnapshotInfoTopComponent.isXYPolarized(band));

        band = createband("Azimuth_Angle_X");
        assertFalse(SnapshotInfoTopComponent.isXYPolarized(band));

        band = createband("BT_Value_H");
        assertFalse(SnapshotInfoTopComponent.isXYPolarized(band));
    }

    private Band createband(String name) {
        return new Band(name, ProductData.TYPE_INT8, 4, 4);
    }
}
