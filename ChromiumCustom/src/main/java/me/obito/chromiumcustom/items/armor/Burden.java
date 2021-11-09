package me.obito.chromiumcustom.items.armor;

import me.obito.chromiumcustom.util.CustomItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class Burden extends CustomItem {

	public Burden() {
		super(Burden.class, Material.LEATHER_CHESTPLATE, "&8&lDeath's Burden", true, "&7&oI-Frames no longer protect you from damage", "&7&oMakes you immune to knockback", "&7&oWhile worn your armor is &9&ounbreakable");
		
		ItemStack i = super.getItem();
		
		i.addEnchantment(Enchantment.BINDING_CURSE, 1);
		
		super.setItem(i);
	}
	
}
