package com.naitool.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import com.naitool.Reference;
import com.naitool.config.Configs;
import com.naitool.config.Hotkeys;

public class GuiConfigs extends GuiConfigsBase {
    private static int currentTab = Tab.ALL.ordinal();

    public GuiConfigs() {
        super(10, 50, Reference.MOD_ID, null, "naitool.gui.title.configs");
    }

    @Override
    public void initGui() {
        super.initGui();

        int x = 10;
        int y = 30;

        for (Tab tab : Tab.values()) {
            ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, StringUtils.translate(tab.translationKey));
            button.setEnabled(currentTab != tab.ordinal());
            addButton(button, new TabButtonListener(tab, this));
            x += button.getWidth() + 2;
        }
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        switch (Tab.values()[currentTab]) {
            case ALL:
                return ImmutableList.<ConfigOptionWrapper>builder()
                        .add(new ConfigOptionWrapper(StringUtils.translate("naitool.gui.label.generic")))
                        .addAll(ConfigOptionWrapper.createFor(Configs.Generic.OPTIONS))
                        .add(new ConfigOptionWrapper(StringUtils.translate("naitool.gui.label.hotkeys")))
                        .addAll(ConfigOptionWrapper.createFor(Hotkeys.HOTKEY_LIST))
                        .build();
            case GENERIC:
                return ConfigOptionWrapper.createFor(Configs.Generic.OPTIONS);
            case HOTKEYS:
                return ConfigOptionWrapper.createFor(Hotkeys.HOTKEY_LIST);
        }
        return ImmutableList.of();
    }

    private static class TabButtonListener implements IButtonActionListener {
        private final Tab tab;
        private final GuiConfigs gui;

        TabButtonListener(Tab tab, GuiConfigs gui) {
            this.tab = tab;
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            if (currentTab != tab.ordinal()) {
                currentTab = tab.ordinal();
                gui.initGui();
            }
        }
    }

    public enum Tab {
        ALL("naitool.gui.tab.all"),
        GENERIC("naitool.gui.tab.generic"),
        HOTKEYS("naitool.gui.tab.hotkeys");

        final String translationKey;

        Tab(String translationKey) {
            this.translationKey = translationKey;
        }
    }
}
