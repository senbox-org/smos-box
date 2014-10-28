package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.selection.SelectionManager;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.RegionBoundsInputUI;
import org.esa.beam.smos.ee2netcdf.EEToNetCDFExporterOp;
import org.esa.beam.smos.ee2netcdf.ExportParameter;
import org.esa.beam.smos.gui.BindingConstants;
import org.esa.beam.smos.gui.DefaultChooserFactory;
import org.esa.beam.smos.gui.DirectoryChooserFactory;
import org.esa.beam.smos.gui.GuiHelper;
import org.esa.beam.smos.gui.ProductChangeAwareDialog;
import org.esa.beam.util.io.WildcardMatcher;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Ralf Quast
 */
class SmosEEToNetCDFExportDialog extends ProductChangeAwareDialog {

    private final AppContext appContext;
    private final ExportParameter exportParameter;
    private final PropertySet propertySet;
    private final BindingContext bindingContext;
    private final GeometryListener geometryListener;
    private final ProductSelectionListener productSelectionListener;

    private final JPanel ioParametersPanel;
    private final JTabbedPane form;

    SmosEEToNetCDFExportDialog(AppContext appContext, String helpID) {
        super(appContext.getApplicationWindow(), "Convert SMOS EE Files to NetCDF-4", ID_OK | ID_CLOSE | ID_HELP, helpID);
        this.appContext = appContext;

        exportParameter = new ExportParameter();

        propertySet = PropertyContainer.createObjectBacked(exportParameter, new ParameterDescriptorFactory());
        propertySet.setDefaultValues();
        bindingContext = new BindingContext(propertySet);

        ioParametersPanel = GuiHelper.createPanelWithBoxLayout();
        ioParametersPanel.add(createSourceProductsSelector());
        ioParametersPanel.add(createRegionSelector());
        ioParametersPanel.add(createTargetDirSelector());
        form = new JTabbedPane();
        form.add("I/O Parameters", ioParametersPanel);
        /*
        if (bindingContext.getPropertySet().getProperties().length > 0) {
            final PropertyPane parametersPane = new PropertyPane(bindingContext);
            final JPanel parametersPanel = parametersPane.createPanel();
            parametersPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
            form.add("Processing Parameters", new JScrollPane(parametersPanel));
            //updateSourceProduct();
        }
        */
        setContent(form);

        bindingContext.bindEnabledState(BindingConstants.REGION, true, BindingConstants.ROI_TYPE,
                                        BindingConstants.ROI_TYPE_GEOMETRY);

        try {
            init(propertySet);
        } catch (ValidationException e) {
            throw new IllegalStateException(e.getMessage());
        }

        final ProductManager productManager = appContext.getProductManager();
        productManager.addListener(new ProductManagerListener(this));

        geometryListener = new GeometryListener(this);

        final SelectionManager selectionManager = appContext.getApplicationPage().getSelectionManager();
        productSelectionListener = new ProductSelectionListener(this, selectionManager);
        selectionManager.addSelectionChangeListener(productSelectionListener);
    }

    @Override
    protected final void onOK() {
        try {
            final List<File> targetFiles;
            if (exportParameter.isUseSelectedProduct()) {
                targetFiles = getTargetFiles(appContext.getSelectedProduct().getFileLocation().getAbsolutePath(),
                                             exportParameter.getTargetDirectory());
            } else {
                targetFiles = getTargetFiles(
                        exportParameter.getSourceDirectory().getAbsolutePath() + File.separator + "*",
                        exportParameter.getTargetDirectory());
            }

            final List<File> existingFiles = getExistingFiles(targetFiles);
            if (!existingFiles.isEmpty()) {
                final String files = listToString(existingFiles);
                final String message = MessageFormat.format(
                        "The selected target file(s) already exists.\n\nDo you want to overwrite the target file(s)?\n\n" +
                        "{0}",
                        files
                );
                final int answer = JOptionPane.showConfirmDialog(getJDialog(), message, getTitle(),
                                                                 JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.NO_OPTION) {
                    return;
                }
                exportParameter.setOverwriteTarget(true);
            }
        } catch (IOException e) {
            showErrorDialog(e.getMessage());
            return;
        }

        final ConverterSwingWorker worker = new ConverterSwingWorker(appContext, exportParameter);

        GuiHelper.setDefaultSourceDirectory(exportParameter.getSourceDirectory(), appContext);
        GuiHelper.setDefaultTargetDirectory(exportParameter.getTargetDirectory(), appContext);

        worker.execute();
    }

