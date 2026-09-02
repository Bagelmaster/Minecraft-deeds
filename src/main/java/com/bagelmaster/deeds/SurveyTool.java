package com.bagelmaster.deeds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

/**
 * The survey tool: a plain stick, for now.
 *
 * <p>With a stick in your main hand, left-click a block to set the first corner of a plot
 * and right-click a block to set the second corner. Then run {@code /deed claim}.
 *
 * <p>Selections only live in memory (they are not saved), and each player has their own.
 */
public final class SurveyTool {
	/** The item that acts as the survey tool. Swap this for a custom item later. */
	public static final Item ITEM = Items.STICK;

	/** Each player's in-progress selection, by player UUID. */
	private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();

	/** The two corners a player has picked so far. Either can still be missing. */
	public static final class Selection {
		public String dimension;
		public BlockPos first;
		public BlockPos second;

		public boolean isComplete() {
			return first != null && second != null;
		}
	}

	private SurveyTool() {
	}

	/** Hooks the tool into Fabric's block click events. Called once from {@link Deeds}. */
	public static void register() {
		// Left-click = first corner. Returning SUCCESS also stops the block from being broken.
		AttackBlockCallback.EVENT.register((player, level, hand, pos, side) -> {
			if (!isHoldingTool(player, hand)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer serverPlayer) {
				setCorner(serverPlayer, level, pos, true);
			}
			return InteractionResult.SUCCESS;
		});

		// Right-click = second corner. Returning SUCCESS also stops chests, doors etc. from opening.
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (!isHoldingTool(player, hand)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer serverPlayer) {
				setCorner(serverPlayer, level, hitResult.getBlockPos(), false);
			}
			return InteractionResult.SUCCESS;
		});
	}

	/** Returns the player's current selection, or {@code null} if they haven't clicked anything. */
	public static Selection getSelection(ServerPlayer player) {
		return SELECTIONS.get(player.getUUID());
	}

	/** Forgets the player's selection, e.g. after a successful claim. */
	public static void clearSelection(ServerPlayer player) {
		SELECTIONS.remove(player.getUUID());
	}

	private static boolean isHoldingTool(Player player, InteractionHand hand) {
		// Only react to the main hand, otherwise one click fires twice (once per hand).
		return hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).is(ITEM);
	}

	private static void setCorner(ServerPlayer player, Level level, BlockPos pos, boolean first) {
		String dimension = level.dimension().identifier().toString();
		Selection selection = SELECTIONS.computeIfAbsent(player.getUUID(), uuid -> new Selection());

		// A plot can't span dimensions, so switching dimension starts a fresh selection.
		if (!dimension.equals(selection.dimension)) {
			selection.first = null;
			selection.second = null;
			selection.dimension = dimension;
		}

		if (first) {
			selection.first = pos;
		} else {
			selection.second = pos;
		}

		String which = first ? "First" : "Second";
		String next = selection.isComplete()
				? " Run /deed claim to claim it."
				: (first ? " Now right-click the opposite corner." : " Now left-click the opposite corner.");
		player.sendSystemMessage(Component.literal(which + " corner set at " + pos.getX() + ", " + pos.getZ() + "." + next));
	}
}
