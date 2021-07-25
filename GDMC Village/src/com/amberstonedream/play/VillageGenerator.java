package com.amberstonedream.play;

import org.bukkit.World;

import org.bukkit.Material;

public class VillageGenerator extends Generator {

	private static final int KERNEL_SIZE = 5;

	private int[][][] slopeMap;
	private int[][] heightMap;
	private boolean[][] treeMap, waterMap;

	public VillageGenerator(World w, int x0, int z0, int x1, int z1) {
		super(w, x0, z0, x1, z1);

		heightMap = new int[xw][zw];
		slopeMap = new int[2][xw][zw];
		treeMap = new boolean[xw][zw];
		waterMap = new boolean[xw][zw];

		computeHeightMap();
		computeSlopeMap();
		computeTreeMap();
		computeWaterMap();
	}

	@Override
	protected void generate() {

	}

	private int getGround(int x, int z) {
		Material m;
		for (int y = this.getTopBlockY(x, z); y > 0; y--) {
			m = this.getBlock(x, y, z);
			if (m.isBlock() && m.isOccluding() && !m.isFlammable())
				return y;
		}
		return 0;
	}

	private boolean isTree(int x, int z) {
		return this.getTopBlock(x, z).name().endsWith("LEAVES");
	}

	private boolean isWater(int x, int z) {
		Material m;
		for (int y = this.getTopBlockY(x, z); y > 0; y--) {
			m = this.getBlock(x, y, z);
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

	private int[][] createKernel(int w, int h) {
		assert (w != 0 && h != 0);
		assert (w == 1 || h == 1);
		int[][] kernel = new int[w][h];
		if (w == 1) {
			for (int i = 0; i < w; i++) {
				if (i == w / 2) {
					kernel[i][1] = 0;
				} else if (i < w / 2) {
					kernel[i][1] = -1;
				} else if (i > w / 2) {
					kernel[i][1] = 1;
				}
			}
			return kernel;
		} else if (h == 1) {
			for (int j = 0; j < h; j++) {
				if (j == h / 2) {
					kernel[1][j] = 0;
				} else if (j < h / 2) {
					kernel[1][j] = -1;
				} else if (j > h / 2) {
					kernel[1][j] = 1;
				}
			}
			return kernel;
		}
		return null;
	}

	private void computeSlopeMap() {
		int[][] tmp1, tmp2;
		tmp1 = Convolution.convolution2DPadded(heightMap, xw, zw, createKernel(KERNEL_SIZE, 1), KERNEL_SIZE, 1);
		tmp2 = Convolution.convolution2DPadded(heightMap, xw, zw, createKernel(1, KERNEL_SIZE), 1, KERNEL_SIZE);
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				slopeMap[x][z][0] = tmp1[x][z];
				slopeMap[x][z][1] = tmp2[x][z];
			}
		}
	}

	private void computeTreeMap() {
		for (int x = 0; x < xw; x++) {
			for (int y = 0; y < zw; y++) {
				treeMap[x][y] = isTree(x, y);
			}
		}
	}

	private void computeWaterMap() {
		for (int x = 0; x < xw; x++) {
			for (int y = 0; y < zw; y++) {
				waterMap[x][y] = isWater(x, y);
			}
		}
	}
}
