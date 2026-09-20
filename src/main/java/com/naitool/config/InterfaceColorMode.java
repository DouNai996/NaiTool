package com.naitool.config;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum InterfaceColorMode implements IConfigOptionListEntry {
    RAINBOW("rainbow", "naitool.config.generic.interfaceColorMode.rainbow"),
    STATIC("static", "naitool.config.generic.interfaceColorMode.static"),
    FADE("fade", "naitool.config.generic.interfaceColorMode.fade"),
    GRADIENT("gradient", "naitool.config.generic.interfaceColorMode.gradient");

    private final String stringValue;
    private final String displayNameKey;

    InterfaceColorMode(String stringValue, String displayNameKey) {
        this.stringValue = stringValue;
        this.displayNameKey = displayNameKey;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public String getDisplayName() {
        return StringUtils.translate(displayNameKey);
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        int id = ordinal();
        id += forward ? 1 : -1;
        InterfaceColorMode[] values = values();
        return values[(id + values.length) % values.length];
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        for (InterfaceColorMode mode : values()) {
            if (mode.stringValue.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return RAINBOW;
    }
}
