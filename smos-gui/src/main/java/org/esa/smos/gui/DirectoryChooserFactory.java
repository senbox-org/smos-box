package org.esa.smos.gui;


import org.esa.snap.ui.FileChooserFactory;

import javax.swing.JFileChooser;
import java.io.File;

public class DirectoryChooserFactory implements ChooserFactory{

    @Override
    public JFileChooser createChooser(File file) {
        return FileChooserFactory.getInstance().createDirChooser(file);
    }
}
