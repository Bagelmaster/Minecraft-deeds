package com.bagelmaster.deeds;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mod's entry point. Fabric calls {@link #onInitialize()} once while the game is starting,
 * on both the dedicated server and the integrated (singleplayer) server.
 */
public class Deeds implements ModInitializer {
	public static final String MOD_ID = "deeds";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		SurveyTool.register();
		DeedCommand.register();

		LOGGER.info("Deeds loaded. Select a plot with a stick, then /deed claim.");
	}
}
