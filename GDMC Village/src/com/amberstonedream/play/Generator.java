package com.amberstonedream.play;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;

import net.minecraft.server.v1_16_R3.BlockCryingObsidian;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public abstract class Generator {

	private static final List<Material> NATURAL_BLOCK = Arrays.asList(new Material[] { Material.STONE,
			Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL, Material.CLAY, Material.DIRT, Material.PODZOL,
			Material.COARSE_DIRT, Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.SANDSTONE,
			Material.PUMPKIN, Material.MELON, Material.COAL_ORE, Material.IRON_ORE });
	private World w;
	protected int x0, z0, xw, zw;

	private int[][] heightMap, biomeMap, treeMap, waterMap, slopeMap, terraformedMap, terraformedSlopeMap;
	private Material[][] structureMap;
	private BlockChangeBuffer b;
	private POI[][] cityMap;

	public Generator(World w, CommandSender s, int x0, int z0, int x1, int z1) {
		this.w = w;
		this.x0 = Math.min(x0, x1);
		this.z0 = Math.min(z0, z1);
		this.xw = Math.max(x0, x1) - x0;
		this.zw = Math.max(z0, z1) - z0;
		this.b = new BlockChangeBuffer(w, s);

		heightMap = new int[xw][zw];
		slopeMap = new int[xw][zw];
		biomeMap = new int[xw][zw];
		treeMap = new int[xw][zw];
		waterMap = new int[xw][zw];
		terraformedMap = new int[xw][zw];
		terraformedSlopeMap = new int[xw][zw];
		structureMap = new Material[xw][zw];
		cityMap = new POI[xw][zw];

		long time = System.currentTimeMillis();
		int targetTick = 0;
		VillagePlugin plugin = VillagePlugin.getInstance();

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating HeightMap...");
				computeHeightMap();
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating SlopeMap...");
				computeSlopeMap(slopeMap, heightMap);
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {

				s.sendMessage("Generating TreeMap...");
				computeTreeMap();
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating WaterMap...");
				computeWaterMap();
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating BiomeMap...");
				computeBiomeMap();
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating Ressource...");
				computeStructureMap();
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating TerraformedMap...");
				computeTerraformedMap();
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating TerraformedSlopeMap...");
				computeSlopeMap(terraformedSlopeMap, terraformedMap);
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Finished prefetching map. Time taken: "
						+ ((int) (System.currentTimeMillis() - time)) / 1000.0 + " seconds");
			}
		}, targetTick++);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {

						
						generateAsync(s, b, slopeMap, heightMap, treeMap, waterMap, biomeMap, terraformedMap,
								terraformedSlopeMap, structureMap);
						b.close();
					}
				});
			}
		}, targetTick++);
	}

	public abstract void generateAsync(CommandSender s, BlockChangeBuffer b, int[][] slopeMap, int[][] heightMap,
			int[][] treeMap, int[][] waterMap, int[][] biomeMap, int[][] terraformedMap, int[][] terraformedSlopeMap,
			Material[][] structureMap);

	private int isTree(Block b) {
		Material m = b.getType();
		if (m.name().endsWith("LEAVES") || m.name().endsWith("LOG") || m == Material.BROWN_MUSHROOM_BLOCK
				|| m == Material.RED_MUSHROOM_BLOCK || m == Material.MUSHROOM_STEM || m == Material.BAMBOO
				|| m == Material.CACTUS) {
			return b.getY();
		}
		return 0;
	}

	private int getGround(int x, int z) {
		Block b;
		Material m;
		for (int y = w.getHighestBlockYAt(x, z); y > 0; y--) {
			b = w.getBlockAt(x, y, z);
			m = b.getType();
			if (m.isBlock() && isTree(b) == 0 && m.isSolid() && !m.name().endsWith("GLASS"))
				return y;
		}
		return 0;
	}

	private int getCave(int x, int z) {
		Material m;
		for (int y = w.getHighestBlockYAt(x, z); y > 0;) {
			m = w.getBlockAt(x, y, z).getType();
			if (m == Material.AIR || m.isBurnable()) {
				y--;
			} else if (m == Material.CAVE_AIR) {
				return y;
			} else {
				int highest = 0;
				m = w.getBlockAt(x, ++y, z).getType();
				while (m != Material.AIR) {
					if (m == Material.CAVE_AIR) {
						highest = y;
					}
					m = w.getBlockAt(x, ++y, z).getType();
				}
				m = w.getBlockAt(x, --y, z).getType();
				if (m.isBurnable()) {
					return w.getBlockAt(x - 1, y, z).getType() == Material.CAVE_AIR
							|| w.getBlockAt(x + 1, y, z).getType() == Material.CAVE_AIR
							|| w.getBlockAt(x, y, z - 1).getType() == Material.CAVE_AIR
							|| w.getBlockAt(x, y, z + 1).getType() == Material.CAVE_AIR ? y : highest;
				}
				return highest;

			}
		}
		return 0;
	}

	private int isWater(int x, int z) {
		Material m;
		for (int y = w.getHighestBlockYAt(x, z); y > 0; y--) {
			m = w.getBlockAt(x, y, z).getType();
			if (m == Material.WATER)
				return y;
			if (m.isBlock() && m.isOccluding() && !m.isFlammable())
				return 0;
		}
		return 0;
	}

	private int maxHeightDiff(int x, int z, int[][] heightMap) {
		int max = 0;
		int min = 255;
		for (int i = x - 3; i < x + 4; i++) {
			for (int j = z - 3; j < z + 4; j++) {
				if (i < 0 || i >= heightMap.length || j < 0 || j >= heightMap[0].length)
					continue;
				int height = heightMap[i][j];
				if (height < min) {
					min = height;
				} else if (height > max) {
					max = height;
				}
			}
		}
		return max - min;
	}

	private void computeHeightMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				heightMap[x][z] = getGround(x0 + x, z0 + z);
			}
		}
	}

	/*
	 * private int[][] createKernel(int w, int h) { assert (w != 0 && h != 0);
	 * assert (w == 1 || h == 1); int[][] kernel = new int[w][h]; if (w == 1) { for
	 * (int i = 0; i < w; i++) { if (i == w / 2) { kernel[i][1] = 0; } else if (i <
	 * w / 2) { kernel[i][1] = -1; } else if (i > w / 2) { kernel[i][1] = 1; } }
	 * return kernel; } else if (h == 1) { for (int j = 0; j < h; j++) { if (j == h
	 * / 2) { kernel[1][j] = 0; } else if (j < h / 2) { kernel[1][j] = -1; } else if
	 * (j > h / 2) { kernel[1][j] = 1; } } return kernel; } return null; }
	 */
	/*
	 * int[][] tmp1, tmp2; tmp1 = Convolution.convolution2DPadded(heightMap, xw, zw,
	 * createKernel(KERNEL_SIZE, 1), KERNEL_SIZE, 1); tmp2 =
	 * Convolution.convolution2DPadded(heightMap, xw, zw, createKernel(1,
	 * KERNEL_SIZE), 1, KERNEL_SIZE); for (int x = 0; x < xw; x++) { for (int z = 0;
	 * z < zw; z++) { slopeMap[x][z][0] = tmp1[x][z]; slopeMap[x][z][1] =
	 * tmp2[x][z]; } }
	 */

	private void computeSlopeMap(int[][] slopeMap, int[][] heightMap) {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				slopeMap[x][z] = maxHeightDiff(x, z, heightMap);
			}
		}

	}

	private void computeTreeMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				Block b = w.getHighestBlockAt(x0 + x, z0 + z);
				treeMap[x][z] = isTree(b);
			}
		}
	}

	private void computeWaterMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				waterMap[x][z] = isWater(x0 + x, z0 + z);
			}
		}
	}

	private void computeStructureMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				Material m = w.getBlockAt(x0 + x, heightMap[x][z], z0 + z).getType();
				if (NATURAL_BLOCK.contains(m)) {
					structureMap[x][z] = null;
				} else {
					structureMap[x][z] = m;
				}
			}
		}
	}

	private void populateCityMapWithVanillaStructures() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (structureMap[x][z] == null) {
					cityMap[x][z] = null;
				} else {
					switch (structureMap[x][z]) {
					//TODO Structures left : Villager house, desert_well, JUNGLE_PYRAMID, WOODlAND_MANSION
					case BLUE_TERRACOTTA:
						// check side for good pattern then convert radius around to desert_pyramide
						Block b = w.getBlockAt(x, heightMap[x][z], z);
						if (b.getRelative(BlockFace.NORTH_EAST) == b.getRelative(BlockFace.NORTH_WEST)
								&& b.getRelative(BlockFace.NORTH_EAST) == b.getRelative(BlockFace.SOUTH_EAST)
								&& b.getRelative(BlockFace.NORTH_EAST) == b.getRelative(BlockFace.SOUTH_WEST)
								&& b.getRelative(BlockFace.NORTH_EAST).getType() == Material.ORANGE_TERRACOTTA) {
							for (int i = -10; i < 10; i++) {
								for (int j = -10; j < 10; j++) {
									if (x + i < 0 || x + i >= heightMap.length || z + j < 0
											|| z + j >= heightMap[0].length)
										continue;
									cityMap[x + i][z + j] = POI.DESERT_PYRAMID;
								}
							}
						}
						break;
					case SNOW_BLOCK:
						cityMap[x][z] = POI.IGLOO;
						break;
					case DARK_OAK_PLANKS:
						// Several subcase here : woodland mansion or pillager outpost (we can diff with
						// the fence_post)
						break;
					case DARK_OAK_FENCE:
						for (int i = -25; i < 25; i++) {
							for (int j = -25; j < 25; j++) {
								if (x + i < 0 || x + i >= heightMap.length || z + j < 0 || z + j >= heightMap[0].length)
									continue;
								cityMap[x + i][z + j] = POI.PILLAGER_OUTPOST;
							}
						}
					case SPRUCE_PLANKS:
					case SPRUCE_STAIRS:
						cityMap[x][z] = POI.SWAMP_HUT;
						break;
					case OBSIDIAN:
					case CRYING_OBSIDIAN:
					case NETHERRACK:
						for (int i = -1; i < 1; i++) {
							for (int j = -1; j < 1; j++) {
								if (x + i < 0 || x + i >= heightMap.length || z + j < 0 || z + j >= heightMap[0].length)
									continue;
								cityMap[x][z] = POI.RUINED_PORTAL;
							}
						}
						break;
					case GRASS_PATH:
					case SMOOTH_SANDSTONE:
						cityMap[x][z] = POI.PATH;
						break;

					default:
						cityMap[x][z] = POI.UNKOWN;
					}
				}
			}
		}
	}

	private void computeTerraformedMap() {
		// Base
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				terraformedMap[x][z] = getCave(x0 + x, z0 + z);
			}
		}

		// Erode cave layer
		int dx, dz, criterion, current, nearbyHeight;
		int[][] clone = new int[terraformedMap.length][terraformedMap[0].length];
		for (int i = 0; i < 10; i++) {
			for (int x = 1; x < xw - 1; x++) {
				for (int z = 1; z < zw - 1; z++) {
					nearbyHeight = 0;
					current = terraformedMap[x][z];
					if (current != 0) {
						criterion = 0;
						for (dx = -1; dx < 2; dx++) {
							for (dz = -1; dz < 2; dz++) {
								nearbyHeight = Math.max(nearbyHeight, heightMap[x + dx][z + dz]);
								if (current > Math.max(terraformedMap[x + dx][z + dz], heightMap[x + dx][z + dz])) {
									criterion++;
								}
							}
						}
						if (criterion >= 3) {
							clone[x][z] = Math.max(current - 1, nearbyHeight - 1);
						} else {
							clone[x][z] = current;
						}
					} else {
						clone[x][z] = 0;
					}
				}
			}
			int[][] tmp = terraformedMap;
			terraformedMap = clone;
			clone = tmp;
		}
		// Smoothen / expand cave layers
		boolean admissible;
		for (int i = 0; i < 3; i++) {
			for (int x = 1; x < xw - 1; x++) {
				for (int z = 1; z < zw - 1; z++) {
					current = terraformedMap[x][z];
					criterion = 0;
					nearbyHeight = 0;
					admissible = false;
					for (dx = -1; dx < 2; dx++) {
						for (dz = -1; dz < 2; dz++) {
							if (terraformedMap[x + dx][z + dz] != 0)
								admissible = true;
							if (current < Math.max(terraformedMap[x + dx][z + dz], heightMap[x + dx][z + dz])) {
								criterion++;
								nearbyHeight = Math.max(nearbyHeight,
										Math.max(terraformedMap[x + dx][z + dz], heightMap[x + dx][z + dz]));
							}
						}
					}
					if (admissible && criterion >= 2) {
						clone[x][z] = nearbyHeight;
					} else {
						clone[x][z] = current;
					}

				}
			}
			int[][] tmp = terraformedMap;
			terraformedMap = clone;
			clone = tmp;
		}

		// Merge cave layer and heightmap
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				terraformedMap[x][z] = Math.max(terraformedMap[x][z], heightMap[x][z]);
			}
		}
	}

	private void computeBiomeMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				biomeMap[x][z] = w.getBlockAt(x0 + x, 60, z0 + z).getBiome().ordinal();
			}
		}
	}
	
	public BlockChangeBuffer getBuffer() {
		return b;
	}
}
