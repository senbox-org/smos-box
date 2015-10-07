package org.esa.smos.gui;

import org.esa.snap.framework.ui.FileChooserFactory;

import javax.swing.JFileChooser;
import java.io.File;

public class DefaultChooserFactory implements ChooserFactory {

    @Override
    public JFileChooser createChooser(File file) {
        return FileChooserFactory.getInstance().createDirChooser(file);
    }
}
