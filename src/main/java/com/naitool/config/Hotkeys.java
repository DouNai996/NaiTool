package com.naitool.config;

import java.util.List;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import com.naitool.Reference;

public class Hotkeys {
    private static final String HOTKEY_KEY = Reference.MOD_ID + ".config.hotkey";

    public static final ConfigHotkey OPEN_CONFIG_GUI =
            new ConfigHotkey("openConfigGui", "X,N").apply(HOTKEY_KEY);

    public static final ConfigHotkey ELYTRA_BOOST =
            new ConfigHotkey("elytraBoost", "F", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);

    public static final ConfigHotkey GHOST_MINE_TOGGLE =
            new ConfigHotkey("ghostMineToggle", "G").apply(HOTKEY_KEY);

    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            OPEN_CONFIG_GUI,
            ELYTRA_BOOST,
            GHOST_MINE_TOGGLE
    );
}
