package me.obito.chromiumcustom.items;

import me.obito.chromiumcustom.sapling.FungalSapling;
import me.obito.chromiumcustom.util.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.metadata.FixedMetadataValue;

public class Pinecone extends CustomItem {
	
	public Pinecone() {
		super(FungalSapling.class, 1, Material.SNOWBALL, "&fPinecone", false, "&7&oHarvested from pine trees", "&7&oThrowing this at someone will hurt them");
		
		super.setOnThrowConsumer((e) -> {
			e.getEntity().setMetadata("pinecone", new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("ChromiumItems"),1));
			e.getEntity().setCustomName("pinecone");
		});
	}
	
}
