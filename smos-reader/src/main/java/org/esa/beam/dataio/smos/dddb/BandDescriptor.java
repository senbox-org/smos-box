package org.esa.beam.dataio.smos.dddb;

public interface BandDescriptor {

    String getBandName();

    String getMemberName();

    int getPolarization();

    boolean isVisible();

    int getSampleModel();

    double getScalingOffset();

    double getScalingFactor();

    boolean hasTypicalMin();

    boolean hasTypicalMax();

    boolean hasFillValue();

    double getTypicalMin();

    double getTypicalMax();

    double getFillValue();

    String getValidPixelExpression();

    String getUnit();

    boolean isCyclic();

    String getDescription();

    String getFlagCodingName();

    Family<FlagDescriptor> getFlagDescriptors();
}