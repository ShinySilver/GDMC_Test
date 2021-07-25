package com.amberstonedream.play;

import org.bukkit.World;

public class VillageGenerator extends Generator{

	private int[][][] slopeMap;
	private int[][] heightMap;
	private boolean[][] treeMap, waterMap;
	
	public VillageGenerator(World w, int x0, int z0, int x1, int z1) {
		super(w, x0, z0, x1, z1);

		heightMap = new int[xw][zw];
		slopeMap = new int[xw][zw][3];
		treeMap = new boolean[xw][zw];
		waterMap = new boolean[xw][zw];
	}

	@Override
	protected void generate() {
		
	}

}
