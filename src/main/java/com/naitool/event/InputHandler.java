package com.naitool.event;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import com.naitool.Reference;
import com.naitool.config.Configs;
import com.naitool.config.Hotkeys;

public class InputHandler implements IKeybindProvider {
    private static final InputHandler INSTANCE = new InputHandler();

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (var hotkey : Hotkeys.HOTKEY_LIST) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_NAME,
                "naitool.hotkeys.category.generic_hotkeys",
                Hotkeys.HOTKEY_LIST);
    }
}
