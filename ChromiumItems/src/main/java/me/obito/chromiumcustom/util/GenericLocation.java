package me.obito.chromiumcustom.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;

public class GenericLocation implements Serializable {
	
	private int x;
	private int y;
	private int z;
	private String worldName;
	
	public GenericLocation(int x, int y, int z, String worldName) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.worldName = worldName;
	}
	
	public GenericLocation(Location l) {
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
		this.worldName = l.getWorld().getName();
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldName), x, y, z);
	}
}
