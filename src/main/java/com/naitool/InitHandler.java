package com.naitool;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import com.naitool.config.Callbacks;
import com.naitool.config.Configs;
import com.naitool.event.InputHandler;
import com.naitool.gui.GuiConfigs;
import net.minecraft.client.Minecraft;

public class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());

        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(Reference.MOD_ID, Reference.MOD_NAME, GuiConfigs::new));

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());

        Callbacks.init(Minecraft.getInstance());

        NaiTool.LOGGER.info("NaiTool mod handlers registered.");
    }
}
