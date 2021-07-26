package com.amberstonedream.play.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.amberstonedream.play.VillageGenerator;

public class VillageCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 3) {
			Player p = Bukkit.getPlayer(args[0]);
			if (p == null) {
				sender.sendMessage("\"" + args[0] + "\" is not an online player!");
				return true;
			}
			int x_width, z_width;
			try {
				x_width = Integer.parseInt(args[1]);
				z_width = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Either \"" + args[1] + "\" or \"" + args[2] + "\" is not a valid integer.");
				return true;
			}
			new VillageGenerator(p.getWorld(), sender, p.getLocation().getBlockX() - x_width / 2,
					p.getLocation().getBlockZ() - z_width / 2, p.getLocation().getBlockX() + x_width / 2,
					p.getLocation().getBlockZ() + z_width / 2);
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if (args.length == 1) {
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (p.getName().startsWith(args[0])) {
					list.add(p.getName());
				}
			}
		} else if (args.length == 2) {
			list.add("512");
		} else if (args.length == 3) {
			list.add("512");
		}
		return list;
	}
}
