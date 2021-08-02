package silver.screw.gdmc.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import silver.screw.gdmc.BlockChangeBuffer;
import silver.screw.gdmc.VillageGenerator;

public class VillageCommand implements CommandExecutor, TabCompleter {
	private HashMap<String, BlockChangeBuffer> map;
	
	public VillageCommand() {
		map = new HashMap<String, BlockChangeBuffer>();
	}

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
			map.put(sender.getName(), new VillageGenerator(p.getWorld(), sender, p.getLocation().getBlockX() - x_width / 2,
					p.getLocation().getBlockZ() - z_width / 2, p.getLocation().getBlockX() + x_width / 2,
					p.getLocation().getBlockZ() + z_width / 2).getBuffer());
			return true;
		} else if (args.length == 1 && args[0].equals("undo")) {
			if(map.containsKey(sender.getName())) {
				if(!map.get(sender.getName()).restore()) {
					sender.sendMessage("Can't undo, either an operation is still ongoing or you have already undone your last operation.");
				}else {
					sender.sendMessage("Restorating...");
				}
			}else {
				sender.sendMessage("No operation saved under the name \""+sender.getName()+"\". Impossible to undo!");
			}
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if (args.length == 1) {
			if ("undo".startsWith(args[0])) {
				list.add("undo");
			}
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (p.getName().startsWith(args[0])) {
					list.add(p.getName());
				}
			}
		} else if (args.length == 2 && !args[0].equals("undo")) {
			list.add("512");
		} else if (args.length == 3 && !args[0].equals("undo")) {
			list.add("512");
		}
		return list;
	}
}
