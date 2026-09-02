package com.bagelmaster.deeds;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

/**
 * All claims on the server, saved to disk so they survive restarts.
 *
 * <p>Minecraft's {@link PersistentState} system handles the actual file I/O. We only need to:
 * <ul>
 *   <li>describe how to convert this object to and from NBT (the {@link #CODEC}), and</li>
 *   <li>call {@link #markDirty()} whenever something changes so Minecraft knows to save it.</li>
 * </ul>
 *
 * <p>The data ends up in {@code <world>/data/deeds.dat}. We keep a single state for the whole
 * server (stored with the Overworld); each plot remembers its own dimension, so claims in
 * the Nether and End live in the same file.
 */
public class DeedState extends PersistentState {
	/** On disk, claims are stored as a plain list. */
	public static final Codec<DeedState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Claim.CODEC.listOf().fieldOf("claims").forGetter(state -> state.claims)
	).apply(instance, DeedState::fromList));

	/**
	 * Tells Minecraft the file name ("deeds"), how to make an empty state, and how to
	 * read/write it. The last argument is for Mojang's data fixers, which we don't use.
	 */
	public static final PersistentStateType<DeedState> TYPE = new PersistentStateType<>(
			Deeds.MOD_ID,
			DeedState::new,
			CODEC,
			null
	);

	/**
	 * Every claim on the server. A plain list is fine for a small server: looking up a
	 * position just walks the list.
	 */
	private final List<Claim> claims = new ArrayList<>();

	/** Creates an empty state. Used when the server has no deeds.dat yet. */
	public DeedState() {
	}

	/** Fetches the shared claim data for this server, loading it from disk the first time. */
	public static DeedState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
	}

	/** Returns the claim covering the block column at (x, z), or {@code null} if nobody owns it. */
	public Claim getClaimAt(String dimension, int x, int z) {
		for (Claim claim : claims) {
			if (claim.plot().contains(dimension, x, z)) {
				return claim;
			}
		}
		return null;
	}

	/** Returns an existing claim that overlaps the plot, or {@code null} if the plot is free. */
	public Claim findOverlapping(Plot plot) {
		for (Claim claim : claims) {
			if (claim.plot().overlaps(plot)) {
				return claim;
			}
		}
		return null;
	}

	/** Records a new claim. Callers must check for overlaps first. */
	public void add(Claim claim) {
		claims.add(claim);
		// Without this, Minecraft would assume nothing changed and skip saving.
		markDirty();
	}

	private static DeedState fromList(List<Claim> list) {
		DeedState state = new DeedState();
		state.claims.addAll(list);
		return state;
	}
}
