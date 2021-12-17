package me.obito.chromiumcustom.items.weapon;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import de.tr7zw.nbtapi.NBTItem;
import me.obito.chromiumcustom.util.BiomeSelector;
import me.obito.chromiumcustom.util.GUI;
import me.obito.chromiumcustom.util.Logger;
import me.obito.chromiumcustom.util.ToolCustomItem;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;

public class Landscaper extends ToolCustomItem {
	
	private static String NBT_BIOME = "BIOME";
	
	public Landscaper() {
		super(Landscaper.class, 1, Material.DIAMOND_PICKAXE, "&f&lLandscaping Kit", true, "&6(Right-Click) &eChange the biome of the chunk you're in to the selected biome", "&6(Shift-Right-Click) &eOpens GUI to select the biome type for this tool");
		
		NBTItem nbti = new NBTItem(super.getItem());
		
		nbti.setString(NBT_BIOME, Biome.PLAINS.toString());
		
		super.setItem(nbti.getItem());
		
		super.setOnRightClickConsumer((e) -> {
			Player p = e.getPlayer();
			ToolCustomItem i = ToolCustomItem.getItem(p.getItemInHand());
			NBTItem nbt = new NBTItem(p.getItemInHand());
			try {
				TownBlock b = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(p.getLocation()));
				Resident r = TownyUniverse.getInstance().getResident(p.getName());
				if(r != null) {
					if(b != null) {
						if(r.getTown().equals(b.getTown())){
							if(p.isSneaking()) {
								if(i != null) { 
									GUI.getGUI(BiomeSelector.getId()).open(p);
								}
							} else {
								Biome biome = Biome.valueOf(nbt.getString(NBT_BIOME));
								Chunk chunk = p.getLocation().getChunk();
								int cX = chunk.getX() * 16;
								int cZ = chunk.getZ() * 16;
								for(int x = 0 ; x < 16; x++) {
									for(int z = 0 ; z < 16; z++) {
										p.getWorld().setBiome(cX+x, cZ+z, biome);
										p.getWorld().spawnParticle(Particle.CRIT_MAGIC, cX+x, p.getLocation().getY(), cZ+z, 1);
									}
								}
								Logger.log(p, String.format("&aChanged biome of this chunk to &e%s", biome.toString()));
							}
						} else {
							Logger.log(p, "&cYou must be in a town to do this and own this land!");
						}
					}
				}
			} catch (NotRegisteredException e1) {
				Logger.log(p, "&cYou must be in a town to do this and own this land!");
			}
			
			
		});
		
		NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager()
				.getPlugin("ChromiumItems"), "landscaper");

		// Create our custom recipe variable
		ShapedRecipe recipe = new ShapedRecipe(key, super.getItem());

		// Here we will set the places. E and S can represent anything, and the letters can be anything. Beware; this is case sensitive.
		recipe.shape("ABC", "NSN", "NNN");

		// Set what the letters represent.
		// E = Emerald, S = Stick
		recipe.setIngredient('A', Material.DIAMOND_PICKAXE);
		recipe.setIngredient('B', Material.DIAMOND_SHOVEL);
		recipe.setIngredient('C', Material.DIAMOND_AXE);
		recipe.setIngredient('S', Material.NETHER_STAR);
		recipe.setIngredient('N', Material.IRON_BLOCK);
		// Finally, add the recipe to the bukkit recipes
		Bukkit.addRecipe(recipe);
		
	}

}
