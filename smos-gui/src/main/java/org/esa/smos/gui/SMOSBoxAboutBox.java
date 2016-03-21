package org.esa.smos.gui;

import org.esa.snap.rcp.about.AboutBox;
import org.esa.snap.rcp.util.BrowserUtils;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author muhammad.bc
 */
@AboutBox(displayName = "SMOS-Box", position = 60)
public class SMOSBoxAboutBox extends JPanel {

    private final static String releaseNotesHTTP = "https://github.com/senbox-org/smos-box/blob/master/ReleaseNotes.md";

    public SMOSBoxAboutBox() {
        super(new BorderLayout(4, 4));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        ModuleInfo moduleInfo = Modules.getDefault().ownerOf(SMOSBoxAboutBox.class);
        ImageIcon imageIcon = new ImageIcon(SMOSBoxAboutBox.class.getResource("smos_aboutbox.jpg"));
        JLabel label = new JLabel(imageIcon);
        add(label, BorderLayout.CENTER);
        add(createVersionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createVersionPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        final ModuleInfo moduleInfo = Modules.getDefault().ownerOf(SMOSBoxAboutBox.class);
        panel.add(new JLabel("<html><b>SMOS-Box version " + moduleInfo.getImplementationVersion() + "</b>",
                             SwingConstants.RIGHT));
        final URI releaseNotesURI = getReleaseNotesURI();
        if (releaseNotesURI != null) {
            final JLabel releaseNoteLabel = new JLabel("<html><a href=\"" + releaseNotesURI.toString() + "\">Release Notes</a>",
                                                       SwingConstants.RIGHT);
            releaseNoteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            releaseNoteLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(releaseNotesHTTP));
            panel.add(releaseNoteLabel);
        }
        return panel;
    }

    private URI getReleaseNotesURI() {
        try {
            return new URI(releaseNotesHTTP);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
