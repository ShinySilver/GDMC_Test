package com.amberstonedream.play;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class VillageGenerator extends Generator {

	private static final Material[] ringMaterials = new Material[] { Material.PINK_CONCRETE, Material.BLUE_CONCRETE,
			Material.LIME_CONCRETE, Material.YELLOW_CONCRETE, Material.ORANGE_CONCRETE, Material.RED_CONCRETE };

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

	private ArrayList<Integer> spreadRing(BlockChangeBuffer b, int x, int y, int z, int[][] slopes, int[][] heights) {

		Backtrace current = new Backtrace(x, z, null);
		int biggestLoop = 0;
		int[][] noBacktrack = new int[xw][zw];
		ArrayList<Integer> output = new ArrayList<Integer>();

		int dx, dz;

		outer: // there's now way I'll be using 4 if/break blocks.
		for (;;) {

			// Trying to find another block to spread to.
			for (dx = -1; dx <= 1; dx++) {
				for (dz = -1; dz <= 1; dz++) {

					// Skipping blocks out of scope, lower than the target or with low slope
					if (x + dx < 0 || x + dx >= xw || z + dz < 0 || z + dz >= zw || heights[x + dx][z + dz] < y
							|| slopes[x + dx][z + dz] < 3 || (dx == 0 && dz == 0)) {
						continue;
					}

					// Saving and skipping when we find a loop
					if (noBacktrack[x + dx][z + dz] != 0) {
						// Big approximation
						biggestLoop = Math.max(biggestLoop, Math.abs(current.rank - noBacktrack[x + dx][z + dz]));
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
								output.addAll(Arrays.asList(new Integer[] { x + dx, y, z + dz }));
								x = x + dx;
								z = z + dz;
								continue outer;
							}
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
			} else if (biggestLoop > 32) {
				return output;
			} else {
				Bukkit.getLogger().info("size: "+output.size()+";maxloop: " + biggestLoop);
				return null;
			}
		}
	}

	@Override
	public void generateAsync(CommandSender s, BlockChangeBuffer b, int[][] slopeMap, int[][] heightMap,
			int[][] treeMap, int[][] waterMap, int[][] biomeMap, int[][] terraformedMap, int[][] terraformedSlopeMap) {

		long time = System.currentTimeMillis();
		s.sendMessage("Starting the async work!");

		s.sendMessage("Generating Terrain...");
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (waterMap[x][z] != 0) {
					b.setBlock(x0 + x, waterMap[x][z] + 100, z0 + z, Material.BLUE_STAINED_GLASS);
				}
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
		s.sendMessage("Done generating terrain!");

		s.sendMessage("Montain detection...");
		int[][] isRing = new int[xw][zw];
		int i, j, k;
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (isRing[x][z] == 0 && terraformedSlopeMap[x][z] > 2 && terraformedMap[x][z] >= 62) {
					ArrayList<Integer> ring = spreadRing(b, x, terraformedMap[x][z], z, terraformedSlopeMap,
							terraformedMap);
					if (ring != null) {
						for (int index = 0; index < ring.size(); index += 3) {
							i = ring.get(index);
							j = ring.get(index + 1);
							k = ring.get(index + 2);
							b.setBlock(x0 + i, j + 100, z0 + k, ringMaterials[j % ringMaterials.length]);
						}
					}
				}
			}
		}
		s.sendMessage("Done detecting mountain!");
		s.sendMessage("Done the async work! Time taken: " + ((int) (System.currentTimeMillis() - time)) / 1000.0
				+ " seconds");
		s.sendMessage("We are now waiting for the block placement to end...");
	}
}
