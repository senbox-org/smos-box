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

import org.jdesktop.swingx.table.TableColumnModelExt;

import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A class which can export a given {@link TableModel table model} to an output stream
 * in a CSV format.
 *
 * @author Marco Peters
 * @version $Revision: $ $Date: $
 */
class TableModelExporter {

    private char separator;
    private final TableModel tableModel;
    private final TableColumnModelExt columnModel;

    /**
     * Creates an CSV exporter for the given {@link TableModel table model}.
     * As default separator ',' is used.
     *
     * @param tableModel the table model to export.
     */
    TableModelExporter(TableModel tableModel, TableColumnModelExt columnModel) {
        this.tableModel = tableModel;
        this.columnModel = columnModel;
        this.separator = ',';
    }

    /**
     * Gets the currently used separator character.
     *
     * @return the separator character.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Sets the separtor character.
     *
     * @param separator the separtor character.
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * Exports the  associated table model to the given output stream
     *
     * @param out the stream to export the table model to.
     */
    public void export(OutputStream out) {
        try (PrintWriter writer = new PrintWriter(out)) {
            writeHeaderRow(writer);
            writeDataRows(writer);
        }
    }

    private void writeHeaderRow(final PrintWriter out) {
        int visibleColumnCount = columnModel.getColumnCount(false);
        for (int columnIndex = 0; columnIndex < visibleColumnCount; columnIndex++) {
            final TableColumn column = columnModel.getColumn(columnIndex);
            out.print(column.getHeaderValue());
            if (columnIndex < visibleColumnCount - 1) {
                out.print(getSeparator());
            }
        }
        out.println();
    }

    private void writeDataRows(final PrintWriter out) {
        int visibleColumnCount = columnModel.getColumnCount(false);
        final int rowCount = tableModel.getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < visibleColumnCount; columnIndex++) {
                final TableColumn column = columnModel.getColumn(columnIndex);
                int modelIndex = column.getModelIndex();
                out.print(tableModel.getValueAt(rowIndex, modelIndex));
                if (columnIndex < visibleColumnCount - 1) {
                    out.print(getSeparator());
                }
            }
            out.println();
        }
    }
}
