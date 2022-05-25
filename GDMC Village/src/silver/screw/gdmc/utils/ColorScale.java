package silver.screw.gdmc.utils;

import org.bukkit.Material;

public class ColorScale {
	private static final Material[] scale = new Material[] { Material.BLACK_CONCRETE, Material.OBSIDIAN,
			Material.BLACK_WOOL, Material.BLACKSTONE, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,
			Material.POLISHED_BLACKSTONE_BRICKS, Material.CHISELED_POLISHED_BLACKSTONE, Material.POLISHED_BLACKSTONE,
			Material.GRAY_CONCRETE, Material.GRAY_WOOL, Material.NETHERITE_BLOCK, Material.BLAST_FURNACE,
			Material.BASALT, Material.CYAN_TERRACOTTA, Material.BEDROCK, Material.GRAY_GLAZED_TERRACOTTA,
			Material.ACACIA_WOOD, Material.SMOKER, Material.FURNACE, Material.COBBLESTONE,
			Material.CRACKED_STONE_BRICKS, Material.CHISELED_STONE_BRICKS, Material.STONE_BRICKS, Material.STONE,
			Material.POLISHED_ANDESITE, Material.ANDESITE, Material.DEAD_BRAIN_CORAL_BLOCK,
			Material.DEAD_TUBE_CORAL_BLOCK, Material.DEAD_BUBBLE_CORAL_BLOCK, Material.DEAD_FIRE_CORAL_BLOCK,
			Material.DEAD_HORN_CORAL_BLOCK, Material.LIGHT_GRAY_WOOL, Material.LODESTONE, Material.SMOOTH_STONE,
			Material.CLAY, Material.DIORITE, Material.POLISHED_DIORITE, Material.WHITE_CONCRETE, Material.IRON_BLOCK,
			Material.BIRCH_WOOD, Material.CHISELED_QUARTZ_BLOCK, Material.QUARTZ_BRICKS, Material.QUARTZ_BLOCK,
			Material.SMOOTH_QUARTZ, Material.SNOW_BLOCK };
	private float max_value, min_value;
	private boolean inv;

	public ColorScale(float max, float min, boolean inverse) {
		max_value = max;
		min_value = min;
		inv = inverse;
	}

	public Material getMaterialForValue(float value) {
		int len = scale.length;
		if (value >= max_value) {
			if (inv) {
				return scale[0];
			} else {
				return scale[len - 1];
			}
		} else if (value <= min_value) {
			if (inv) {
				return scale[len - 1];
			} else {
				return scale[0];
			}
		} else {
			if (inv) {
				return scale[(int) ((1 - (value - min_value) / (max_value - min_value)) * (len - 1))];
			} else {
				return scale[(int) (((value - min_value) / (max_value - min_value)) * (len - 1))];
			}
		}
	}
}
