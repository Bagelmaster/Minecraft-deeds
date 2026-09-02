package com.bagelmaster.deeds;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

/**
 * Identifies one chunk in one dimension, e.g. chunk (3, -7) in "minecraft:overworld".
 *
 * <p>Chunk coordinates alone are not enough because the Overworld, Nether and End each
 * have their own chunk (0, 0). Records compare by value, so two ChunkKeys with the same
 * dimension and coordinates are equal, which makes this a good map key.
 */
public record ChunkKey(String dimension, int chunkX, int chunkZ) {
	/** Builds a key for a chunk in the given world. */
	public static ChunkKey of(ServerWorld world, ChunkPos pos) {
		// getRegistryKey().getValue() is the dimension id, such as "minecraft:the_nether".
		String dimension = world.getRegistryKey().getValue().toString();
		return new ChunkKey(dimension, pos.x, pos.z);
	}

	@Override
	public String toString() {
		return "chunk (" + chunkX + ", " + chunkZ + ") in " + dimension;
	}
}
