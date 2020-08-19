package org.esa.smos.dataio.smos.dddb;

import java.awt.*;

/**
 * Special implementation of a flag descriptor that handles combined flags, i.e. flags that
 * do not follow the pattern one bit - one Flag but instead use a subset of the flag mask to define local number patterns.
 * As this class is (currently) only used in the FlagMatrix view, a number of fields just return default values.
 */

public class FlagDescriptorCombined implements FlagDescriptor {

    private final String flagName;
    private final int mask;
    private final int value;

    public FlagDescriptorCombined(String[] tokens) {
        flagName = TokenParser.parseString(tokens[0]);
        mask = TokenParser.parseHex(tokens[1], 0);
        value = TokenParser.parseInt(tokens[2], -1);
    }

    @Override
    public String getFlagName() {
        return flagName;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public double getTransparency() {
        return TRANSPARENCY_DEFAULT;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION_DEFAULT;
    }

    @Override
    public boolean evaluate(int flags) {
        final int flagSet = flags & mask;
        return flagSet == value;
    }
}
