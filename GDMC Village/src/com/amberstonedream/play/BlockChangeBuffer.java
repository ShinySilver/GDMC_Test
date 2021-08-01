package com.amberstonedream.play;

import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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

	private long time;
	private World w;
	private Boolean done = false;
	private BukkitTask restorationTask = null;
	private CommandSender s;
	private ConcurrentLinkedQueue<ScheduledBlock> fifo;
	private Stack<ScheduledBlock> lifo;
	private int blockCount = 0;
	private boolean shouldRestoreAutosave;

	private static final int BATCH_SIZE = 20000;

	public BlockChangeBuffer(World w, CommandSender s) {
		this.w = w;
		this.s = s;
		shouldRestoreAutosave = w.isAutoSave();
		w.setAutoSave(false);
		fifo = new ConcurrentLinkedQueue<>();
		lifo = new Stack<>();

		runTaskTimer(VillagePlugin.getInstance(), 1, 0);
	}

	@Override
	public void run() {
		ScheduledBlock b;
		Block bl;
		Material m;
		int i = 0;
		synchronized (this.done) {
			while ((b = fifo.poll()) != null) {
				bl = w.getBlockAt(b.x, b.y, b.z);
				m = bl.getType();
				bl.setType(b.m, false);
				b.m = m;
				lifo.add(b);
				i++;
				if (i == BATCH_SIZE)
					break;
			}
			blockCount += i;
			if (i != BATCH_SIZE && this.done) {
				s.sendMessage("This buffer finished placing blocks "
						+ ((int) (System.currentTimeMillis() - time)) / 1000.0
						+ " seconds after the generation thread. It placed a total of " + blockCount + " blocks.");
				this.cancel();
				if (shouldRestoreAutosave) {
					Bukkit.getScheduler().runTaskLater(VillagePlugin.getInstance(), new Runnable() {
						@Override
						public void run() {
							w.setAutoSave(true); // Rethink this
						}
					}, 20);
				}
			}
		}
	}

	public void close() {
		synchronized (this.done) {
			this.done = true;
			this.time = System.currentTimeMillis();
		}
	}

	public boolean restore() {
		synchronized (this.done) {
			if (!this.done || !fifo.isEmpty() || restorationTask != null) {
				return false;
			}
		}
		restorationTask = Bukkit.getScheduler().runTaskTimer(VillagePlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				ScheduledBlock b;
				for (int i = 0; i < BATCH_SIZE && !lifo.isEmpty(); i++) {
					b = lifo.pop();
					w.getBlockAt(b.x, b.y, b.z).setType(b.m, false);
				}
				if (lifo.isEmpty()) {
					s.sendMessage("Done undoing!");
					restorationTask.cancel();
				}
			}
		}, 1, 0);
		return true;
	}

	public void setBlock(int x, int y, int z, Material m) {
		fifo.add(new ScheduledBlock(x, y, z, m));
	}
}
