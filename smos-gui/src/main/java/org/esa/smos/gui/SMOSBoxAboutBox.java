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

@AboutBox(displayName = "SMOS-Box", position = 60)
public class SMOSBoxAboutBox extends JPanel {

    private final static String releaseNotesUrlString = "https://senbox.atlassian.net/issues/?jql=project%20%3D%20SMOSTBX%20AND%20fixVersion%20%3D%20";

    public SMOSBoxAboutBox() {
        super(new BorderLayout(4, 4));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        ImageIcon imageIcon = new ImageIcon(SMOSBoxAboutBox.class.getResource("smos_aboutbox.jpg"));
        JLabel label = new JLabel(imageIcon);
        add(label, BorderLayout.CENTER);
        add(createVersionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createVersionPanel() {
        JLabel copyRightLabel = new JLabel("<html><b>© 2005-2020 Brockmann Consult GmbH and contributors</b>", SwingConstants.CENTER);

        final ModuleInfo moduleInfo = Modules.getDefault().ownerOf(SMOSBoxAboutBox.class);
        JLabel versionLabel = new JLabel("<html><b>SMOS-Box version " + moduleInfo.getImplementationVersion() + "</b>", SwingConstants.CENTER);

        Version specVersion = Version.parseVersion(moduleInfo.getSpecificationVersion().toString());
        String versionString = String.format("%s.%s.%s", specVersion.getMajor(), specVersion.getMinor(), specVersion.getMicro());
        String changelogUrl = releaseNotesUrlString + versionString;
        final JLabel releaseNoteLabel = new JLabel("<html><a href=\"" + changelogUrl + "\">Release Notes</a>", SwingConstants.CENTER);
        releaseNoteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        releaseNoteLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(changelogUrl));

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(copyRightLabel);
        mainPanel.add(versionLabel);
        mainPanel.add(releaseNoteLabel);
        return mainPanel;
    }

}
