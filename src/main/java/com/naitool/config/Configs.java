package com.naitool.config;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;
import com.naitool.Reference;
import com.naitool.feature.NightVision;
import com.naitool.feature.Sprint;
import com.naitool.feature.ElytraTrails;

public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";

    private static final String GENERIC_KEY = Reference.MOD_ID + ".config.generic";

    public static class Generic {
        public static final ConfigBoolean ELYTRA_BOOST_ENABLED =
                new ConfigBoolean("elytraBoostEnabled", false).apply(GENERIC_KEY);

        public static final ConfigBoolean ELYTRA_BOOST_DONT_CONSUME =
                new ConfigBoolean("elytraBoostDontConsume", true).apply(GENERIC_KEY);

        public static final ConfigInteger ELYTRA_BOOST_FIREWORK_LEVEL =
                new ConfigInteger("elytraBoostFireworkLevel", 0, 0, 255).apply(GENERIC_KEY);

        public static final ConfigBoolean ELYTRA_BOOST_PLAY_SOUND =
                new ConfigBoolean("elytraBoostPlaySound", true).apply(GENERIC_KEY);

        public static final ConfigBoolean ELYTRA_BOOST_REAL_FIREWORK =
                new ConfigBoolean("elytraBoostRealFirework", false).apply(GENERIC_KEY);

        public static final ConfigBoolean ELYTRA_TRAILS_ENABLED =
                new ConfigBoolean("elytraTrailsEnabled", false).apply(GENERIC_KEY);

        public static final ConfigOptionList ELYTRA_TRAILS_MODE =
                new ConfigOptionList("elytraTrailsMode", ElytraTrails.TrailMode.RAINBOW).apply(GENERIC_KEY);

        public static final ConfigInteger ELYTRA_TRAILS_COLOR =
                new ConfigInteger("elytraTrailsColor", 0x55FF55, 0, 0xFFFFFF).apply(GENERIC_KEY);

        public static final ConfigInteger ELYTRA_TRAILS_DURATION =
                new ConfigInteger("elytraTrailsDuration", 120, 20, 5000).apply(GENERIC_KEY);

        public static final ConfigDouble ELYTRA_TRAILS_SPREAD =
                new ConfigDouble("elytraTrailsSpread", 1.0, 0.0, 10.0).apply(GENERIC_KEY);

        public static final ConfigInteger ELYTRA_TRAILS_DENSITY =
                new ConfigInteger("elytraTrailsDensity", 30, 5, 150).apply(GENERIC_KEY);

        public static final ConfigDouble ELYTRA_TRAILS_Y_OFFSET =
                new ConfigDouble("elytraTrailsYOffset", 0.5, 0.0, 3.0).apply(GENERIC_KEY);

        public static final ConfigBoolean ELYTRA_TRAILS_HIDE_FIREWORKS =
                new ConfigBoolean("elytraTrailsHideFireworks", true).apply(GENERIC_KEY);

        public static final ConfigBoolean NIGHT_VISION_ENABLED =
                new ConfigBoolean("nightVisionEnabled", false).apply(GENERIC_KEY);

        public static final ConfigOptionList NIGHT_VISION_MODE =
                new ConfigOptionList("nightVisionMode", NightVision.Mode.GAMMA).apply(GENERIC_KEY);

        public static final ConfigBoolean SPRINT_ENABLED =
                new ConfigBoolean("sprintEnabled", false).apply(GENERIC_KEY);

        public static final ConfigOptionList SPRINT_MODE =
                new ConfigOptionList("sprintMode", Sprint.Mode.RAGE).apply(GENERIC_KEY);

        public static final ConfigBoolean FREE_CAMERA_ENABLED =
                new ConfigBoolean("freeCameraEnabled", true).apply(GENERIC_KEY);

        public static final ConfigBoolean FREE_CAMERA_BLOCK_INTERACTIONS =
                new ConfigBoolean("freeCameraBlockInteractions", true).apply(GENERIC_KEY);

        public static final ConfigBoolean FREE_CAMERA_ENTITY_INTERACTIONS =
                new ConfigBoolean("freeCameraEntityInteractions", true).apply(GENERIC_KEY);

        public static final ConfigBoolean FREE_CAMERA_EASY_PLACE =
                new ConfigBoolean("freeCameraEasyPlace", true).apply(GENERIC_KEY);

        public static final ImmutableList<@NotNull IConfigBase> OPTIONS = ImmutableList.of(
                ELYTRA_BOOST_ENABLED,
                ELYTRA_BOOST_DONT_CONSUME,
                ELYTRA_BOOST_FIREWORK_LEVEL,
                ELYTRA_BOOST_PLAY_SOUND,
                ELYTRA_BOOST_REAL_FIREWORK,
                ELYTRA_TRAILS_ENABLED,
                ELYTRA_TRAILS_MODE,
                ELYTRA_TRAILS_COLOR,
                ELYTRA_TRAILS_DURATION,
                ELYTRA_TRAILS_SPREAD,
                ELYTRA_TRAILS_DENSITY,
                ELYTRA_TRAILS_HIDE_FIREWORKS,
                ELYTRA_TRAILS_Y_OFFSET,
                NIGHT_VISION_ENABLED,
                NIGHT_VISION_MODE,
                SPRINT_ENABLED,
                SPRINT_MODE,
                FREE_CAMERA_ENABLED,
                FREE_CAMERA_BLOCK_INTERACTIONS,
                FREE_CAMERA_ENTITY_INTERACTIONS,
                FREE_CAMERA_EASY_PLACE
        );
    }

    public static void loadFromFile() {
        Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME);

        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
                ConfigUtils.readConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
            }
        }
    }

    public static void saveToFile() {
        Path dir = FileUtils.getConfigDirectory();

        if (!Files.exists(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);
        }

        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
            JsonUtils.writeJsonToFile(root, dir.resolve(CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }
}