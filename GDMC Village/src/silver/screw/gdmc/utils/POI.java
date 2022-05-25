package silver.screw.gdmc.utils;

public enum POI {
	HOUSE(true), // plank, log, sandstone
	PATH(false), // path_block, smooth sandstone, gravel
	DESERT_WELL(false),
	// doto if desert, look for buried desert pyramid
	DESERT_PYRAMID(true), // blue terracotta with high slope (probably 9)
	IGLOO(true), // snow block (with maybe carpet a few blocks under it)
	JUNGLE_PYRAMID(false), // mossy cobble + high slope sometimes
	PILLAGER_OUTPOST(true), // dark oak plank and log
	SWAMP_HUT(true), // spruce plank and stairs
	WOODLAND_MANSION(true), // dark oak plank with slope 0
	SHIPWREK(false), // IDKM, too hard (a buch of random wood of random size)
	RUINED_PORTAL(false), // obsidian and netherrack
	UNKOWN(true); // everything else
//	private boolean is_path_and_construct_open();

	private final boolean toAvoid;

	private POI(final boolean toAvoid) {
		this.toAvoid = toAvoid;
	}

	public boolean toAvoid() {
		return this.toAvoid;
	}
}
