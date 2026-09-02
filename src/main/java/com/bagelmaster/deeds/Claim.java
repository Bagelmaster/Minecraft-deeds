package com.bagelmaster.deeds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * One claimed plot: where it is and who owns it.
 *
 * <p>This is a Java record, so it is immutable and gets equals/hashCode/toString for free.
 * The {@link #CODEC} describes how to turn a Claim into NBT (and back) so it can be written
 * to disk by {@link DeedState}.
 */
public record Claim(Plot plot, String ownerUuid, String ownerName) {
	public static final Codec<Claim> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Plot.CODEC.fieldOf("plot").forGetter(Claim::plot),
			Codec.STRING.fieldOf("owner_uuid").forGetter(Claim::ownerUuid),
			Codec.STRING.fieldOf("owner_name").forGetter(Claim::ownerName)
	).apply(instance, Claim::new));

	/** Creates a claim for the given plot owned by the given player. */
	public static Claim of(Plot plot, ServerPlayerEntity owner) {
		// We store the name as well as the UUID so "/deed info" can print a readable name
		// without having to look the player up (they might be offline).
		return new Claim(plot, owner.getUuidAsString(), owner.getName().getString());
	}

	/** True if the given player is the owner of this claim. */
	public boolean isOwnedBy(ServerPlayerEntity player) {
		return ownerUuid.equals(player.getUuidAsString());
	}
}
