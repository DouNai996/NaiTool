package com.naitool;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import com.naitool.config.Callbacks;
import com.naitool.config.Configs;
import com.naitool.event.InputHandler;
import net.minecraft.client.Minecraft;

public class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());

        // 配置界面已完全替换为 Compose ClickGui，MaLiLib 原版 GuiConfigs 不再注册；
        // ModMenu 入口见 NaiModMenuApi，游戏内热键见 Callbacks.OPEN_CONFIG_GUI。

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());

        Callbacks.init(Minecraft.getInstance());

        NaiTool.LOGGER.info("NaiTool mod handlers registered.");
    }
}
