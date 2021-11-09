package me.obito.chromiumcustom.items;

import me.obito.chromiumcustom.util.EdibleCustomItem;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;

public class Mango extends EdibleCustomItem {
	
	public Mango() {
		super(Mango.class, 1, Material.GOLDEN_APPLE, "&fMango", false, 6, 
				new ArrayList<PotionEffect>(Arrays.asList(new PotionEffect(PotionEffectType.FAST_DIGGING, 30*20, 0))),
				"&7&oHarvested from mango trees");
	}
	/*
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Player p = e.getPlayer();
		
		if(!b.getType().equals(Material.JUNGLE_LEAVES))
			return;
		
		ItemStack tool = p.getItemInHand();
		Integer fortune = 0;
		if(tool != null) {
			fortune = tool.getEnchantments().get(Enchantment.LOOT_BONUS_BLOCKS);
			if(fortune == null)
				fortune = 0;
		}
		int chance = 180-(fortune*20);
		if(random.nextInt(chance <= 0 ? 1 : chance) == 1) {
			Inventory inv = p.getInventory();
			p.getWorld().dropItemNaturally(e.getBlock().getLocation(),Mango.getItem());
		}
	}
	*//*
	@EventHandler
	public void onEat(PlayerItemConsumeEvent e) {
		Player p = e.getPlayer();
		
		if(e.getItem().isSimilar(mango)) {
			//ItemStack item = new ItemStack(mango);
			//mango.setAmount(e.getItem().getAmount()-1);
			p.getItemInHand().setAmount(p.getItemInHand().getAmount()-1);
			//e.setItem(item);
			int newHunger = p.getFoodLevel()+6;
			p.setFoodLevel(newHunger > 20 ? 20 : newHunger);
			p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 30*20, 0));
			e.setCancelled(true);
		}
	}*/
}
