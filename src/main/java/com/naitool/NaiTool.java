package com.naitool;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dy.masa.malilib.event.InitializationHandler;

public class NaiTool implements ModInitializer {
	public static final String MOD_ID = "naitool";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world! NaiTool initializing with MaLiLib...");

		InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
