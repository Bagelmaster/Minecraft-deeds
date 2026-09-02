package com.bagelmaster.deeds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.BlockPos;

/**
 * A rectangle of land in one dimension, from (minX, minZ) to (maxX, maxZ) inclusive.
 *
 * <p>Plots cover every Y level, so you only ever think about X and Z. Records compare
 * by value, so two Plots with the same numbers are equal.
 */
public record Plot(String dimension, int minX, int minZ, int maxX, int maxZ) {
	public static final Codec<Plot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("dimension").forGetter(Plot::dimension),
			Codec.INT.fieldOf("min_x").forGetter(Plot::minX),
			Codec.INT.fieldOf("min_z").forGetter(Plot::minZ),
			Codec.INT.fieldOf("max_x").forGetter(Plot::maxX),
			Codec.INT.fieldOf("max_z").forGetter(Plot::maxZ)
	).apply(instance, Plot::new));

	/** Builds a plot from two opposite corners, in any order. */
	public static Plot fromCorners(String dimension, BlockPos first, BlockPos second) {
		return new Plot(
				dimension,
				Math.min(first.getX(), second.getX()),
				Math.min(first.getZ(), second.getZ()),
				Math.max(first.getX(), second.getX()),
				Math.max(first.getZ(), second.getZ())
		);
	}

	/** True if the block column at (x, z) in the given dimension is inside this plot. */
	public boolean contains(String dimension, int x, int z) {
		return this.dimension.equals(dimension)
				&& x >= minX && x <= maxX
				&& z >= minZ && z <= maxZ;
	}

	/** True if the two plots share at least one block column. */
	public boolean overlaps(Plot other) {
		if (!dimension.equals(other.dimension)) {
			return false;
		}
		// Two rectangles overlap unless one is entirely to the left/right/front/back of the other.
		return minX <= other.maxX && maxX >= other.minX
				&& minZ <= other.maxZ && maxZ >= other.minZ;
	}

	/** Size along X, in blocks. */
	public int width() {
		return maxX - minX + 1;
	}

	/** Size along Z, in blocks. */
	public int length() {
		return maxZ - minZ + 1;
	}

	@Override
	public String toString() {
		return "plot (" + minX + ", " + minZ + ") to (" + maxX + ", " + maxZ + ") in " + dimension
				+ " [" + width() + "x" + length() + " blocks]";
	}
}
