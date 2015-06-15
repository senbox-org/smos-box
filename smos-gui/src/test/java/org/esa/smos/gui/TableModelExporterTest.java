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

package org.esa.smos.gui;

import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.junit.Before;
import org.junit.Test;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableModelExporterTest {

    private ByteArrayOutputStream stream;
    private TableColumnModelExt columnModel;

    @Before
    public void setUp() {
        stream = new ByteArrayOutputStream();
        columnModel = createColumnModel();
    }

    @Test
    public void testSimpleModel() {
        final TableModel tableModel = createTableModel();
        final TableModelExporter exporter = new TableModelExporter(tableModel, columnModel);
        exporter.setSeparator('\t');

        exporter.export(stream);

        final String actual = stream.toString();
        assertTableModel(actual);
    }

    @Test
    public void testSimpleModelWithDifferentColumnVisibility() {
        final TableModel tableModel = createTableModel();
        final TableModelExporter exporter = new TableModelExporter(tableModel, createColumnModelWithoutTiffy());
        exporter.setSeparator('\t');

        exporter.export(stream);

        final String actual = stream.toString();
        assertTrue(actual.contains("Bibo\tSamson"));
        assertTrue(actual.contains("12\t45.456"));
        assertTrue(actual.contains("11\t129.5678"));
        assertTrue(actual.contains("2\t0.1"));
    }

    @Test
    public void testSimpleModelNoColumnVisibility() {
        final TableModel tableModel = createTableModel();
        final TableModelExporter exporter = new TableModelExporter(tableModel, createEmptyColumnModel());
        exporter.setSeparator('\t');

        exporter.export(stream);

        assertTrue(stream.toString().trim().isEmpty());
    }

    private void assertTableModel(String actual) {
        assertTrue(actual.contains("Bibo\tTiffy\tSamson"));
        assertTrue(actual.contains("12\tCat\t45.456"));
        assertTrue(actual.contains("11\tMouse\t129.5678"));
        assertTrue(actual.contains("2\tDog\t0.1"));
    }

    private TableModel createTableModel() {
        final String[] columnNames = {"Bibo", "Tiffy", "Samson"};
        final Object[][] tableData = {
                {12, "Cat", 45.456},
                {11, "Mouse", 129.5678},
                {2, "Dog", 0.1}
        };
        final DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setDataVector(tableData, columnNames);
        return tableModel;
    }

    private TableColumnModelExt createColumnModel() {
        final TableColumnModelExt columnModel = mock(TableColumnModelExt.class);
        when(columnModel.getColumnCount(false)).thenReturn(3);

        final TableColumnExt biboMock = mock(TableColumnExt.class);
        when(biboMock.getHeaderValue()).thenReturn("Bibo");
        when(biboMock.getModelIndex()).thenReturn(0);

        final TableColumnExt tiffyMock = mock(TableColumnExt.class);
        when(tiffyMock.getHeaderValue()).thenReturn("Tiffy");
        when(tiffyMock.getModelIndex()).thenReturn(1);

        final TableColumnExt samsonMock = mock(TableColumnExt.class);
        when(samsonMock.getHeaderValue()).thenReturn("Samson");
        when(samsonMock.getModelIndex()).thenReturn(2);

        when(columnModel.getColumn(0)).thenReturn(biboMock);
        when(columnModel.getColumn(1)).thenReturn(tiffyMock);
        when(columnModel.getColumn(2)).thenReturn(samsonMock);

        return columnModel;
    }

    private TableColumnModelExt createColumnModelWithoutTiffy() {
        final TableColumnModelExt columnModel = mock(TableColumnModelExt.class);
        when(columnModel.getColumnCount(false)).thenReturn(2);

        final TableColumnExt biboMock = mock(TableColumnExt.class);
        when(biboMock.getHeaderValue()).thenReturn("Bibo");
        when(biboMock.getModelIndex()).thenReturn(0);

        final TableColumnExt samsonMock = mock(TableColumnExt.class);
        when(samsonMock.getHeaderValue()).thenReturn("Samson");
        when(samsonMock.getModelIndex()).thenReturn(2);

        when(columnModel.getColumn(0)).thenReturn(biboMock);
        when(columnModel.getColumn(1)).thenReturn(samsonMock);

        return columnModel;
    }

    private TableColumnModelExt createEmptyColumnModel() {
        final TableColumnModelExt columnModel = mock(TableColumnModelExt.class);
        when(columnModel.getColumnCount(false)).thenReturn(0);

        return columnModel;
    }
}
