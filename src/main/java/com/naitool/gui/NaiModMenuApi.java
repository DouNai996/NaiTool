package com.naitool.gui;

import com.naitool.ui.clickgui.ClickGuiScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** ModMenu 集成：在 Mods 列表点 NaiTool 的「配置」打开 Compose ClickGui。 */
public class NaiModMenuApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClickGuiScreen::new;
    }
}
