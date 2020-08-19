package org.esa.smos.dataio.smos.dddb;


import java.awt.Color;

public class FlagDescriptorImpl implements FlagDescriptor {

    private final boolean visible;
    private final String flagName;
    private final int mask;
    private final Color color;
    private final double transparency;
    private final String description;

    public FlagDescriptorImpl(String[] tokens) {
        visible = TokenParser.parseBoolean(tokens[0], false);
        flagName = TokenParser.parseString(tokens[1]);
        mask = TokenParser.parseHex(tokens[2], 0);
        color = TokenParser.parseColor(tokens[3], null);
        transparency = TokenParser.parseDouble(tokens[4], TRANSPARENCY_DEFAULT);
        description = TokenParser.parseString(tokens[5], DESCRIPTION_DEFAULT);
    }

    @Override
    public final String getFlagName() {
        return flagName;
    }

    @Override
    public final int getMask() {
        return mask;
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    @Override
    public final Color getColor() {
        return color;
    }

    @Override
    public final double getTransparency() {
        return transparency;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public boolean evaluate(int flags) {
        return (mask & flags) == mask;
    }
}