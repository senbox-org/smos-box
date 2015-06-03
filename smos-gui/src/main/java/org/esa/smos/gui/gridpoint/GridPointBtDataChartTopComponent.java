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
import org.esa.smos.dataio.smos.PolarisationModel;
import org.esa.smos.dataio.smos.SmosReader;
import org.esa.snap.framework.ui.product.ProductSceneView;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.windows.TopComponent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.IOException;

@TopComponent.Description(
        preferredID = "GridPointBtDataChartTopComponent",
        iconBase = "org/esa/smos/icons/SmosBtGraph.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "navigator",
        openAtStartup = false,
        position = 1
)
@ActionID(category = "Window", id = "org.esa.smos.gui.gridpoint.GridPointBtDataChartTopComponent")
@ActionReferences({
        @ActionReference(path = "Menu/Window/Tool Windows/SMOS")
})
@TopComponent.OpenActionRegistration(
        displayName = GridPointBtDataChartTopComponent.DISPLAY_NAME,
        preferredID = "GridPointBtDataChartTopComponent"
)

public class GridPointBtDataChartTopComponent extends GridPointBtDataTopComponent {

    static final String DISPLAY_NAME = "GridPoint BT Data Chart";

    private JFreeChart chart;
    private YIntervalSeriesCollection coPolDataset;
    private YIntervalSeriesCollection crossPolDataset;
    private XYPlot plot;
    private JCheckBox[] modeCheckers;
    private PolarisationModel polarisationModel;

    public GridPointBtDataChartTopComponent() {
        super();
        setDisplayName(DISPLAY_NAME);
    }

