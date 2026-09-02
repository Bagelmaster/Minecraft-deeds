package com.bagelmaster.deeds;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
				dispatcher.register(Commands.literal("deed")
						.then(Commands.literal("claim").executes(DeedCommand::claim))
						.then(Commands.literal("info").executes(DeedCommand::info))));
	}

	private static int claim(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			// The command was run from the server console or a command block.
			source.sendFailure(Component.literal("Only players can claim plots."));
			return 0;
		}

		SurveyTool.Selection selection = SurveyTool.getSelection(player);
		if (selection == null || !selection.isComplete()) {
			source.sendFailure(Component.literal("Select a plot first: with a stick, left-click one corner and right-click the opposite corner."));
			return 0;
		}

		Plot plot = Plot.fromCorners(selection.dimension, selection.first, selection.second);
		DeedState deeds = DeedState.get(source.getServer());

		Claim overlapping = deeds.findOverlapping(plot);
		if (overlapping != null) {
			if (overlapping.isOwnedBy(player)) {
				source.sendFailure(Component.literal("That overlaps a plot you already own."));
			} else {
				source.sendFailure(Component.literal("That overlaps a plot owned by " + overlapping.ownerName() + "."));
			}
			return 0;
		}

		deeds.add(Claim.of(plot, player));
		SurveyTool.clearSelection(player);
		source.sendSuccess(() -> Component.literal("You now own " + plot + "."), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int info(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.literal("Only players can check plots."));
			return 0;
		}

		String dimension = source.getLevel().dimension().identifier().toString();
		Claim claim = DeedState.get(source.getServer()).getClaimAt(dimension, player.getBlockX(), player.getBlockZ());

		if (claim == null) {
			source.sendSuccess(() -> Component.literal("Unclaimed."), false);
		} else {
			source.sendSuccess(() -> Component.literal("Owner: " + claim.ownerName() + " (" + claim.plot() + ")"), false);
		}
		return Command.SINGLE_SUCCESS;
	}
}
