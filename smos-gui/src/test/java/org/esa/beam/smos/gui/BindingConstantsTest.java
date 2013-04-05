package org.esa.beam.smos.gui;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class BindingConstantsTest {

    @Test
    public void testBindingConstants() {
       assertEquals("useSelectedProduct", BindingConstants.SELECTED_PRODUCT);
       assertEquals("sourceDirectory", BindingConstants.SOURCE_DIRECTORY);
       assertEquals("openFileDialog", BindingConstants.OPEN_FILE_DIALOG);
       assertEquals("geometry", BindingConstants.GEOMETRY);
       assertEquals("roiType", BindingConstants.ROI_TYPE);
       assertEquals("north", BindingConstants.NORTH);
       assertEquals("south", BindingConstants.SOUTH);
       assertEquals("east", BindingConstants.EAST);
       assertEquals("west", BindingConstants.WEST);
    }

    @Test
    public void testRoiTypeConstants() {
        assertEquals(0, BindingConstants.ROI_TYPE_PRODUCT);
        assertEquals(1, BindingConstants.ROI_TYPE_GEOMETRY);
        assertEquals(2, BindingConstants.ROI_TYPE_AREA);
    }
}
