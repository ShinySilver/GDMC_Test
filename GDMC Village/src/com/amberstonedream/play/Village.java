package com.amberstonedream.play;

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
}
