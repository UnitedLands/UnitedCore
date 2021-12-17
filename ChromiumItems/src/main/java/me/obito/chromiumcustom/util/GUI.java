package me.obito.chromiumcustom.util;

import me.obito.chromiumcustom.listeners.GUIListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class GUI extends GUIListener {
	
	private static HashMap<String, GUI> guiMap = new HashMap<String, GUI>();
	private Inventory inventory;
	
	public GUI(int slots, String name, String id, ArrayList<ItemStack> items) {
		inventory = Bukkit.createInventory(null, slots, ChatColor.translateAlternateColorCodes('&',name));
		setInventory(items);
		guiMap.put(id, this);
	}
	
	public GUI(int slots, String name, String id) {
		this(slots, name, id, new ArrayList<ItemStack>());
	}

	public Inventory getInventory() {
		return inventory;
	}
	
	public void setInventory(ArrayList<ItemStack> items) {
		items.forEach( (i) -> {
			int slot = inventory.firstEmpty();
			if(slot != -1) {
				inventory.setItem(slot, i);
			}
		});
	}
	
	public void open(Player p) {
		p.openInventory(inventory);
	}
	
	public static GUI getGUI(String id) {
		return guiMap.get(id);
	}
	
	public static GUI getByInventory(Inventory inventory) {
		for (Entry<String, GUI> e : guiMap.entrySet()) {
			if(e.getValue().getInventory().equals(inventory)) {
				return e.getValue();
			}
		}
		return null;
	}
}
