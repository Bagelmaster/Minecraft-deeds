package com.bagelmaster.deeds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (!isHoldingTool(player, hand)) {
				return ActionResult.PASS;
			}
			if (player instanceof ServerPlayerEntity serverPlayer) {
				setCorner(serverPlayer, world, pos, true);
			}
			return ActionResult.SUCCESS;
		});

		// Right-click = second corner. Returning SUCCESS also stops chests, doors etc. from opening.
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!isHoldingTool(player, hand)) {
				return ActionResult.PASS;
			}
			if (player instanceof ServerPlayerEntity serverPlayer) {
				setCorner(serverPlayer, world, hitResult.getBlockPos(), false);
			}
			return ActionResult.SUCCESS;
		});
	}

	/** Returns the player's current selection, or {@code null} if they haven't clicked anything. */
	public static Selection getSelection(ServerPlayerEntity player) {
		return SELECTIONS.get(player.getUuid());
	}

	/** Forgets the player's selection, e.g. after a successful claim. */
	public static void clearSelection(ServerPlayerEntity player) {
		SELECTIONS.remove(player.getUuid());
	}

	private static boolean isHoldingTool(PlayerEntity player, Hand hand) {
		// Only react to the main hand, otherwise one click fires twice (once per hand).
		return hand == Hand.MAIN_HAND && player.getStackInHand(hand).isOf(ITEM);
	}

	private static void setCorner(ServerPlayerEntity player, World world, BlockPos pos, boolean first) {
		String dimension = world.getRegistryKey().getValue().toString();
		Selection selection = SELECTIONS.computeIfAbsent(player.getUuid(), uuid -> new Selection());

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
		player.sendMessage(Text.literal(which + " corner set at " + pos.getX() + ", " + pos.getZ() + "." + next), false);
	}
}
