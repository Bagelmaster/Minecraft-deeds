package com.bagelmaster.deeds;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * The {@code /deed} command and its sub-commands.
 *
 * <pre>
 *   /deed claim   claims the plot you selected with the survey tool (a stick)
 *   /deed info    shows who owns the plot you are standing in
 * </pre>
 */
public final class DeedCommand {
	private DeedCommand() {
	}

	/** Hooks our command into the server's command tree. Called once from {@link Deeds}. */
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("deed")
						.then(CommandManager.literal("claim").executes(DeedCommand::claim))
						.then(CommandManager.literal("info").executes(DeedCommand::info))));
	}

	private static int claim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		// Throws a friendly "must be run by a player" error if run from the console.
		ServerPlayerEntity player = source.getPlayerOrThrow();

		SurveyTool.Selection selection = SurveyTool.getSelection(player);
		if (selection == null || !selection.isComplete()) {
			source.sendError(Text.literal("Select a plot first: with a stick, left-click one corner and right-click the opposite corner."));
			return 0;
		}

		Plot plot = Plot.fromCorners(selection.dimension, selection.first, selection.second);
		DeedState deeds = DeedState.get(source.getServer());

		Claim overlapping = deeds.findOverlapping(plot);
		if (overlapping != null) {
			if (overlapping.isOwnedBy(player)) {
				source.sendError(Text.literal("That overlaps a plot you already own."));
			} else {
				source.sendError(Text.literal("That overlaps a plot owned by " + overlapping.ownerName() + "."));
			}
			return 0;
		}

		deeds.add(Claim.of(plot, player));
		SurveyTool.clearSelection(player);
		source.sendFeedback(() -> Text.literal("You now own " + plot + "."), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();

		String dimension = source.getWorld().getRegistryKey().getValue().toString();
		Claim claim = DeedState.get(source.getServer()).getClaimAt(dimension, player.getBlockX(), player.getBlockZ());

		if (claim == null) {
			source.sendFeedback(() -> Text.literal("Unclaimed."), false);
		} else {
			source.sendFeedback(() -> Text.literal("Owner: " + claim.ownerName() + " (" + claim.plot() + ")"), false);
		}
		return Command.SINGLE_SUCCESS;
	}
}
