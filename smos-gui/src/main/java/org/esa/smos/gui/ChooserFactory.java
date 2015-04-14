package org.esa.smos.gui;


import javax.swing.JFileChooser;
import java.io.File;

public interface ChooserFactory {

    JFileChooser createChooser(File file);
}
