package com.amberstonedream.play;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
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
	private int blockCount = 0;
	private long startTime;
	private boolean shouldRestoreAutosave;

	private static final int BATCH_SIZE = 500;

	public BlockChangeBuffer(World w, CommandSender s) {
		this.w = w;
		this.s = s;
		shouldRestoreAutosave = w.isAutoSave();
		w.setAutoSave(false);
		startTime = System.currentTimeMillis();
		fifo = new ConcurrentLinkedQueue<>();

		runTaskTimer(VillagePlugin.getInstance(), 1, 0);
	}

	@Override
	public void run() {
		ScheduledBlock b;
		int i = 0;
		synchronized (this.done) {
			while ((b = fifo.poll()) != null) {
				w.getBlockAt(b.x, b.y, b.z).setType(b.m, false);
				i++;
				if (i == BATCH_SIZE)
					break;
			}
			/*
			 * if (i != 0) { s.sendMessage("Placing " + i + " blocks/tick"); }
			 */
			blockCount += i;
			if (i != BATCH_SIZE && this.done) {
				s.sendMessage("Done! Placed a total of of " + blockCount + "blocks over a duration of "
						+ ((int) (System.currentTimeMillis() - startTime)) / 1000.0 + " seconds");
				this.cancel();
				if (shouldRestoreAutosave) {
					Bukkit.getScheduler().runTaskLater(VillagePlugin.getInstance(), new Runnable() {
						@Override
						public void run() {
							w.setAutoSave(true);
						}
					}, 60);
				}
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
