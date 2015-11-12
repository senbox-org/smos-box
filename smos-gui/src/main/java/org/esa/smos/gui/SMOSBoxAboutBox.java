package org.esa.smos.gui;

import org.esa.snap.rcp.about.AboutBox;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Image;

/**
 * @author muhammad.bc
 */
@AboutBox(displayName = "SMOS-BOX", position = 60)
public class SMOSBoxAboutBox extends JPanel {
    public SMOSBoxAboutBox() {
        super(new BorderLayout(4, 4));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        ModuleInfo moduleInfo = Modules.getDefault().ownerOf(SMOSBoxAboutBox.class);
        ImageIcon imageIcon = new ImageIcon(SMOSBoxAboutBox.class.getResource("smos_aboutbox.jpg"));
        JLabel label = new JLabel(imageIcon);
        add(label, BorderLayout.CENTER);
        add(new JLabel("<html><b>SMOS-BOX version " + moduleInfo.getImplementationVersion() + "</b>", SwingConstants.RIGHT), BorderLayout.SOUTH);
    }
}
