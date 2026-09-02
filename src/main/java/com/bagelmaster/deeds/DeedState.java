package com.bagelmaster.deeds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
 * server (stored with the Overworld) and use the dimension as part of each key, so claims in
 * the Nether and End live in the same file.
 */
public class DeedState extends PersistentState {
	/** On disk, claims are stored as a plain list. */
	public static final Codec<DeedState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Claim.CODEC.listOf().fieldOf("claims").forGetter(DeedState::toList)
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

	/** In memory, claims are kept in a map so lookups by chunk are fast. */
	private final Map<ChunkKey, Claim> claims = new HashMap<>();

	/** Creates an empty state. Used when the server has no deeds.dat yet. */
	public DeedState() {
	}

	/** Fetches the shared claim data for this server, loading it from disk the first time. */
	public static DeedState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
	}

	/** Returns the claim for the chunk, or {@code null} if nobody owns it. */
	public Claim getClaim(ChunkKey chunk) {
		return claims.get(chunk);
	}

	/** Records the chunk as owned by the player. Callers must check it is unclaimed first. */
	public void claim(ChunkKey chunk, ServerPlayerEntity owner) {
		claims.put(chunk, Claim.of(chunk, owner));
		// Without this, Minecraft would assume nothing changed and skip saving.
		markDirty();
	}

	private List<Claim> toList() {
		return new ArrayList<>(claims.values());
	}

	private static DeedState fromList(List<Claim> list) {
		DeedState state = new DeedState();
		for (Claim claim : list) {
			state.claims.put(claim.chunk(), claim);
		}
		return state;
	}
}
