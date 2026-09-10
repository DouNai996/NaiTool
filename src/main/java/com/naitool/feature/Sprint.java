package com.naitool.feature;

import net.minecraft.client.Minecraft;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import com.naitool.config.Configs;
import com.naitool.event.ScreenTracker;

public final class Sprint {
    private Sprint() {}

    public enum Mode implements IConfigOptionListEntry {
        STRICT("strict", "naitool.config.generic.sprintMode.strict"),
        RAGE("rage", "naitool.config.generic.sprintMode.rage");

        private final String stringValue;
        private final String displayNameKey;

        Mode(String stringValue, String displayNameKey) {
            this.stringValue = stringValue;
            this.displayNameKey = displayNameKey;
        }

        @Override
        public String getStringValue() {
            return stringValue;
        }

        @Override
        public String getDisplayName() {
            return fi.dy.masa.malilib.util.StringUtils.translate(displayNameKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int id = ordinal();
            if (forward) {
                id++;
            } else {
                id--;
            }
            Mode[] values = values();
            return values[(id + values.length) % values.length];
        }

        @Override
        public IConfigOptionListEntry fromString(String value) {
            for (Mode mode : values()) {
                if (mode.stringValue.equalsIgnoreCase(value)) {
                    return mode;
                }
            }
            return STRICT;
        }
    }

    public static void tick() {
        if (!Configs.Generic.SPRINT_ENABLED.getBooleanValue()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || ScreenTracker.getCurrentScreen() != null) {
            return;
        }
        if (mc.player.isInWater() || mc.player.isMobilityRestricted()) {
            return;
        }

        Mode mode = (Mode) Configs.Generic.SPRINT_MODE.getOptionListValue();

        if (mode == Mode.STRICT) {
            if (mc.player.zza > 0.8F && mc.player.getFoodData().hasEnoughFood()) {
                mc.player.setSprinting(true);
            }
        } else {
            float movement = Math.abs(mc.player.zza) + Math.abs(mc.player.xxa);
            if (movement > (mc.player.isUnderWater() ? 1.0E-5F : 0.8F)) {
                mc.player.setSprinting(true);
            }
        }
    }

    public static boolean isEnabled() {
        return Configs.Generic.SPRINT_ENABLED.getBooleanValue();
    }

    public static Mode getActiveMode() {
        return (Mode) Configs.Generic.SPRINT_MODE.getOptionListValue();
    }

    public static boolean isRageActive() {
        if (!isEnabled()) {
            return false;
        }
        return getActiveMode() == Mode.RAGE;
    }
}
