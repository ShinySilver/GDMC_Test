package com.amberstonedream.play;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public abstract class Generator {

	public static final int KERNEL_SIZE = 5;
	private World w;
	protected int x0, z0, xw, zw;

	private int[][][] slopeMap;
	private int[][] heightMap;
	private boolean[][] treeMap, waterMap;

	public Generator(World w, CommandSender s, int x0, int z0, int x1, int z1) {
		this.w = w;
		this.x0 = Math.min(x0, x1);
		this.z0 = Math.min(z0, z1);
		this.xw = Math.max(x0, x1) - x0;
		this.zw = Math.max(z0, z1) - z0;

		heightMap = new int[xw][zw];
		slopeMap = new int[2][xw][zw];
		treeMap = new boolean[xw][zw];
		waterMap = new boolean[xw][zw];

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
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {

						BlockChangeBuffer b = new BlockChangeBuffer(w, s);
						generateAsync(s, b, slopeMap, heightMap, treeMap, waterMap);
						b.close();
					}
				});
			}
		}, 5);
	}

	public abstract void generateAsync(CommandSender s, BlockChangeBuffer b, int[][][] slopeMap, int[][] heightMap, boolean[][] treeMap,
			boolean[][] waterMap);

	private int getGround(int x, int z) {
		Material m;
		for (int y = w.getHighestBlockYAt(x, z); y > 0; y--) {
			m = w.getBlockAt(x, y, z).getType();
			if (m.isBlock() && m.isOccluding() && !m.isFlammable())
				return y;
		}
		return 0;
	}

	private boolean isTree(int x, int z) {
		return w.getHighestBlockAt(x, z).getType().name().endsWith("LEAVES");
	}

	private boolean isWater(int x, int z) {
		Material m;
		for (int y = w.getHighestBlockYAt(x, z); y > 0; y--) {
			m = w.getBlockAt(x, y, z).getType();
			if (m == Material.WATER)
				return true;
			if (m.isBlock() && m.isOccluding() && !m.isFlammable())
				return false;
		}
		return false;
	}

	private void computeHeightMap() {
		for (int x = 0; x < xw; x++) {
			for (int y = 0; y < zw; y++) {
				heightMap[x][y] = getGround(x, y);
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

	private void computeSlopeMap() {
		/*
		 * int[][] tmp1, tmp2; tmp1 = Convolution.convolution2DPadded(heightMap, xw, zw,
		 * createKernel(KERNEL_SIZE, 1), KERNEL_SIZE, 1); tmp2 =
		 * Convolution.convolution2DPadded(heightMap, xw, zw, createKernel(1,
		 * KERNEL_SIZE), 1, KERNEL_SIZE); for (int x = 0; x < xw; x++) { for (int z = 0;
		 * z < zw; z++) { slopeMap[x][z][0] = tmp1[x][z]; slopeMap[x][z][1] =
		 * tmp2[x][z]; } }
		 */
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
}