    private JComponent createSourceProductsSelector() {
        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);
        final JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Source Products"));
        final boolean canProductSelectionBeEnabled = DialogHelper.canProductSelectionBeEnabled(appContext);

        GuiHelper.addSourceProductsButtons(panel, canProductSelectionBeEnabled, bindingContext);

        final PropertyDescriptor sourceDirectoryDescriptor = propertySet.getDescriptor(
                BindingConstants.SOURCE_DIRECTORY);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(sourceDirectoryDescriptor,
                                                                          new DefaultChooserFactory(),
                                                                          bindingContext);

        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        panel.add(fileEditor);

        return panel;
    }

    private JComponent createRegionSelector() {
        final JRadioButton wholeProductButton = new JRadioButton("Whole Product");

        final JRadioButton useGeometryButton = new JRadioButton("Geometry");
        final PropertyDescriptor geometryDescriptor = propertySet.getDescriptor(BindingConstants.REGION);
        if (geometryDescriptor.getValueSet() == null) {
            useGeometryButton.setEnabled(false);
        }

        final JRadioButton useAreaButton = new JRadioButton("Area");
        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<>();
        buttonGroupValueSet.put(wholeProductButton, BindingConstants.ROI_TYPE_WHOLE_PRODUCT);
        buttonGroupValueSet.put(useGeometryButton, BindingConstants.ROI_TYPE_GEOMETRY);
        buttonGroupValueSet.put(useAreaButton, BindingConstants.ROI_TYPE_AREA);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(wholeProductButton);
        buttonGroup.add(useGeometryButton);
        buttonGroup.add(useAreaButton);
        bindingContext.bind(BindingConstants.ROI_TYPE, buttonGroup, buttonGroupValueSet);

        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);
        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        layout.setCellPadding(4, 0, new Insets(0, 24, 3, 3));

        final JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Region of Interest"));

        final JComboBox geometryComboBox = GuiHelper.createGeometryComboBox(geometryDescriptor, bindingContext);

        panel.add(wholeProductButton);
        panel.add(useGeometryButton);
        panel.add(geometryComboBox);
        panel.add(useAreaButton);

        final RegionBoundsInputUI regionBoundsInputUI = new RegionBoundsInputUI(bindingContext);
        bindingContext.addPropertyChangeListener(BindingConstants.ROI_TYPE, evt -> {
            final int roiType = (Integer) evt.getNewValue();
            if (roiType == BindingConstants.ROI_TYPE_AREA) {
                regionBoundsInputUI.setEnabled(true);
            } else {
                regionBoundsInputUI.setEnabled(false);
            }
        });
        regionBoundsInputUI.setEnabled(false);
        panel.add(regionBoundsInputUI.getUI());

        return panel;
    }

    private JComponent createTargetDirSelector() {
        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);

        final JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Target Directory"));

        final JLabel label = new JLabel();
        label.setText("Save files to directory:");
        panel.add(label);

        final PropertyDescriptor targetDirectoryDescriptor = propertySet.getDescriptor(
                BindingConstants.TARGET_DIRECTORY);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(targetDirectoryDescriptor,
                                                                          new DirectoryChooserFactory(),
                                                                          bindingContext,
                                                                          false);

        panel.add(fileEditor);

        return panel;
    }

    private void init(PropertySet propertySet) throws ValidationException {
        final File defaultSourceDirectory = GuiHelper.getDefaultSourceDirectory(appContext);
        propertySet.setValue(BindingConstants.SOURCE_DIRECTORY, defaultSourceDirectory);

        final File defaultTargetDirectory = GuiHelper.getDefaultTargetDirectory(appContext);
        propertySet.setValue(BindingConstants.TARGET_DIRECTORY, defaultTargetDirectory);

        updateSelectedProductAndGeometries(propertySet);
    }

    private void updateSelectedProductAndGeometries(PropertySet propertySet) throws ValidationException {
        final Product selectedSmosProduct = DialogHelper.getSelectedSmosProduct(appContext);
        if (selectedSmosProduct != null) {
            propertySet.setValue(BindingConstants.SELECTED_PRODUCT, true);
            final List<Geometry> geometries = GuiHelper.getPolygonGeometries(selectedSmosProduct);
            if (!geometries.isEmpty()) {
                GuiHelper.bindGeometries(geometries, propertySet);
            } else {
                removeGeometries();
            }
            setSelectedProductButtonEnabled(true);
            propertySet.setValue(BindingConstants.SELECTED_PRODUCT, true);
            setSelectionToSelectedGeometry(propertySet);
            selectedSmosProduct.addProductNodeListener(geometryListener);
        } else {
            propertySet.setValue(BindingConstants.SELECTED_PRODUCT, false);
            propertySet.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_WHOLE_PRODUCT);
        }
    }

    private void removeGeometries() {
        final Property geometryProperty = propertySet.getProperty(BindingConstants.REGION);
        geometryProperty.getDescriptor().setValueSet(new ValueSet(new VectorDataNode[0]));
        propertySet.setValue(BindingConstants.REGION, null);
        propertySet.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_AREA);
    }

    private void removeProductAndGeometries(Product product) {
        final Product selectedSmosProduct = DialogHelper.getSelectedSmosProduct(appContext);
        if (selectedSmosProduct == null) {
            setSelectedProductButtonEnabled(false);

            final List<VectorDataNode> geometryNodeList = GuiHelper.getGeometries(product);
            if (!geometryNodeList.isEmpty()) {
                removeGeometries();
            }
        }
    }

    private void setSelectedProductButtonEnabled(boolean enabled) {
        final Binding binding = bindingContext.getBinding(BindingConstants.SELECTED_PRODUCT);
        final JComponent[] components = binding.getComponents();
        for (final JComponent component : components) {
            if (component instanceof JRadioButton) {
                if (((JRadioButton) component).getText().equals(BindingConstants.USE_SELECTED_PRODUCT_BUTTON_NAME)) {
                    component.setEnabled(enabled);
                    break;
                }
            }
        }
    }

    private void setSelectionToSelectedGeometry(PropertySet propertySet) {
        final Geometry selectedGeometry = GuiHelper.getSelectedGeometry(appContext);
        if (selectedGeometry != null) {
            propertySet.setValue(BindingConstants.REGION, selectedGeometry);
        }
    }

    // package access for testing only tb 2014-08-04
    static void setAreaToGlobe(PropertyContainer propertyContainer) {
        propertyContainer.setValue(BindingConstants.NORTH, 90.0);
        propertyContainer.setValue(BindingConstants.SOUTH, -90.0);
        propertyContainer.setValue(BindingConstants.EAST, 180.0);
        propertyContainer.setValue(BindingConstants.WEST, -180.0);
    }

    // package access for testing only tb 2013-05-27
    static List<File> getTargetFiles(String filePath, File targetDir) throws IOException {
        final ArrayList<File> targetFiles = new ArrayList<>();

        final File file = new File(filePath);
        if (file.isFile()) {
            final File outputFile = EEToNetCDFExporterOp.getOutputFile(file, targetDir);
            targetFiles.add(outputFile);
        } else {
            final TreeSet<File> sourceFileSet = new TreeSet<>();
            WildcardMatcher.glob(filePath, sourceFileSet);
            for (File aSourceFile : sourceFileSet) {
                final File outputFile = EEToNetCDFExporterOp.getOutputFile(aSourceFile, targetDir);
                targetFiles.add(outputFile);
            }
        }

        return targetFiles;
    }

    // package access for testing only tb 2013-05-27
    static List<File> getExistingFiles(List<File> targetFiles) {
        return targetFiles.stream().filter(File::isFile).collect(Collectors.toList());
    }

    // package access for testing only tb 2013-05-27
    static String listToString(List<File> targetFiles) {
        final StringBuilder builder = new StringBuilder();
        int fileCount = 0;
        for (final File targetFile : targetFiles) {
            builder.append(targetFile.getAbsolutePath());
            builder.append("\n");
            fileCount++;
            if (fileCount >= 10) {
                builder.append("...");
                break;
            }
        }
        return builder.toString();
    }

    @Override
    protected void onClose() {
        productSelectionListener.dispose();
        super.onClose();
    }

    @Override
    protected void productAdded() {
        try {
            updateSelectedProductAndGeometries(propertySet);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected void productRemoved(Product product) {
        removeProductAndGeometries(product);
        product.removeProductNodeListener(geometryListener);
    }

    @Override
    protected void geometryAdded() {
        try {
            updateSelectedProductAndGeometries(propertySet);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected void geometryRemoved() {
        try {
            updateSelectedProductAndGeometries(propertySet);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected void productSelectionChanged() {
        try {
            updateSelectedProductAndGeometries(propertySet);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }

}
