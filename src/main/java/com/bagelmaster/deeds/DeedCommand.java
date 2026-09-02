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
 *   /deed claim   claims the chunk you are standing in
 *   /deed info    shows who owns the chunk you are standing in
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
		ChunkKey chunk = ChunkKey.of(source.getWorld(), player.getChunkPos());

		DeedState deeds = DeedState.get(source.getServer());
		Claim existing = deeds.getClaim(chunk);

		if (existing != null) {
			if (existing.isOwnedBy(player)) {
				source.sendError(Text.literal("You already own this chunk."));
			} else {
				source.sendError(Text.literal("This chunk is already claimed by " + existing.ownerName() + "."));
			}
			return 0;
		}

		deeds.claim(chunk, player);
		source.sendFeedback(() -> Text.literal("You now own " + chunk + "."), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		ChunkKey chunk = ChunkKey.of(source.getWorld(), player.getChunkPos());

		Claim claim = DeedState.get(source.getServer()).getClaim(chunk);

		if (claim == null) {
			source.sendFeedback(() -> Text.literal("Unclaimed: " + chunk), false);
		} else {
			source.sendFeedback(() -> Text.literal("Owner: " + claim.ownerName() + " (" + chunk + ")"), false);
		}
		return Command.SINGLE_SUCCESS;
	}
}
