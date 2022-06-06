package org.unitedlands.items.trees;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.items.util.CustomItem;
import org.unitedlands.items.util.Logger;

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
			"midas_oak_log",
			Material.OAK_LEAVES,
			"midas_oak_leaves",
			CustomItem.getItemByName("&eMidas' Oak Sapling")),
	MIDAS_JUNGLE(Material.JUNGLE_SAPLING,
			Material.JUNGLE_LOG,
			"midas_jungle_log",
			Material.JUNGLE_LEAVES,
			"midas_jungle_leaves",
			CustomItem.getItemByName("&eMidas' Jungle Sapling")),
	ANCIENT_OAK(Material.OAK_SAPLING,
			Material.OAK_LOG,
			"ancient_oak_log",
			Material.OAK_LEAVES,
			"ancient_oak_leaves",
			CustomItem.getItemByName("&6Ancient Oak Sapling")),
	FUNGAL_BIRCH(Material.BIRCH_SAPLING,
			Material.BIRCH_LOG,
			"fungal_birch_log",
			Material.BIRCH_LEAVES,
			"fungal_birch_leaves",
			CustomItem.getItemByName("&fBracket Mushroom")),
	PINE(Material.SPRUCE_SAPLING,
			Material.SPRUCE_LOG,
			"pine_tree_log",
			Material.SPRUCE_LEAVES,
			"pine_tree_leaves",
			CustomItem.getItemByName("&fPine Sapling")),
	FLOWERING_ACACIA(Material.ACACIA_SAPLING,
			Material.ACACIA_LOG,
			"flowering_acacia_log",
			Material.ACACIA_LEAVES,
			"flowering_acacia_leaves",
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
