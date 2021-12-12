package me.obito.chromiumcustom.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;

public enum TreeType implements Serializable {
	MANGO(Material.JUNGLE_SAPLING,
			Material.JUNGLE_LOG,
			Material.STRIPPED_JUNGLE_LOG,
			Material.JUNGLE_LEAVES,
			Material.JUNGLE_LEAVES,
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fMango"),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fMango Sapling")),
	APPLE(Material.OAK_SAPLING,
			Material.OAK_LOG,
			Material.STRIPPED_OAK_LOG,
			Material.OAK_LEAVES,
			Material.OAK_LEAVES,
			new ItemStack(Material.APPLE),
			new ItemStack(Material.APPLE)),
	GOLDEN_APPLE(Material.OAK_SAPLING,
			Material.OAK_LOG,
			Material.STRIPPED_OAK_LOG,
			Material.OAK_LEAVES,
			Material.OAK_LEAVES,
			new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fAncient Seed")),
	FUNGAL_BIRCH(Material.BIRCH_SAPLING,
			Material.BIRCH_LOG,
			Material.STRIPPED_BIRCH_LOG,
			Material.BIRCH_LEAVES,
			Material.BIRCH_LEAVES,
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fFungal Sapling"),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fFungal Sapling"),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fBracket Mushroom")),
	PINE(Material.SPRUCE_SAPLING,
			Material.SPRUCE_LOG,
			Material.STRIPPED_SPRUCE_LOG,
			Material.SPRUCE_LEAVES,
			Material.SPRUCE_LEAVES,
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fPinecone"),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fPine Sapling")),
	FLOWERING_ACACIA(Material.ACACIA_SAPLING,
			Material.ACACIA_LOG,
			Material.STRIPPED_ACACIA_LOG,
			Material.ACACIA_LEAVES,
			Material.ACACIA_LEAVES,
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fMimosa Flower"),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fFlowering Acacia Sapling")),
	SAUSAGE(Material.BIRCH_SAPLING,
			Material.BIRCH_LOG,
			Material.BROWN_MUSHROOM_BLOCK,
			Material.BROWN_MUSHROOM_BLOCK,
			Material.BIRCH_LEAVES,
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fSausage"),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fSausage"),
			me.obito.chromiumcustom.util.CustomItem.getItemByName("&fSausage"));
	
	private transient static HashMap<String, TreeType> validSeed = new HashMap<String, TreeType>();
	
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
	
	private Material vanillaSapling;
	private Material stemBlock;
	private Material stemReplaceBlock;
	private Material fruitBlock;
	private Material fruitReplaceBlock;
	private ItemStack fruitDrop;
	private ItemStack fruitSeed;
	private ItemStack logDrop;
	
	TreeType(Material vanillaSapling, Material stemBlock, Material stemReplaceBlock, Material fruitBlock, Material fruitReplaceBlock, ItemStack fruitDrop, ItemStack fruitSeed, ItemStack logDrop) {
		this.vanillaSapling = vanillaSapling;
		this.stemBlock = stemBlock;
		this.stemReplaceBlock = stemReplaceBlock;
		this.fruitBlock = fruitBlock;
		this.fruitReplaceBlock = fruitReplaceBlock;
		this.fruitDrop = fruitDrop;
		this.fruitSeed = fruitSeed;
		this.logDrop = logDrop;
	}
	
	TreeType(Material vanillaSapling, Material stemBlock, Material stemReplaceBlock, Material fruitBlock, Material fruitReplaceBlock, ItemStack fruitDrop, ItemStack fruitSeed) {
		this(vanillaSapling, stemBlock, stemReplaceBlock, fruitBlock, fruitReplaceBlock, fruitDrop, fruitSeed, new ItemStack(Material.AIR));
	}
	
	public Material getStemBlock() {
		return stemBlock;
	}
	
	public ItemStack getLogDrop() {
		return logDrop;
	}
	
	public Material getStemReplaceBlock(){
		return stemReplaceBlock;
	}
	
	public Material getVanillaSapling() {
		return vanillaSapling;
	}
	
	public Material getFruitReplaceBlock() {
		return fruitReplaceBlock;
	}
	
	public ItemStack getDrop() {
		return fruitDrop;
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
