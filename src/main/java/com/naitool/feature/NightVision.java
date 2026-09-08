package com.naitool.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import com.naitool.config.Configs;

public final class NightVision {
    private NightVision() {}

    public enum Mode implements IConfigOptionListEntry {
        GAMMA("gamma", "naitool.config.generic.nightVisionMode.gamma"),
        POTION("potion", "naitool.config.generic.nightVisionMode.potion");

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
            return GAMMA;
        }
    }

    private static boolean enabled = false;
    private static Mode currentMode = Mode.GAMMA;

    private static boolean lastEnabled = false;
    private static Mode lastMode = Mode.GAMMA;

    public static void tick() {
        boolean configEnabled = Configs.Generic.NIGHT_VISION_ENABLED.getBooleanValue();
        Mode configMode = (Mode) Configs.Generic.NIGHT_VISION_MODE.getOptionListValue();

        if (configEnabled == lastEnabled && configMode == lastMode) {
            return;
        }

        boolean wasEnabled = lastEnabled;
        Mode prevMode = lastMode;
        lastEnabled = configEnabled;
        lastMode = configMode;
        enabled = configEnabled;
        currentMode = configMode;

        if (!wasEnabled && configEnabled) {
            if (configMode == Mode.POTION) {
                applyNightVision();
            }
            Minecraft.getInstance().levelExtractor.allChanged();
        } else if (wasEnabled && !configEnabled) {
            if (prevMode == Mode.POTION) {
                removeNightVision();
            }
            Minecraft.getInstance().levelExtractor.allChanged();
        } else if (configEnabled && prevMode != configMode) {
            if (prevMode == Mode.POTION) {
                removeNightVision();
            }
            if (configMode == Mode.POTION) {
                applyNightVision();
            }
            Minecraft.getInstance().levelExtractor.allChanged();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isGammaActive() {
        return enabled && currentMode == Mode.GAMMA;
    }

    private static void applyNightVision() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 99999, 0, false, false));
    }

    private static void removeNightVision() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
