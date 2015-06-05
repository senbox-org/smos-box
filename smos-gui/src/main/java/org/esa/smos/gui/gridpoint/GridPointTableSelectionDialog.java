package org.esa.smos.gui.gridpoint;


import org.esa.snap.framework.ui.UIUtils;

import javax.swing.*;
import java.awt.*;

public class GridPointTableSelectionDialog {

    static JDialog create(Frame frame) {
        final JDialog dialog = new JDialog(frame, "Choose Colums to Display", true);
        dialog.setResizable(false);

        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        panel.add(new JLabel("Select the columns you want to display for this table"), gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPreferredSize(new Dimension(400, 250));
        panel.add(scrollPane, gbc);

        gbc.gridy = 2;
        panel.add(createButtonPanel(), gbc);

        dialog.add(panel);

        dialog.pack();
        UIUtils.centerComponent(dialog, frame);

        return dialog;
    }

    private static JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;

        buttonPanel.add(new JButton("OK"), gbc);
        gbc.gridx = 1;
        buttonPanel.add(new JButton("Cancel"), gbc);

        return buttonPanel;
    }
}

