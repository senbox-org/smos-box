package org.esa.smos.dataio.smos.bufr;

import java.awt.geom.Rectangle2D;

class IndexArea {

    private int messageIndex;
    private Rectangle2D area;

    IndexArea(int messageIndex) {
        this.messageIndex = messageIndex;
        area = null;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public Rectangle2D getArea() {
        return area;
    }

    public void setArea(Rectangle2D area) {
        this.area = area;
    }
}
