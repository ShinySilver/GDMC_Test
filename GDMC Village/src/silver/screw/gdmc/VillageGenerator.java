package silver.screw.gdmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import com.google.common.primitives.Ints;

import silver.screw.gdmc.utils.BlockChangeBuffer;

public class VillageGenerator extends Generator {

	public final Random random;

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
		random = new Random();
	}

	@SuppressWarnings("unused")
	private void apply(int[][] objectiveHeightmap, int[][] modifiedHeightmap, int radius, boolean[][] mask) {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {

			}
		}
	}

	private boolean[][] dilate(boolean[][] src, int count) {
		int dx, dz, score;
		boolean[][] tmp, backup = src, copy = new boolean[src.length][src[0].length];
		for (int i = 0; i < count; i++) {
			for (int x = 0; x < xw; x++) {
				for (int z = 0; z < zw; z++) {
					score = 0;
					outer: for (dx = -1; dx <= 1; dx++) {
						for (dz = -1; dz <= 1; dz++) {
							if (x + dx < 0 || x + dx >= xw || z + dz < 0 || z + dz >= zw) {
								continue;
							}
							if (src[x + dx][z + dz]) {
								if (score == 2) {
									copy[x][z] = true;
									break outer;
								}
								score++;
							}
						}
					}
				}
			}
			tmp = src;
			src = copy;
			copy = tmp;
		}

		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				backup[x][z] = src[x][z];
			}
		}
		return src;
	}

	@SuppressWarnings("unused")
	private boolean[][] erode(boolean[][] src, int count) {

		int dx, dz, score;
		boolean[][] copy = new boolean[src.length][src[0].length];

		for (int i = 0; i < count; i++) {
			for (int x = 0; x < xw; x++) {
				for (int z = 0; z < zw; z++) {
					copy[x][z] = src[x][z];
				}
			}
			for (int x = 0; x < xw; x++) {
				for (int z = 0; z < zw; z++) {
					if (!src[x][z])
						continue;
					score = 0;
					outer: for (dx = -1; dx <= 1; dx++) {
						for (dz = -1; dz <= 1; dz++) {
							if (x + dx < 0 || x + dx >= xw || z + dz < 0 || z + dz >= zw) {
								continue;
							}
							if (!src[x + dx][z + dz]) {
								if (score == 3) {
									copy[x][z] = false;
									break outer;
								}
								score++;
							}
						}
					}
				}
			}
			for (int x = 0; x < xw; x++) {
				for (int z = 0; z < zw; z++) {
					src[x][z] = copy[x][z];
				}
			}
		}
		return src;
	}

	private int[][] getRivers(int[][] biomeMap, int[][] waterMap) {
		Stack<Integer> stack = new Stack<>();
		boolean[][] riverMap = new boolean[xw][zw], extendedRiverMap = new boolean[xw][zw];
		int dx, dz, xI, zI;
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (!Biome.values()[biomeMap[x][z]].name().contains("OCEAN")
						&& !Biome.values()[biomeMap[x][z]].name().contains("RIVER")) {
					continue;
				}
				riverMap[x][z] = true;
				if (extendedRiverMap[x][z]) {
					continue;
				}
				xI = x;
				zI = z;
				extendedRiverMap[x][z] = true;

				for (;;) { // Water connected to river/ocean
					for (dx = -1; dx <= 1; dx++) {
						for (dz = -1; dz <= 1; dz++) {
							if (xI + dx < 0 || xI + dx >= xw || zI + dz < 0 || zI + dz >= zw) {
								continue;
							}
							if (!extendedRiverMap[xI + dx][zI + dz] && waterMap[xI + dx][zI + dz] == 62) {
								stack.add(xI + dx);
								stack.add(zI + dz);
								extendedRiverMap[xI + dx][zI + dz] = true;
							}
						}
					}

					if (!stack.isEmpty()) {
						zI = stack.pop();
						xI = stack.pop();
					} else {
						break;
					}
				}

			}
		}
		dilate(riverMap, 6);
		erode(riverMap, 5);

		ArrayList<Integer> currentTier = new ArrayList<>();
		ArrayList<Integer> nextTier = new ArrayList<>();
		int[][] betterRiverMap = new int[xw][zw];
		int dist = 0;

		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				riverMap[x][z] |= extendedRiverMap[x][z];

				if (riverMap[x][z]) {
					betterRiverMap[x][z] = dist;
					currentTier.add(x);
					currentTier.add(z);
				} else {
					betterRiverMap[x][z] = -1;
				}
			}
		}

		do {
			dist += 1;

			int x, z;
			for (int i = 0; i < currentTier.size(); i += 2) {
				x = currentTier.get(i);
				z = currentTier.get(i + 1);

				for (dx = -1; dx <= 1; dx++) {
					for (dz = -1; dz <= 1; dz++) {
						if (x + dx < 0 || x + dx >= xw || z + dz < 0 || z + dz >= zw
								|| Math.abs(dx) + Math.abs(dz) != 1) {
							continue;
						}
						if (betterRiverMap[x + dx][z + dz] == -1) {
							betterRiverMap[x + dx][z + dz] = dist;
							nextTier.add(x + dx);
							nextTier.add(z + dz);
						}
					}
				}
			}
			currentTier = nextTier;
			nextTier = new ArrayList<>();
		} while (!currentTier.isEmpty());

		return betterRiverMap;
	}

	private int[][] spreadRing(int x, int y, int z, int[][] slopes, int[][] heights, boolean[][] explored) {

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

		if (Config.ENABLE_DEBUG) {
			s.sendMessage("Debug is enabled. Queuing the drawing of all blocks in feature maps...");
			for (int x = 0; x < xw; x++) {
				for (int z = 0; z < zw; z++) {
					if (waterMap[x][z] != 0) {
						b.setBlock(x0 + x, waterMap[x][z] + 100, z0 + z, Material.BLUE_STAINED_GLASS);
					}
					b.setBlock(x0 + x, heightMap[x][z] + 101, z0 + z,
							structureMap[x][z] == null ? Material.AIR : Material.BLACK_STAINED_GLASS);
					for (int layer = 0; layer < 5; layer++) {
						if (treeMap[x][z] != 0) {
							b.setBlock(x0 + x, treeMap[x][z] + 100 - layer, z0 + z, Material.LIME_TERRACOTTA);
						}
						b.setBlock(x0 + x, terraformedMap[x][z] + 100 - layer, z0 + z,
								terraformedSlopeMap[x][z] < 3 ? Material.WHITE_TERRACOTTA
										: (terraformedSlopeMap[x][z] < 8 ? Material.YELLOW_TERRACOTTA
												: Material.RED_TERRACOTTA));
					}
				}
			}
			s.sendMessage("Done queuing the drawings of the feature maps!");
		}

		if (Config.COMPUTE_EXPERIMENTAL_MOUNTAINS) {
			s.sendMessage("Experimental Mountain Search enabled. Ring search...");
			boolean[][] explored = new boolean[xw][zw];
			int i, j, k;
			for (int x = 0; x < xw; x++) {
				for (int z = 0; z < zw; z++) {
					if (!explored[x][z] && terraformedSlopeMap[x][z] > 2 && terraformedMap[x][z] >= 62) {
						int[][] ring = spreadRing(x, terraformedMap[x][z], z, terraformedSlopeMap, terraformedMap,
								explored);
						for (int[] loop : ring) {
							for (int index = 0; index < loop.length; index += 3) {
								i = loop[index];
								j = loop[index + 1];
								k = loop[index + 2];
								if (Config.ENABLE_DEBUG) {
									b.setBlock(x0 + i, j + 100, z0 + k,
											Config.RAINBOW_MATERIALS[j % Config.RAINBOW_MATERIALS.length]);
								}
							}
						}
					}
				}
			}
			s.sendMessage("Done searching rings!");
			s.sendMessage("Mountain top search...");
			for (int x = 0; x < xw; x++) {
				for (int z = 0; z < zw; z++) {
					// Giving up
				}
			}
			s.sendMessage("Done searching mountains!");
		}

		s.sendMessage("Mapping rivers & oceans...");
		int[][] riverMap = getRivers(biomeMap, waterMap);
		int y;
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (riverMap[x][z] == 0) {
					b.setBlock(x0 + x, 62, z0 + z, Material.WATER);
					y = 63;
					while (y <= heightMap[x][z] || y <= treeMap[x][z]) {
						b.setBlock(x0 + x, y, z0 + z, Material.AIR);
						y++;
					}
				} else if (riverMap[x][z] > 0) {
					y = 62 + riverMap[x][z];
					while (y <= heightMap[x][z] || y <= treeMap[x][z]) {
						b.setBlock(x0 + x, y, z0 + z, Material.AIR);
						y++;
					}
				}
			}
		}
		s.sendMessage("Done mapping rivers & oceans!");

		s.sendMessage("Done the async work! Time taken: " + ((int) (System.currentTimeMillis() - time)) / 1000.0
				+ " seconds");
		s.sendMessage("We are now waiting for the block placement to end...");
	}
}
