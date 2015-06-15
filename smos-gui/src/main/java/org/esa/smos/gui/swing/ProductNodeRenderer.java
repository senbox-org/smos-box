package org.esa.smos.gui.swing;

import com.vividsolutions.jts.geom.Geometry;
import org.esa.snap.framework.datamodel.ProductNode;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;

class ProductNodeRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (!(component instanceof JLabel)) {
            return component;
        }
        final JLabel label = (JLabel) component;
        if (value instanceof ProductNode) {
            label.setText(((ProductNode) value).getDisplayName());
        } else if (value instanceof Geometry) {
            label.setText(((Geometry) value).toText().substring(0, 48).concat(" ..."));
        } else {
            label.setText("");
        }
        return label;
    }
}
