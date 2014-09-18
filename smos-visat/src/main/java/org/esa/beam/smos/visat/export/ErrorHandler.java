package org.esa.beam.smos.visat.export;

interface ErrorHandler {

    public void warning(final Throwable t);

    public void error(final Throwable t);
}
