package me.obito.chromiumcustom.items;

import me.obito.chromiumcustom.util.CustomItem;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Mimosa extends CustomItem {
	
	public Mimosa() {
		super(Mimosa.class, 1, Material.DANDELION, "&fMimosa Flower", false, "&7&oHarvested from flowering acacia trees");
		
		super.setOnRightClickConsumer((e) -> {
			Block b = e.getClickedBlock();
			if(b != null) 
				if(b.getType() != null) 
					if(b.getType().equals(Material.CAULDRON))
						return;
			e.setCancelled(true);
		});
	}
	
}
