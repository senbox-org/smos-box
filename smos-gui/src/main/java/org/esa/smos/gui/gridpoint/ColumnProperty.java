package org.esa.smos.gui.gridpoint;

class ColumnProperty {

    private String columnName;
    private boolean visible;

    public ColumnProperty(String columnName, boolean visible) {
        this.columnName = columnName;
        this.visible = visible;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isVisible() {
        return visible;
    }
}
