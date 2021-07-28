package com.amberstonedream.play;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockChangeBuffer extends BukkitRunnable {

	private class ScheduledBlock {
		int x, y, z;
		Material m;

		ScheduledBlock(int x, int y, int z, Material m) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.m = m;
		}
	}

	private World w;
	private Boolean done = false;
	private CommandSender s;
	private ConcurrentLinkedQueue<ScheduledBlock> fifo;
	private int blockCount = 0, tickCount = 0;

	public BlockChangeBuffer(World w, CommandSender s) {
		this.w = w;
		this.s = s;
		fifo = new ConcurrentLinkedQueue<>();

		runTaskTimer(VillagePlugin.getInstance(), 1, 10);
	}

	@Override
	public void run() {
		ScheduledBlock b;
		tickCount += 1;
		int i = 0;
		synchronized (this.done) {
			while ((b = fifo.poll()) != null) {
				w.getBlockAt(b.x, b.y, b.z).setType(b.m, false);
				i++;
				if (i == 5000)
					break;
			}
			if (i != 0) {
				s.sendMessage("Placing " + i + " blocks/tick");
			}
			blockCount += i;
			if (i != 5000 && this.done) {
				s.sendMessage(
						"Done! Placed a total of of " + blockCount + "blocks over a duration of " + tickCount + " ticks.");
				this.cancel();
			}
		}
	}

	public void close() {
		synchronized (this.done) {
			this.done = true;
		}
	}

	public void setBlock(int x, int y, int z, Material m) {
		fifo.add(new ScheduledBlock(x, y, z, m));
	}
}
