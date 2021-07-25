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
		slopeMap = new int[xw][zw][3];
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
		for(int y = this.getTopBlockY(x, z);y>0;y--) {
			m = this.getBlock(x, y, z);
			if (m.isBlock() && !m.isOccluding()
					&& !m.isFlammable())
				return y;
		}
		return 0;
	}

	private void computeHeightMap() {
		for (int x = 0; x < xw; x++) {
			for (int y = 0; y < zw; y++) {
				heightMap[x][y] = getGround(x, y);
			}
		}
	}
	
	private void computeSlopeMap() {
		
	}

	private void computeTreeMap() {
		
	}

	private void computeWaterMap() {
		
	}
}
