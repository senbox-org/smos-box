package org.esa.smos.gui.gridpoint;


import org.esa.snap.framework.ui.UIUtils;

import javax.swing.*;
import java.awt.*;

public class GridPointTableSelectionDialog {

    static JDialog create(Frame frame) {
        final JDialog dialog = new JDialog(frame, "Choose Colums to Display", true);

        dialog.add(new JLabel("Select the columns you want to display for this table"));

        dialog.pack();
        UIUtils.centerComponent(dialog, frame);

        return dialog;
    }
}

