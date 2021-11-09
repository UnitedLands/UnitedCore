package me.obito.chromiumcustom.items.weapon;

import de.tr7zw.nbtapi.NBTItem;
import me.obito.chromiumcustom.util.CustomItem;
import me.obito.chromiumcustom.util.Logger;
import me.obito.chromiumcustom.util.ToolCustomItem;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class GaelGreatsword extends ToolCustomItem {
	
	private static String NBT_COOLDOWN = "COOLDOWN";
	
	public GaelGreatsword() {
		super(GaelGreatsword.class, 1, Material.IRON_SWORD, "&4&lGael's Greatsword", true, "&6(Right-Click) &eUnleash dark magic (30s Cooldown)");
		
		NBTItem nbti = new NBTItem(super.getItem());
		
		nbti.setLong(NBT_COOLDOWN, new Date().getTime());
		
		super.setItem(nbti.getItem());
		
		super.setOnRightClickConsumer((e) -> {
			
			NBTItem nbt = new NBTItem(e.getItem());
			
			Long l = nbt.getLong(NBT_COOLDOWN);
			
			Random random = new Random();
			
			if(new Date().getTime() - l >= 30000) {
				Player p = e.getPlayer();
				World world = p.getWorld();
				ItemStack dark = CustomItem.getItemBySimilarName("Dark_Magic");
				for(int i = 0; i < 100; i++) {
					NBTItem nbtb = new NBTItem(dark);
					nbtb.setString("UUID", UUID.randomUUID().toString());
					nbtb.setString("SOURCE", p.getUniqueId().toString());
					Item b = world.dropItem(p.getLocation(), nbtb.getItem());
					b.setPickupDelay(20);
					b.setTicksLived(5800);
					b.setGravity(false);
					
					b.setVelocity(new Vector(random.nextDouble()*.25 * (random .nextBoolean() ? -1 : 1),
							random.nextDouble()*.1 * (random .nextBoolean() ? -1 : 1),
							random.nextDouble()*.25 * (random .nextBoolean() ? -1 : 1)));
				}
				nbt.setLong(NBT_COOLDOWN, new Date().getTime());
			} else {
				Logger.log(e.getPlayer(), String.format("This ability is still on cooldown for%6.2f s", (30-(new Date().getTime()-l)/1000.0)));
			}
			
			e.getPlayer().getInventory().setItemInMainHand(nbt.getItem());
		});
	}
	
}
