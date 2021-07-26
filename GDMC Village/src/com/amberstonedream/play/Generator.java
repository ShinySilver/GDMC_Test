package com.amberstonedream.play;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Generator extends BukkitRunnable {

	private World w;
	protected int x0, z0, xw, zw;
	private Boolean done = false;
	private ChunkSnapshot[][] work;
	protected CommandSender s;

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

	ConcurrentLinkedQueue<ScheduledBlock> fifo = new ConcurrentLinkedQueue<>();

	public Generator(World w, CommandSender s, int x0, int z0, int x1, int z1) {
		this.w = w;
		this.s = s;
		this.x0 = Math.min(x0, x1);
		this.z0 = Math.min(z0, z1);
		x1 = Math.max(x0, x1);
		z1 = Math.max(z0, z1);
		xw = x1 - x0;
		zw = z1 - z0;

		Bukkit.getScheduler().runTaskAsynchronously(Village.getInstance(), new Runnable() {
			@Override
			public void run() {
				s.sendMessage("Creating a copy of the working zone...");
				work = new ChunkSnapshot[xw / 16 + 1][zw / 16 + 1];
				for (int x = 0; x < xw / 16 + 1; x += 1) {
					for (int z = 0; z < zw / 16 + 1; z += 1) {
						work[x][z] = w.getChunkAt(x * 16 + x0, z * 16 + z0).getChunkSnapshot();
					}
				}

				runTaskTimer(Village.getInstance(), 1, 1);
				Bukkit.getScheduler().runTaskAsynchronously(Village.getInstance(), new Runnable() {
					@Override
					public void run() {
						generate();
					}
				});

			}
		});
	}

	/**
	 * Has to be implemented by the generator implementation. Has to call
	 * "this.done()" when the generation is complete.
	 */
	protected abstract void generate();

	@Override
	public void run() {
		ScheduledBlock b;
		int i = 0;
		synchronized (this.done) {
			while ((b = fifo.poll()) != null) {
				w.getBlockAt(b.x, b.y, b.z).setType(b.m, false);
				i++;
				if (i > 1000)
					return;
			}
			if (i != 0) {
				s.sendMessage("Placing " + i + " blocks/tick");
			}
			if (this.done) {
				s.sendMessage("Done!");
				this.cancel();
			}
		}
	}

	protected void done() {
		synchronized (this.done) {
			this.done = true;
		}
	}

	protected void setBlock(int x, int y, int z, Material m) {
		if (x >= 0 && z >= 0 && x < xw && z < zw) {
			fifo.add(new ScheduledBlock(x, y, z, m));
		}
	}

	protected Material getBlock(int x, int y, int z) {
		if (x >= 0 && z >= 0 && x < xw && z < zw) {
			return work[(x + x0 % 16) / 16][(z + (z0 % 16 + 16) % 16) / 16].getBlockType((((x + x0) % 16 + 16) % 16), y,
					(((z + z0) % 16 + 16) % 16));
		}
		return Material.AIR;
	}

	protected Material getTopBlock(int x, int z) {
		if (x >= 0 && z >= 0 && x < xw && z < zw) {
			ChunkSnapshot s = work[(x + (x0 % 16 + 16) % 16) / 16][(z + (z0 % 16 + 16) % 16) / 16];
			int y = s.getHighestBlockYAt((((x + x0) % 16 + 16) % 16), (((z + z0) % 16 + 16) % 16));
			return s.getBlockType((((x + x0) % 16 + 16) % 16), y, (((z + z0) % 16 + 16) % 16));
		}
		return Material.AIR;
	}

	protected int getTopBlockY(int x, int z) {
		if (x >= 0 && z >= 0 && x < xw && z < zw) {
			ChunkSnapshot s = work[(x + (x0 % 16 + 16) % 16) / 16][(z + (z0 % 16 + 16) % 16) / 16];
			return s.getHighestBlockYAt((((x + x0) % 16 + 16) % 16), (((z + z0) % 16 + 16) % 16));
		}
		return 0;
	}
}
