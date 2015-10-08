/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.smos.gui.gridpoint;

import org.esa.smos.dataio.smos.GridPointBtDataset;
import org.esa.smos.dataio.smos.SmosReader;
import org.esa.smos.dataio.smos.dddb.Dddb;
import org.esa.smos.dataio.smos.dddb.FlagDescriptor;
import org.esa.snap.framework.ui.product.ProductSceneView;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleInsets;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.windows.TopComponent;

import javax.swing.JComponent;
import java.awt.Color;
import java.io.IOException;
import java.util.List;

@TopComponent.Description(
        preferredID = "GridPointBtDataFlagmatrixTopComponent",
        iconBase = "org/esa/smos/icons/SmosFlags.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "navigator", openAtStartup = false, position = 3)
@ActionID(category = "Window", id = "org.esa.smos.gui.gridpoint.GridPointBtDataFlagmatrixTopComponent")
@ActionReferences({
        @ActionReference(path = "Menu/View/Tool Windows/SMOS", position = 30),
        @ActionReference(path = "Toolbars/SMOS", position = 30)
})
@TopComponent.OpenActionRegistration(
        displayName = GridPointBtDataFlagmatrixTopComponent.DISPLAY_NAME,
        preferredID = "GridPointBtDataFlagmatrixTopComponent"
)

public class GridPointBtDataFlagmatrixTopComponent extends GridPointBtDataTopComponent {

    static final String DISPLAY_NAME = "SMOS L1C Flag-Matrix";

    private static final String SERIES_KEY = "Flags";
    private static final String DEFAULT_FLAG_DESCRIPTOR_IDENTIFIER = "DBL_SM_XXXX_MIR_XXXF1C_0400_flags";

    private JFreeChart chart;
    private DefaultXYZDataset dataset;
    private XYPlot plot;
    private ChartPanel chartPanel;
    private FlagDescriptor[] flagDescriptors;
    private XYBlockRenderer renderer;

    public GridPointBtDataFlagmatrixTopComponent() {
        super();
        setDisplayName(DISPLAY_NAME);
    }

    @Override
    protected JComponent createGridPointComponent() {
        dataset = new DefaultXYZDataset();

        final NumberAxis xAxis = new NumberAxis("Record #");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);

        final List<FlagDescriptor> flagDescriptorList = Dddb.getInstance().getFlagDescriptors(
                DEFAULT_FLAG_DESCRIPTOR_IDENTIFIER).asList();
        flagDescriptors = flagDescriptorList.toArray(new FlagDescriptor[flagDescriptorList.size()]);
        final String[] flagNames = createFlagNames(flagDescriptors);
        final NumberAxis yAxis = createRangeAxis(flagNames);

        final LookupPaintScale paintScale = new LookupPaintScale(0.0, 4.0, Color.WHITE);
        paintScale.add(0.0, Color.BLACK);
        paintScale.add(1.0, Color.RED);
        paintScale.add(2.0, Color.GREEN);
        paintScale.add(3.0, Color.BLUE);
        paintScale.add(4.0, Color.YELLOW);

        renderer = new XYBlockRenderer();
        renderer.setPaintScale(paintScale);
        renderer.setBaseToolTipGenerator(new FlagToolTipGenerator(flagNames));

        plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setForegroundAlpha(0.5f);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        plot.setNoDataMessage("No data");

        chart = new JFreeChart(null, plot);
        chart.removeLegend();
        chartPanel = new ChartPanel(chart);

        return chartPanel;
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        boolean enabled = smosView != null;
        SmosReader smosReader = null;
        if (enabled) {
            smosReader = getSelectedSmosReader();
            if (smosReader == null || !smosReader.canSupplyGridPointBtData()) {
                enabled = false;
            }
        }
        chartPanel.setEnabled(enabled);
        if (enabled) {
            flagDescriptors = smosReader.getBtFlagDescriptors();
            final String[] flagNames = createFlagNames(flagDescriptors);
            renderer.setBaseToolTipGenerator(new FlagToolTipGenerator(flagNames));
            final NumberAxis rangeAxis = createRangeAxis(flagNames);
            plot.setRangeAxis(rangeAxis);
        }
    }

    @Override
    protected void updateGridPointBtDataComponent(GridPointBtDataset ds) {
        dataset.removeSeries(SERIES_KEY);

        int iq = ds.getFlagBandIndex();
        if (iq != -1) {
            final Number[][] dsData = ds.getData();
            final int m = dsData.length;
            final int n = flagDescriptors.length;
            double[][] data = new double[3][n * m];
            for (int x = 0; x < m; x++) {
                final int flags = dsData[x][iq].intValue();
                for (int y = 0; y < n; y++) {
                    final int index = y * m + x;
                    data[0][index] = (1 + x);
                    data[1][index] = y;
                    final int mask = flagDescriptors[y].getMask();
                    data[2][index] = ((flags & mask) == mask) ? (1 + y % 3) : 0.0;
                }
            }
            dataset.addSeries(SERIES_KEY, data);
        } else {
            plot.setNoDataMessage("Not a SMOS D1C/F1C pixel.");
        }
        chart.fireChartChanged();
    }

    @Override
    protected void updateGridPointBtDataComponent(IOException e) {
        dataset.removeSeries(SERIES_KEY);
        plot.setNoDataMessage("I/O error");
    }

    @Override
    protected void clearGridPointBtDataComponent() {
        if (dataset != null) {
            dataset.removeSeries(SERIES_KEY);
        }
        if (plot != null) {
            plot.setNoDataMessage("No data");
        }
    }

    private String[] createFlagNames(FlagDescriptor[] flagDescriptors) {
        final String[] flagNames = new String[flagDescriptors.length];
        for (int i = 0; i < flagDescriptors.length; i++) {
            flagNames[i] = flagDescriptors[i].getFlagName();
        }

        return flagNames;
    }

    private NumberAxis createRangeAxis(String[] flagNames) {
        final NumberAxis axis = new SymbolAxis(null, flagNames);
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        axis.setAutoRangeIncludesZero(false);
        axis.setLowerMargin(0.0);
        axis.setUpperMargin(0.0);
        axis.setInverted(true);

        return axis;
    }


    private class FlagToolTipGenerator implements XYZToolTipGenerator {

        private String[] flagNames;

        private FlagToolTipGenerator(String[] flagNames) {
            this.flagNames = flagNames;
        }

        @Override
        public String generateToolTip(XYDataset xyDataset, int series, int item) {
            return generateToolTip((XYZDataset) xyDataset, series, item);
        }

        @Override
        public String generateToolTip(XYZDataset xyzDataset, int series, int item) {
            int recordIndex = dataset.getX(series, item).intValue();
            int flagIndex = dataset.getY(series, item).intValue();
            boolean flagSet = dataset.getZ(series, item).intValue() != 0;
            String flagName = "?";
            if (flagIndex >= 0 && flagIndex < flagNames.length) {
                flagName = flagNames[flagIndex];
            }
            return flagName + "(" + recordIndex + "): " + (flagSet ? "ON" : "OFF");
        }
    }
}