    @Override
    protected JComponent createGridPointComponent() {
        coPolDataset = new YIntervalSeriesCollection();
        crossPolDataset = new YIntervalSeriesCollection();
        chart = ChartFactory.createXYLineChart(null,
                null,
                null,
                coPolDataset,
                PlotOrientation.VERTICAL,
                true, // Legend?
                true,
                false);

        plot = chart.getXYPlot();
        plot.setNoDataMessage("No data");
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

        final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setLabel("Incidence Angle (deg)");
        xAxis.setRange(0, 70);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setLabel("Co-Pol BT(K)");
        yAxis.setRange(50, 350);

        final NumberAxis yAxis2 = new NumberAxis("Cross-Pol BT(K)");
        yAxis2.setRange(-25, 25);
        plot.setRangeAxis(1, yAxis2);
        plot.setDataset(1, crossPolDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        DeviationRenderer coPolRenderer = new DeviationRenderer(true, false);
        coPolRenderer.setSeriesFillPaint(0, new Color(255, 127, 127));
        coPolRenderer.setSeriesFillPaint(1, new Color(127, 127, 255));
        DeviationRenderer crossPolRenderer = new DeviationRenderer(true, false);
        crossPolRenderer.setSeriesFillPaint(0, new Color(127, 255, 127));
        crossPolRenderer.setSeriesFillPaint(1, new Color(255, 255, 127));
        plot.setRenderer(0, coPolRenderer);
        plot.setRenderer(1, crossPolRenderer);

        return new ChartPanel(chart);
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        final SmosReader smosReader = getSelectedSmosReader();
        if (smosReader != null && smosReader.canSupplyGridPointBtData()) {
            modeCheckers[0].setEnabled(true);
            modeCheckers[1].setEnabled(true);
            modeCheckers[2].setEnabled(smosReader.canSupplyFullPolData());

            polarisationModel = smosReader.getPolarisationModel();
        }
    }

    @Override
    protected JComponent createGridPointComponentOptionsComponent() {
        modeCheckers = new JCheckBox[]{
                new JCheckBox("X", true),
                new JCheckBox("Y", true),
                new JCheckBox("XY", true),
        };
        final JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        for (JCheckBox modeChecker : modeCheckers) {
            modeChecker.addActionListener(e -> updateGridPointBtDataComponent());
            optionsPanel.add(modeChecker);
        }
        return optionsPanel;
    }

    @Override
    protected void updateGridPointBtDataComponent(GridPointBtDataset ds) {
        coPolDataset.removeAllSeries();
        crossPolDataset.removeAllSeries();

        int ix = ds.getIncidenceAngleBandIndex();
        int iq = ds.getPolarisationFlagBandIndex();
        int id = ds.getRadiometricAccuracyBandIndex();
        // todo: calculate and display H/V/HV BT values instead of X/Y/XY (rq-200100121)
        if (ix != -1 && iq != -1 && id != -1) {
            int iy1 = ds.getColumnIndex("BT_Value");
            if (iy1 != -1) {
                YIntervalSeries series1 = new YIntervalSeries("X");
                YIntervalSeries series2 = new YIntervalSeries("Y");
                boolean m1 = modeCheckers[0].isSelected();
                boolean m2 = modeCheckers[1].isSelected();
                final Number[][] data = ds.getData();
                for (final Number[] dataList : data) {
                    int polMode = polarisationModel.getPolarisationMode(dataList[iq].intValue());
                    double x = dataList[ix].doubleValue();
                    double y = dataList[iy1].doubleValue();
                    double dev = dataList[id].doubleValue();
                    if (m1 && polarisationModel.is_X_Polarised(polMode)) {
                        series1.add(x, y, y - dev, y + dev);
                    } else if (m2 && polarisationModel.is_Y_Polarised(polMode)) {
                        series2.add(x, y, y - dev, y + dev);
                    }
                }
                coPolDataset.addSeries(series1);
                coPolDataset.addSeries(series2);
            } else {
                int iy2;
                iy1 = ds.getBTValueRealBandIndex();
                iy2 = ds.getBTValueImaginaryBandIndex();
                if (iy1 != -1 && iy2 != -1) {
                    final YIntervalSeries series1 = new YIntervalSeries("X");
                    final YIntervalSeries series2 = new YIntervalSeries("Y");
                    final YIntervalSeries series3 = new YIntervalSeries("XY_Real");
                    final YIntervalSeries series4 = new YIntervalSeries("XY_Imag");
                    final boolean m1 = modeCheckers[0].isSelected();
                    final boolean m2 = modeCheckers[1].isSelected();
                    final boolean m3 = modeCheckers[2].isSelected();
                    final Number[][] data = ds.getData();

                    for (final Number[] dataList : data) {
                        int polMode = polarisationModel.getPolarisationMode(dataList[iq].intValue());
                        double dev = dataList[id].doubleValue();
                        double x = dataList[ix].doubleValue();
                        double y1 = dataList[iy1].doubleValue();
                        if (m1 && polarisationModel.is_X_Polarised(polMode)) {
                            series1.add(x, y1, y1 - dev, y1 + dev);
                        } else if (m2 && polarisationModel.is_Y_Polarised(polMode)) {
                            series2.add(x, y1, y1 - dev, y1 + dev);
                        } else if (m3 && (polarisationModel.is_XY1_Polarised(polMode) || polarisationModel.is_XY2_Polarised(polMode))) {
                            double y2 = dataList[iy2].doubleValue();
                            series3.add(x, y1, y1 - dev, y1 + dev);
                            series4.add(x, y2, y2 - dev, y2 + dev);
                        }
                    }
                    coPolDataset.addSeries(series1);
                    coPolDataset.addSeries(series2);
                    crossPolDataset.addSeries(series3);
                    crossPolDataset.addSeries(series4);
                }
            }
        } else {
            plot.setNoDataMessage("Not a SMOS SCxD1C/SCxF1C pixel.");
        }
        chart.fireChartChanged();
    }

    @Override
    protected void updateGridPointBtDataComponent(IOException e) {
        coPolDataset.removeAllSeries();
        crossPolDataset.removeAllSeries();
        plot.setNoDataMessage("I/O error");
    }

    @Override
    protected void clearGridPointBtDataComponent() {
        if (coPolDataset != null) {
            coPolDataset.removeAllSeries();
        }
        if (crossPolDataset != null) {
            crossPolDataset.removeAllSeries();
        }
        if (plot != null) {
            plot.setNoDataMessage("No data");
        }
    }
}
