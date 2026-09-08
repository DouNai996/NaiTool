package com.naitool.config;

import net.minecraft.client.Minecraft;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import com.naitool.feature.ElytraBoost;
import com.naitool.gui.GuiConfigs;

public class Callbacks {
    public static void init(Minecraft mc) {
        IHotkeyCallback callbackGeneric = new KeyCallbackHotkeysGeneric(mc);

        Hotkeys.OPEN_CONFIG_GUI.getKeybind().setCallback(callbackGeneric);
        Hotkeys.ELYTRA_BOOST.getKeybind().setCallback(callbackGeneric);
    }

    private record KeyCallbackHotkeysGeneric(Minecraft mc) implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (key == Hotkeys.OPEN_CONFIG_GUI.getKeybind()) {
                GuiBase.openGui(new GuiConfigs());
                return true;
            } else if (key == Hotkeys.ELYTRA_BOOST.getKeybind()) {
                ElytraBoost.boost();
                return true;
            }
            return false;
        }
    }
}
