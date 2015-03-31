package org.esa.beam.smos.ee2netcdf.ui;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.converters.ArrayConverter;
import com.bc.ceres.binding.converters.StringConverter;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.PropertyPane;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.framework.ui.AbstractDialog;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.RegionBoundsInputUI;
import org.esa.beam.smos.ee2netcdf.ExportParameter;
import org.esa.beam.smos.ee2netcdf.NetcdfExportOp;
import org.esa.beam.smos.gui.BindingConstants;
import org.esa.beam.smos.gui.DefaultChooserFactory;
import org.esa.beam.smos.gui.DirectoryChooserFactory;
import org.esa.beam.smos.gui.GuiHelper;
import org.esa.beam.smos.gui.ProductChangeAwareDialog;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.util.SelectionSupport;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ralf Quast
 */
class NetcdfExportDialog extends ProductChangeAwareDialog {

    private final AppContext appContext;
    private final ExportParameter exportParameter;
    private final PropertySet propertySet;
    private final BindingContext bindingContext;

    NetcdfExportDialog(AppContext appContext, String helpID) {
        super(appContext.getApplicationWindow(), "Export SMOS Earth Explorer Files to NetCDF",
              AbstractDialog.ID_OK | AbstractDialog.ID_CLOSE | AbstractDialog.ID_HELP,
              helpID);
        this.appContext = appContext;

        exportParameter = new ExportParameter();

        propertySet = PropertyContainer.createObjectBacked(exportParameter, new ParameterDescriptorFactory());
        propertySet.setDefaultValues();
        bindingContext = new BindingContext(propertySet);

        final JPanel ioPanel = GuiHelper.createPanelWithBoxLayout();
        ioPanel.add(createSourceProductsSelector());
        ioPanel.add(createTargetDirSelector());
        ioPanel.add(createRoiSelector());
        final JTabbedPane form = new JTabbedPane();
        form.add("I/O Parameters", ioPanel);

        if (propertySet.getProperties().length > 0) {
            propertySet.getDescriptor(BindingConstants.OPEN_FILE_DIALOG).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.SELECTED_PRODUCT).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.SOURCE_DIRECTORY).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.TARGET_DIRECTORY).setAttribute("visible", false);

            propertySet.getDescriptor(BindingConstants.ROI_TYPE).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.GEOMETRY).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.NORTH).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.SOUTH).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.EAST).setAttribute("visible", false);
            propertySet.getDescriptor(BindingConstants.WEST).setAttribute("visible", false);
            final ArrayConverter arrayConverter = new ArrayConverter(String[].class, new StringConverter());
            propertySet.getDescriptor(BindingConstants.VARIABLES).setConverter(arrayConverter);

            final PropertyPane parametersPane = new PropertyPane(bindingContext);
            final JPanel parametersPanel = parametersPane.createPanel();
            parametersPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
            form.add("Processing Parameters", new JScrollPane(parametersPanel));
        }

        setContent(form);

        try {
            init(propertySet);
        } catch (ValidationException e) {
            throw new IllegalStateException(e.getMessage());
        }

        final ProductManager productManager = appContext.getProductManager();
        productManager.addListener(new ProductManagerListener(this));

        final SelectionSupport<Product> selectionSupport = SnapApp.getDefault().getSelectionSupport(Product.class);
        selectionSupport.addHandler(this);
    }

    @Override
    protected final void onOK() {
        try {
            final List<Path> targetFiles;
            if (exportParameter.isUseSelectedProduct()) {
                targetFiles = getTargetFiles(appContext.getSelectedProduct().getFileLocation().toPath(),
                                             exportParameter.getTargetDirectory().toPath());
            } else {
                targetFiles = getTargetFiles(exportParameter.getSourceDirectory().toPath(), "*",
                                             exportParameter.getTargetDirectory().toPath());
            }
            if (!exportParameter.isOverwriteTarget()) {
                final List<Path> existingFiles = getExistingPaths(targetFiles);
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
            }
        } catch (IOException e) {
            showErrorDialog(e.getMessage());
            return;
        }

        final NetcdfExportSwingWorker worker = new NetcdfExportSwingWorker(appContext, exportParameter);

        GuiHelper.setDefaultSourceDirectory(exportParameter.getSourceDirectory(), appContext);
        GuiHelper.setDefaultTargetDirectory(exportParameter.getTargetDirectory(), appContext);

        worker.execute();
    }

    private JComponent createSourceProductsSelector() {
        final PropertyDescriptor sourceDirectoryDescriptor =
                propertySet.getDescriptor(BindingConstants.SOURCE_DIRECTORY);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(sourceDirectoryDescriptor,
                                                                          new DefaultChooserFactory(),
                                                                          bindingContext);

        final TableLayout layout = GuiHelper.createWeightedTableLayout(1);
        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));

        final JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Source Products"));
        GuiHelper.addSourceProductsButtons(panel, DialogHelper.isProductSelectionFeasible(appContext), bindingContext);
        panel.add(fileEditor);

        return panel;
    }

    private JComponent createTargetDirSelector() {
        final JLabel label = new JLabel();
        label.setText("Save files to directory:");

        final PropertyDescriptor targetDirectoryDescriptor =
                propertySet.getDescriptor(BindingConstants.TARGET_DIRECTORY);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(targetDirectoryDescriptor,
                                                                          new DirectoryChooserFactory(),
                                                                          bindingContext,
                                                                          false);

        final JPanel panel = new JPanel(GuiHelper.createWeightedTableLayout(1));
        panel.setBorder(BorderFactory.createTitledBorder("Target Directory"));
        panel.add(label);
        panel.add(fileEditor);

        return panel;
    }

    private JComponent createRoiSelector() {
        final JRadioButton allButton = new JRadioButton("All");
        allButton.setToolTipText("Check this button to select the whole globe");

        final JRadioButton useGeometryButton = new JRadioButton("Polygon");
        useGeometryButton.setToolTipText(
                "Check this button to specify a region-of-interest in form a polygon geometry.");
        final PropertyDescriptor geometryDescriptor = propertySet.getDescriptor(BindingConstants.GEOMETRY);

        final JRadioButton useBoundingBoxButton = new JRadioButton("Bounding box");
        useBoundingBoxButton.setToolTipText(
                "Check this button to specify a region-of-interest in form a bounding box.");
        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<>();
        buttonGroupValueSet.put(allButton, BindingConstants.ROI_TYPE_ALL);
        buttonGroupValueSet.put(useGeometryButton, BindingConstants.ROI_TYPE_GEOMETRY);
        buttonGroupValueSet.put(useBoundingBoxButton, BindingConstants.ROI_TYPE_BOUNDING_BOX);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(allButton);
        buttonGroup.add(useGeometryButton);
        buttonGroup.add(useBoundingBoxButton);
        bindingContext.bind(BindingConstants.ROI_TYPE, buttonGroup, buttonGroupValueSet);

        final TableLayout layout = GuiHelper.createWeightedTableLayout(1);
        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        layout.setCellPadding(4, 0, new Insets(0, 24, 3, 3));

        final JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Region of Interest"));

        final JTextArea geometryTextArea = new JTextArea(5, 20);
        final JScrollPane geometryScrollPane = new JScrollPane(geometryTextArea);
        geometryTextArea.setToolTipText(geometryDescriptor.getDescription());
        bindingContext.bind(BindingConstants.GEOMETRY, geometryTextArea);
        bindingContext.bindEnabledState(BindingConstants.GEOMETRY, true,
                                        BindingConstants.ROI_TYPE,
                                        BindingConstants.ROI_TYPE_GEOMETRY);

        panel.add(allButton);
        panel.add(useGeometryButton);
        panel.add(geometryScrollPane);
        panel.add(useBoundingBoxButton);

        final RegionBoundsInputUI regionBoundsInputUI = new RegionBoundsInputUI(bindingContext);
        bindingContext.addPropertyChangeListener(BindingConstants.ROI_TYPE, changeEvent -> {
            final int roiType = (Integer) changeEvent.getNewValue();
            if (roiType == BindingConstants.ROI_TYPE_BOUNDING_BOX) {
                regionBoundsInputUI.setEnabled(true);
            } else {
                regionBoundsInputUI.setEnabled(false);
            }
        });
        regionBoundsInputUI.setEnabled(false);
        panel.add(regionBoundsInputUI.getUI());
        panel.revalidate();
        panel.repaint();

        return panel;
    }

    private void init(PropertySet propertySet) throws ValidationException {
        final File defaultSourceDirectory = GuiHelper.getDefaultSourceDirectory(appContext);
        propertySet.setValue(BindingConstants.SOURCE_DIRECTORY, defaultSourceDirectory);

        final File defaultTargetDirectory = GuiHelper.getDefaultTargetDirectory(appContext);
        propertySet.setValue(BindingConstants.TARGET_DIRECTORY, defaultTargetDirectory);

        updateSelectedProductButton();
    }

    private void updateSelectedProductButton() {
        final Product selectedSmosProduct = DialogHelper.getSelectedSmosProduct(appContext);
        if (selectedSmosProduct == null) {
            setSelectedProductButtonEnabled(false);
            bindingContext.getBinding(BindingConstants.SELECTED_PRODUCT).setPropertyValue(false);
        } else {
            setSelectedProductButtonEnabled(true);
            bindingContext.getBinding(BindingConstants.SELECTED_PRODUCT).setPropertyValue(true);
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

    // package access for testing only tb 2013-05-27
    static List<Path> getTargetFiles(Path sourceDir, String globPattern, Path targetDir) throws IOException {
        final ArrayList<Path> targetFiles = new ArrayList<>();

        DirectoryStream<Path> paths = Files.newDirectoryStream(sourceDir, globPattern);
        paths.forEach(path -> {
            final Path outputFile = NetcdfExportOp.getOutputFile(path, targetDir);
            targetFiles.add(outputFile);
        });
        Collections.sort(targetFiles);
        return targetFiles;
    }

    static List<Path> getTargetFiles(Path filePath, Path targetDir) throws IOException {
        final ArrayList<Path> targetFiles = new ArrayList<>();

        final Path outputFile = NetcdfExportOp.getOutputFile(filePath, targetDir);
        targetFiles.add(outputFile);

        return targetFiles;
    }

    // package access for testing only tb 2013-05-27
    static List<Path> getExistingPaths(List<Path> targetFiles) {
        return targetFiles.stream().filter(Files::exists).collect(Collectors.toList());
    }

    // package access for testing only tb 2013-05-27
    static String listToString(List<Path> targetFiles) {
        final StringBuilder builder = new StringBuilder();
        int fileCount = 0;
        for (final Path targetFile : targetFiles) {
            builder.append(targetFile.toAbsolutePath());
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
        final SelectionSupport<Product> selectionSupport = SnapApp.getDefault().getSelectionSupport(Product.class);
        selectionSupport.removeHandler(this);
        super.onClose();
    }

    @Override
    protected void productAdded() {
        updateSelectedProductButton();
    }

    @Override
    protected void productRemoved(Product product) {
        updateSelectedProductButton();
    }

    @Override
    protected void geometryAdded() {
    }

    @Override
    protected void geometryRemoved() {
    }

    @Override
    protected void productSelectionChanged() {
        updateSelectedProductButton();
    }

}
