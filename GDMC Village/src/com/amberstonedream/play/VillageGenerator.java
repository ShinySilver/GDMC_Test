package com.amberstonedream.play;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import com.google.common.primitives.Ints;

public class VillageGenerator extends Generator {

	private class Backtrace {
		int x, z, rank;
		Backtrace previous;

		Backtrace(int x, int z, Backtrace previous) {
			this.x = x;
			this.z = z;
			this.previous = previous;

			if (previous != null) {
				rank = previous.rank + 1;
			} else {
				rank = 0;
			}
		}
	}

	public VillageGenerator(World w, CommandSender s, int x0, int z0, int x1, int z1) {
		super(w, s, x0, z0, x1, z1);
	}

	private int[][] spreadRing(BlockChangeBuffer b, int x, int y, int z, int[][] slopes, int[][] heights,
			boolean[][] explored) {

		Backtrace current = new Backtrace(x, z, null);
		int[][] noBacktrack = new int[xw][zw];
		ArrayList<int[]> output = new ArrayList<int[]>();

		int dx, dz, d;
		int[] dxv = new int[] { 1, 0, -1, 0, 1, 1, -1, -1 };
		int[] dzv = new int[] { 0, 1, 0, -1, -1, 1, 1, -1 };

		outer: // there's now way I'll be using 4 if/break blocks.
		for (;;) {

			// Trying to find another block to spread to.
			for (d = 0; d < 8; d++) {
				dx = dxv[d];
				dz = dzv[d];

				// Skipping blocks out of scope, lower than the target or with low slope
				if (x + dx < 0 || x + dx >= xw || z + dz < 0 || z + dz >= zw || heights[x + dx][z + dz] < y
						|| slopes[x + dx][z + dz] < 3) {
					continue;
				}

				// Saving and skipping when we find a loop
				if (noBacktrack[x + dx][z + dz] != 0) {
					if (current.rank - noBacktrack[x + dx][z + dz] > 32) {
						ArrayList<Integer> loop = new ArrayList<>();
						for (Backtrace pointer = current; pointer.rank >= noBacktrack[x + dx][z
								+ dz]; pointer = pointer.previous) {
							loop.addAll(Arrays.asList(new Integer[] { pointer.x, y, pointer.z }));
						}
						assert loop.size() == current.rank - noBacktrack[x + dx][z + dz];
						output.add(Ints.toArray(loop));
					}
					continue;
				}

				// Checking if this block can see the air, ie. if we are digging into the
				// mountain
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						// Skipping blocks out of scope
						if (x + dx + i < 0 || x + dx + i >= xw || z + dz + j < 0 || z + dz + j >= zw) {
							continue;
						}

						// Is this block 'air'?
						if (heights[x + dx + i][z + dz + j] < y) {
							// If yes, the block is valid.
							current = new Backtrace(x + dx, z + dz, current);
							noBacktrack[x + dx][z + dz] = current.rank;
							if (heights[x + dx][z + dz] == y) {
								explored[x + dx][z + dz] = true;
							}
							x = x + dx;
							z = z + dz;
							continue outer;
						}
					}

				}
			}

			// If we get here, it means we found nothing around the current block. We'll go
			// back if we can, else we'll return
			if (current.previous != null) {
				current = current.previous;
				x = current.x;
				z = current.z;
			}
			return output.toArray(new int[0][0]);
		}
	}

	@Override
	public void generateAsync(CommandSender s, BlockChangeBuffer b, int[][] slopeMap, int[][] heightMap,
			int[][] treeMap, int[][] waterMap, int[][] biomeMap, int[][] terraformedMap, int[][] terraformedSlopeMap,
			Material[][] structureMap) {

		long time = System.currentTimeMillis();
		s.sendMessage("Starting the async work!");

		s.sendMessage("Queuing the drawing of all blocks in feature maps...");
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (waterMap[x][z] != 0) {
					b.setBlock(x0 + x, waterMap[x][z] + 100, z0 + z, Material.BLUE_STAINED_GLASS);
				}
				b.setBlock(x0 + x, heightMap[x][z] + 101, z0 + z,
						structureMap[x][z] == null ? Material.WHITE_STAINED_GLASS : Material.RED_STAINED_GLASS);
				for (int layer = 0; layer < 5; layer++) {
					if (treeMap[x][z] != 0) {
						b.setBlock(x0 + x, treeMap[x][z] + 100 - layer, z0 + z, Material.LIME_TERRACOTTA);
					}
					b.setBlock(x0 + x, terraformedMap[x][z] + 100 - layer, z0 + z, terraformedSlopeMap[x][z] < 3
							? Material.WHITE_TERRACOTTA
							: (terraformedSlopeMap[x][z] < 8 ? Material.YELLOW_TERRACOTTA : Material.RED_TERRACOTTA));
				}
			}
		}
		s.sendMessage("Done queuing the drawings of the feature maps!");

		s.sendMessage("Ring search...");
		boolean[][] explored = new boolean[xw][zw];
		int i, j, k;
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (!explored[x][z] && terraformedSlopeMap[x][z] > 2 && terraformedMap[x][z] >= 62) {
					int[][] ring = spreadRing(b, x, terraformedMap[x][z], z, terraformedSlopeMap, terraformedMap,
							explored);
					for (int[] loop : ring) {
						for (int index = 0; index < loop.length; index += 3) {
							i = loop[index];
							j = loop[index + 1];
							k = loop[index + 2];
							if (Config.DEBUG_RINGS) {
								b.setBlock(x0 + i, j + 100, z0 + k,
										Config.RAINBOW_MATERIALS[j % Config.RAINBOW_MATERIALS.length]);
							}
						}
					}
				}
			}
		}
		s.sendMessage("Done searching ring!");
		s.sendMessage("Mountain top search...");
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				// Giving up
			}
		}
		s.sendMessage("Done searching mountain!");

		s.sendMessage("Done the async work! Time taken: " + ((int) (System.currentTimeMillis() - time)) / 1000.0
				+ " seconds");
		s.sendMessage("We are now waiting for the block placement to end...");
	}
}
