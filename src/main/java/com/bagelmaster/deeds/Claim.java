package com.bagelmaster.deeds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * One claimed chunk: where it is and who owns it.
 *
 * <p>This is a Java record, so it is immutable and gets equals/hashCode/toString for free.
 * The {@link #CODEC} describes how to turn a Claim into NBT (and back) so it can be written
 * to disk by {@link DeedState}.
 */
public record Claim(ChunkKey chunk, String ownerUuid, String ownerName) {
	public static final Codec<Claim> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("dimension").forGetter(claim -> claim.chunk().dimension()),
			Codec.INT.fieldOf("chunk_x").forGetter(claim -> claim.chunk().chunkX()),
			Codec.INT.fieldOf("chunk_z").forGetter(claim -> claim.chunk().chunkZ()),
			Codec.STRING.fieldOf("owner_uuid").forGetter(Claim::ownerUuid),
			Codec.STRING.fieldOf("owner_name").forGetter(Claim::ownerName)
	).apply(instance, (dimension, chunkX, chunkZ, ownerUuid, ownerName) ->
			new Claim(new ChunkKey(dimension, chunkX, chunkZ), ownerUuid, ownerName)));

	/** Creates a claim for the given chunk owned by the given player. */
	public static Claim of(ChunkKey chunk, ServerPlayerEntity owner) {
		// We store the name as well as the UUID so "/deed info" can print a readable name
		// without having to look the player up (they might be offline).
		return new Claim(chunk, owner.getUuidAsString(), owner.getName().getString());
	}

	/** True if the given player is the owner of this claim. */
	public boolean isOwnedBy(ServerPlayerEntity player) {
		return ownerUuid.equals(player.getUuidAsString());
	}
}
