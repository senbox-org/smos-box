package org.esa.smos;

import java.io.IOException;


public interface PointList {

    int getElementCount();

    double getLon(int i) throws IOException;

    double getLat(int i) throws IOException;
}
