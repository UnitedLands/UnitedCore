package me.obito.chromiumcustom.items.weapon;

import de.tr7zw.nbtapi.NBTItem;
import me.obito.chromiumcustom.util.ToolCustomItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Whisper extends ToolCustomItem {
	
	private static String name = "&6&lWhisper";
	private static String NBT_SHOT = "Shot";
	
	public Whisper() {
		super(Whisper.class, 1, Material.BOW, name+" &70/4", true, "&7&oEvery 4th shot deals &e&o200% &7&odamage", "&7&oThe 4th shot deals additional execution damage:", "&e&o(Targets Max Health - Targets Current Health) &7&odamage", "&7&oGrants &e&oSpeed II &7&ofor &e&o1s &7&oon 4th shot");
		
		NBTItem nbti = new NBTItem(super.getItem());
		
		nbti.setInteger(NBT_SHOT, 0);
		
		super.setItem(nbti.getItem());
		
		super.setOnBowFireConsumer((e)->{
			NBTItem nbt = new NBTItem(e.getBow());
			
			int shot = nbt.getInteger(NBT_SHOT)+1;
			if(shot == 4) {
				nbt.setInteger(NBT_SHOT, 0);
				e.getProjectile().setMetadata("FOUR", new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("ChromiumCustom"),4));
				e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
			} else {
				nbt.setInteger(NBT_SHOT, shot);
			}
			ItemStack i = nbt.getItem();
			
			ItemMeta meta = i.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Whisper.getName() + (shot == 4 ? " &5&lFOUR" : " &7"+shot+"/4")));
			i.setItemMeta(meta);
			
			if(e.getEntity() instanceof Player) {
				Player p = (Player)e.getEntity();
				p.getInventory().setItemInMainHand(i);
			}
		});
	}
	
	public static String getName() {
		return name;
	}
	
}
