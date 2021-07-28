package com.amberstonedream.play;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class VillageGenerator extends Generator {

	public VillageGenerator(World w, CommandSender s, int x0, int z0, int x1, int z1) {
		super(w, s, x0, z0, x1, z1);
	}

	@Override
	public void generateAsync(CommandSender s, BlockChangeBuffer b, int[][][] slopeMap, int[][] heightMap,
			boolean[][] treeMap, boolean[][] waterMap) {

		s.sendMessage("Starting the async work! Generating Terrain...");
		Material m;
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {
				if (treeMap[x][z]) {
					m = Material.LIME_STAINED_GLASS;
				} else if (waterMap[x][z]) {
					m = Material.LIGHT_BLUE_STAINED_GLASS;
				} else {
					m = Material.WHITE_STAINED_GLASS;
				}
				b.setBlock(x0 + x, heightMap[x][z]+50, z0 + z, m);
			}
		}
	}

}
