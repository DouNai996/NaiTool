package com.naitool.config;

import net.minecraft.client.Minecraft;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import com.naitool.feature.ElytraBoost;
import com.naitool.ui.clickgui.ClickGuiScreen;
import com.naitool.ui.hud.FeatureNotifier;

public class Callbacks {
    public static void init(Minecraft mc) {
        IHotkeyCallback callbackGeneric = new KeyCallbackHotkeysGeneric(mc);

        Hotkeys.OPEN_CONFIG_GUI.getKeybind().setCallback(callbackGeneric);
        Hotkeys.ELYTRA_BOOST.getKeybind().setCallback(callbackGeneric);
        Hotkeys.GHOST_MINE_TOGGLE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.NO_FALL_TOGGLE.getKeybind().setCallback(callbackGeneric);
    }

    private record KeyCallbackHotkeysGeneric(Minecraft mc) implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (key == Hotkeys.OPEN_CONFIG_GUI.getKeybind()) {
                mc.setScreen(new ClickGuiScreen());
                return true;
            } else if (key == Hotkeys.ELYTRA_BOOST.getKeybind()) {
                if (ElytraBoost.boost()) {
                    FeatureNotifier.onBoost(Configs.Generic.ELYTRA_BOOST_MESSAGE.getStringValue());
                }
                return true;
            } else if (key == Hotkeys.GHOST_MINE_TOGGLE.getKeybind()) {
                boolean enabled = !Configs.Generic.GHOST_MINE_ENABLED.getBooleanValue();
                Configs.Generic.GHOST_MINE_ENABLED.setBooleanValue(enabled);
                FeatureNotifier.onFeatureToggled("ghostMine", enabled);
                return true;
            } else if (key == Hotkeys.NO_FALL_TOGGLE.getKeybind()) {
                boolean enabled = !Configs.Generic.NO_FALL_ENABLED.getBooleanValue();
                Configs.Generic.NO_FALL_ENABLED.setBooleanValue(enabled);
                FeatureNotifier.onFeatureToggled("noFall", enabled);
                return true;
            }
            return false;
        }
    }
}
