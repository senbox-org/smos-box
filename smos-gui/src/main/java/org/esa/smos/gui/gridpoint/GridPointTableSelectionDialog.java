package org.esa.smos.gui.gridpoint;


import org.esa.snap.framework.ui.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

class GridPointTableSelectionDialog extends JDialog {

    private final HashMap<String, Boolean> columnSelectionMap;
    private boolean isCanceled;

    static GridPointTableSelectionDialog create(Frame frame, ColumnProperty[] columnProperties) {
        final GridPointTableSelectionDialog dialog = new GridPointTableSelectionDialog(frame, columnProperties);

        UIUtils.centerComponent(dialog, frame);

        return dialog;
    }

    HashMap<String, Boolean> getSelection() {
        return columnSelectionMap;
    }

    boolean isCanceled() {
        return isCanceled;
    }

    private GridPointTableSelectionDialog(Frame owner, ColumnProperty[] columnProperties) {
        super(owner, "Choose Colums to Display", true);
        setResizable(false);

        columnSelectionMap = new HashMap<>(columnProperties.length);
        for (ColumnProperty columnProperty : columnProperties) {
            columnSelectionMap.put(columnProperty.getColumnName(), columnProperty.isVisible());
        }

        createGui(columnProperties);
    }

    private void createGui(ColumnProperty[] columnProperties) {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        panel.add(new JLabel("Select the columns you want to display for this table"), gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(createColumnNamesPane(columnProperties), gbc);

        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(createButtonPanel(), gbc);

        add(panel);

        pack();
    }

    private ScrollPane createColumnNamesPane(ColumnProperty[] columnProperties) {
        final ScrollPane scrollPane = new ScrollPane();
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        for (final ColumnProperty columnProperty : columnProperties) {
            final JCheckBox checkBox = new JCheckBox(columnProperty.getColumnName(), columnProperty.isVisible());
            checkBox.addActionListener(this::toggleColumnState);
            panel.add(checkBox, gbc);
            gbc.gridy++;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        scrollPane.add(panel, gbc);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;

        final JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> this.onOk());
        buttonPanel.add(okButton, gbc);
        gbc.gridx = 1;
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> this.onCancel());
        buttonPanel.add(cancelButton, gbc);

        return buttonPanel;
    }

    private void toggleColumnState(ActionEvent e) {
        final boolean selected = ((JCheckBox) e.getSource()).isSelected();
        final String columnName = e.getActionCommand();

        columnSelectionMap.put(columnName, selected);
    }

    private void onOk() {
        isCanceled = false;
        closeDialog();
    }

    private void onCancel() {
        isCanceled = true;
        closeDialog();
    }

    private void closeDialog() {
        setVisible(false);
        dispose();
    }
}

