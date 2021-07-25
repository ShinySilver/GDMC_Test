package com.amberstonedream.play;

import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.amberstonedream.play.commands.VillageCommand;

public class Village extends JavaPlugin implements Listener {
	private static Village instance;

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

	public static Village getInstance() {
		return instance;
	}

	public static void startGeneration(World world, int i, int j, int k, int l) {
		// TODO Auto-generated method stub
		
	}
}
