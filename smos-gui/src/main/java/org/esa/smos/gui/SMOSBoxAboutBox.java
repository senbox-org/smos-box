package org.esa.smos.gui;

import com.bc.ceres.core.runtime.Version;
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

    private final static String releaseNotesUrlString = "https://senbox.atlassian.net/issues/?jql=project%20%3D%20SMOSTBX%20AND%20fixVersion%20%3D%20";

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
        panel.add(new JLabel("<html><b>SMOS-Box version " + moduleInfo.getImplementationVersion() + "</b>", SwingConstants.RIGHT));

        Version specVersion = Version.parseVersion(moduleInfo.getSpecificationVersion().toString());
        String versionString = String.format("%s.%s.%s", specVersion.getMajor(), specVersion.getMinor(), specVersion.getMicro());
        String changelogUrl = releaseNotesUrlString + versionString;
        final JLabel releaseNoteLabel = new JLabel("<html><a href=\"" + changelogUrl + "\">Release Notes</a>", SwingConstants.RIGHT);
        releaseNoteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        releaseNoteLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(changelogUrl));
        panel.add(releaseNoteLabel);
        return panel;
    }

}
