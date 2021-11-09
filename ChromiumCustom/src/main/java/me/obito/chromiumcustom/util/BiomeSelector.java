package me.obito.chromiumcustom.util;

import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BiomeSelector extends GUI {
	
	static {
		new BiomeSelector();
	}
	
	private static String id = "BIOME_SELECTOR";
	
	public BiomeSelector() {
		super(9, "&6&lBiome Selector", id);
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		
		items.add(createButton(Material.GRASS_BLOCK, "&6Plains", Biome.PLAINS));
		items.add(createButton(Material.SAND, "&6Desert", Biome.DESERT));
		items.add(createButton(Material.LILY_PAD, "&6Swamp", Biome.SWAMP));
		items.add(createButton(Material.SNOW_BLOCK, "&6Snowy Taiga", Biome.SNOWY_TAIGA));
		items.add(createButton(Material.ACACIA_LOG, "&6Savanna", Biome.SAVANNA));
		items.add(createButton(Material.VINE, "&6Jungle", Biome.JUNGLE));
		items.add(createButton(Material.OAK_LOG, "&6Forest", Biome.FOREST));
		super.setInventory(items);
		
		super.setOnClickConsumer( (e) -> {
			ItemStack inHand = e.getWhoClicked().getItemInHand();
			ItemStack clicked = e.getCurrentItem();
			Player p = (Player) e.getWhoClicked();
			int slot = -1;
			for(int i = 0; i < p.getInventory().getSize(); i++) {
				if(inHand.equals(p.getInventory().getItem(i))) {
					slot = i;
					break;
				}
			}
			if(clicked == null) {
				e.setCancelled(true);
				return;
			}
			if(clicked.getType().equals(Material.AIR)) {
				e.setCancelled(true);
				return;
			}
			ToolCustomItem item = ToolCustomItem.getItem(inHand);
			if(item != null) {
				NBTItem nbtClicked = new NBTItem(clicked);
				NBTItem nbtInHand = new NBTItem(inHand);
				if(nbtClicked.hasKey("BIOME")) {
					nbtInHand.setString("BIOME", nbtClicked.getString("BIOME"));
					p.getInventory().setItem(slot, nbtInHand.getItem());
					e.getWhoClicked().closeInventory();
				}
				
			} else {
				e.getWhoClicked().closeInventory();
			}
			e.setCancelled(true);
		});
	}
	
	public static String getId() {
		return id;
	}
	
	private ItemStack createButton(Material display, String name, Biome type) {
		ItemStack baseItem = new ItemStack(display);
		ItemMeta meta = baseItem.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		baseItem.setItemMeta(meta);
		NBTItem nbt = new NBTItem(baseItem);
		nbt.setString("BIOME", type.toString());
		return nbt.getItem();
	}
}
