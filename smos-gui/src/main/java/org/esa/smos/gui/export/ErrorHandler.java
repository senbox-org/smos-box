package org.esa.smos.gui.export;

interface ErrorHandler {

    void warning(final Throwable t);

    void error(final Throwable t);
}
