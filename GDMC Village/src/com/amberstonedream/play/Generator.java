package com.amberstonedream.play;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public abstract class Generator {

	public static final int KERNEL_SIZE = 5;
	private World w;
	protected int x0, z0, xw, zw;

	private int[][] heightMap, biomeMap, treeMap, waterMap, slopeMap;

	public Generator(World w, CommandSender s, int x0, int z0, int x1, int z1) {
		this.w = w;
		this.x0 = Math.min(x0, x1);
		this.z0 = Math.min(z0, z1);
		this.xw = Math.max(x0, x1) - x0;
		this.zw = Math.max(z0, z1) - z0;

		heightMap = new int[xw][zw];
		slopeMap = new int[xw][zw];
		biomeMap = new int[xw][zw];
		treeMap = new int[xw][zw];
		waterMap = new int[xw][zw];
		
		long time = System.currentTimeMillis();

		VillagePlugin plugin = VillagePlugin.getInstance();
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating HeightMap...");
				computeHeightMap();
			}
		}, 1);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating SlopeMap...");
				computeSlopeMap();
			}
		}, 2);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {

				s.sendMessage("Generating TreeMap...");
				computeTreeMap();
			}
		}, 3);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating WaterMap...");
				computeWaterMap();
			}
		}, 4);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Generating BiomeMap...");
				computeBiomeMap();
			}
		}, 5);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Finished prefetching map. Time taken: "+ ((int)(System.currentTimeMillis()-time))/1000.0 + " seconds");
			}
		}, 6);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {

						BlockChangeBuffer b = new BlockChangeBuffer(w, s);
						generateAsync(s, b, slopeMap, heightMap, treeMap, waterMap);
						b.close();
					}
				});
			}
		}, 7);
	}

	public abstract void generateAsync(CommandSender s, BlockChangeBuffer b, int[][] slopeMap, int[][] heightMap,
			int[][] treeMap2, int[][] waterMap2);

	private int getGround(int x, int z) {
		Material m;
		for (int y = w.getHighestBlockYAt(x, z); y > 0; y--) {
			m = w.getBlockAt(x, y, z).getType();
			if (m.isBlock() && m.isOccluding() && !m.isFlammable())
				return y;
		}
		return 0;
	}

	private int isTree(int x, int z) {
		Block b = w.getHighestBlockAt(x, z);
		if (b.getType().name().endsWith("LEAVES")) {
			return b.getY();
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

	private void computeSlopeMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				slopeMap[x][z] = maxHeightDiff(x, z);
			}
		}

	}

	private int maxHeightDiff(int x, int z) {
		int max = 0;
		int min = 255;
		for (int i = x - 3; i < x + 4; i++) {
			for (int j = z - 3; j < z + 4; j++) {
				try {
					int height = heightMap[i][j];
					if (height < min) {
						min = height;
					} else if (height > max) {
						max = height;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					continue;
				}
			}
		}
		return max - min;
	}

	private void computeTreeMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				treeMap[x][z] = isTree(x0 + x, z0 + z);
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

	private void computeBiomeMap() {
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				biomeMap[x][z] = w.getBlockAt(x0 + x, 60, z0 + z).getBiome().ordinal();
			}
		}
	}
}
