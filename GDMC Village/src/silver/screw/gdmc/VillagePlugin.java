package silver.screw.gdmc;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import silver.screw.gdmc.commands.VillageCommand;

/** 
 * TODO: Add mountain filling
 * TODO: Add river enlarging
 * TODO: Add river terraforming
 * TODO: Add road generation from N/S/E/W to riverside to mountains
 * TODO: Stop using placeholder +100Y terrain & add a /vgen cancel to restore the map
 */
public class VillagePlugin extends JavaPlugin implements Listener {
	private static VillagePlugin instance;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable() {
		instance = this;

		// Registering command
		PluginCommand command = this.getCommand("village-gen");
		VillageCommand e = new VillageCommand();
		command.setExecutor(e);
		command.setTabCompleter(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisable() {
		instance = null;
	}

	public static VillagePlugin getInstance() {
		return instance;
	}
}
