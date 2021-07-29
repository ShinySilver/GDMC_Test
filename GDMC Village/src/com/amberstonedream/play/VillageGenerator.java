package com.amberstonedream.play;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class VillageGenerator extends Generator {

	public VillageGenerator(World w, CommandSender s, int x0, int z0, int x1, int z1) {
		super(w, s, x0, z0, x1, z1);
	}

	@Override
	public void generateAsync(CommandSender s, BlockChangeBuffer b, int[][] slopeMap, int[][] heightMap,
			int[][] treeMap, int[][] waterMap, int[][] biomeMap, int[][] terraformedMap, int[][] terraformedSlopeMap) {

		s.sendMessage("Starting the async work! Generating Terrain...");

		// Print layers
		for (int x = 0; x < xw; x++) {
			for (int z = 0; z < zw; z++) {

				if (waterMap[x][z] != 0) {
					b.setBlock(x0 + x, waterMap[x][z] + 50, z0 + z, Material.BLUE_STAINED_GLASS);
					b.setBlock(x0 + x, waterMap[x][z] + 100, z0 + z, Material.BLUE_STAINED_GLASS);
				}

				if (treeMap[x][z] != 0) {
					b.setBlock(x0 + x, treeMap[x][z] + 50, z0 + z, Material.LIME_TERRACOTTA);
					b.setBlock(x0 + x, treeMap[x][z] + 49, z0 + z, Material.LIME_TERRACOTTA);
					b.setBlock(x0 + x, treeMap[x][z] + 100, z0 + z, Material.LIME_TERRACOTTA);
					b.setBlock(x0 + x, treeMap[x][z] + 99, z0 + z, Material.LIME_TERRACOTTA);
				}

				b.setBlock(x0 + x, heightMap[x][z] + 50, z0 + z,
						slopeMap[x][z] < 3 ? Material.WHITE_TERRACOTTA
								: (slopeMap[x][z] < 8 ? Material.YELLOW_TERRACOTTA : Material.RED_TERRACOTTA));
				b.setBlock(x0 + x, heightMap[x][z] + 49, z0 + z,
						slopeMap[x][z] < 3 ? Material.WHITE_TERRACOTTA
								: (slopeMap[x][z] < 8 ? Material.YELLOW_TERRACOTTA : Material.RED_TERRACOTTA));

				b.setBlock(x0 + x, terraformedMap[x][z] + 100, z0 + z,
						terraformedSlopeMap[x][z] < 3 ? Material.WHITE_TERRACOTTA
								: (terraformedSlopeMap[x][z] < 8 ? Material.YELLOW_TERRACOTTA : Material.RED_TERRACOTTA));
				b.setBlock(x0 + x, terraformedMap[x][z] + 99, z0 + z,
						terraformedSlopeMap[x][z] < 3 ? Material.WHITE_TERRACOTTA
								: (terraformedSlopeMap[x][z] < 8 ? Material.YELLOW_TERRACOTTA : Material.RED_TERRACOTTA));
			}
		}
	}
}
