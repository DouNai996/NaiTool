package com.naitool.event;

import net.minecraft.client.gui.screens.Screen;

public class ScreenTracker {
    private static Screen currentScreen;

    public static Screen getCurrentScreen() {
        return currentScreen;
    }

    public static void setCurrentScreen(Screen screen) {
        currentScreen = screen;
    }
}
