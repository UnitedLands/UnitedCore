package org.unitedlands.items.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;

public enum TreeType implements Serializable {
	MANGO(Material.JUNGLE_SAPLING,
			Material.JUNGLE_LOG,
			"mango_tree_log",
			Material.JUNGLE_LEAVES,
			"mango_tree_leaves",
			CustomItem.getItemByName("&fMango Sapling")),
	MIDAS_OAK(Material.OAK_SAPLING,
			Material.OAK_LOG,
			"palm_stripped_log",
			Material.OAK_LEAVES,
			"peach_tree_leaves",
			CustomItem.getItemByName("&eMidas' Oak Seeds")),
	MIDAS_JUNGLE(Material.JUNGLE_SAPLING,
			Material.JUNGLE_LOG,
			"palm_stripped_log",
			Material.JUNGLE_LEAVES,
			"peach_tree_leaves",
			CustomItem.getItemByName("&eMidas' Jungle Seeds")),
	ANCIENT_OAK(Material.OAK_SAPLING,
			Material.OAK_LOG,
			"palm_stripped_log",
			Material.OAK_LEAVES,
			"peach_tree_leaves",
			CustomItem.getItemByName("&6Ancient Oak Seeds")),
	FUNGAL_BIRCH(Material.BIRCH_SAPLING,
			Material.BIRCH_LOG,
			"decaying_tree_log",
			Material.BIRCH_LEAVES,
			"decaying_tree_leaves",
			CustomItem.getItemByName("&fBracket Mushroom")),
	PINE(Material.SPRUCE_SAPLING,
			Material.SPRUCE_LOG,
			"maple_tree_log",
			Material.SPRUCE_LEAVES,
			"maple_tree_leaves",
			CustomItem.getItemByName("&fPine Sapling")),
	FLOWERING_ACACIA(Material.ACACIA_SAPLING,
			Material.ACACIA_LOG,
			"lime_tree_log",
			Material.ACACIA_LEAVES,
			"lime_tree_leaves",
			CustomItem.getItemByName("&fFlowering Acacia Sapling"));

	private static final HashMap<String, TreeType> validSeed = new HashMap<>();
	
	static {
		for(TreeType t : TreeType.values()) {
			if(!validSeed.containsKey(CustomItem.getKey(t.getSeed()))) {
				Logger.log(String.format("&aGenerated new tree &6[&e%s&6] &awith sapling &6[&e%s&6]", t.name(), CustomItem.getKey(t.getSeed())));
				validSeed.put(CustomItem.getKey(t.getSeed()), t);
			} else {
				Logger.log("&c&lDUPLICATE SAPLING FOUND! "+CustomItem.getKey(t.getSeed()));
			}
		}
		
	}
	
	private final Material vanillaSapling;
	private final Material stemBlock;
	private final String stemReplaceBlockName;
	private final Material fruitBlock;
	private final String fruitReplaceBlockName;
	private final ItemStack fruitSeed;

	TreeType(Material vanillaSapling, Material stemBlock, String stemReplaceBlockName, Material fruitBlock, String fruitReplaceBlockName,ItemStack fruitSeed) {
		this.vanillaSapling = vanillaSapling;
		this.stemBlock = stemBlock;
		this.stemReplaceBlockName = stemReplaceBlockName;
		this.fruitBlock = fruitBlock;
		this.fruitReplaceBlockName = fruitReplaceBlockName;
		this.fruitSeed = fruitSeed;
	}

	public Material getStemBlock() {
		return stemBlock;
	}
	
	public String getStemReplaceBlockName(){
		return stemReplaceBlockName;
	}
	
	public Material getVanillaSapling() {
		return vanillaSapling;
	}
	
	public String getFruitReplaceBlockName() {
		return fruitReplaceBlockName;
	}

	
	public Material getFruitBlock() {
		return fruitBlock;
	}

	public ItemStack getSeed() {
		return fruitSeed;
	}

	public static TreeType isValidSeed(ItemStack seed) {
		return validSeed.get(CustomItem.getKey(seed));
	}
}
