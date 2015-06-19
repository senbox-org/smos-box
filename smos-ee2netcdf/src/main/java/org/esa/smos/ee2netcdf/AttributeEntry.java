package org.esa.smos.ee2netcdf;

class AttributeEntry {

    private String name;
    private String value;

    AttributeEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    String getName() {
        return name;
    }

    String getValue() {
        return value;
    }
}
