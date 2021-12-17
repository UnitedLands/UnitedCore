package me.obito.chromiumcustom.trees;

import me.obito.chromiumcustom.util.GenericLocation;
import me.obito.chromiumcustom.util.Logger;
import me.obito.chromiumcustom.util.SerializableData;
import me.obito.chromiumcustom.util.TreeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

public class Tree implements Listener, Serializable {
	
	private static final long serialVersionUID = 1L;
	private static HashMap<Location, TreeType> sapling = new HashMap<Location, TreeType>();
	private static HashMap<Location, TreeType> fruit = new HashMap<Location, TreeType>();
	private static HashMap<Location, TreeType> log = new HashMap<Location, TreeType>();
	private transient Random random = new Random();
	
	@SuppressWarnings("unchecked")
	public static void load() {
		HashMap<GenericLocation, String> safeFruit = (HashMap<GenericLocation, String>) SerializableData.Farming.readFromDatabase("fruit.dat");
		HashMap<GenericLocation, String> safeSapling = (HashMap<GenericLocation, String>) SerializableData.Farming.readFromDatabase("sapling.dat");
		HashMap<GenericLocation, String> safeLog = (HashMap<GenericLocation, String>) SerializableData.Farming.readFromDatabase("log.dat");
		
		if(safeSapling != null) {
			Logger.log(String.format("&aLoading cached trees &6[&e%d&6]...", safeSapling.size()));
			safeSapling.forEach((x,y) -> {
				//System.out.println(y+":"+TreeType.valueOf(y));
				sapling.put(x.getLocation(), TreeType.valueOf(y));
			});
		} else {
			Logger.log("&aLoading cached trees &6[&e0&6]...");
		}
		Logger.log(String.format("&aLoaded cached trees &6[&e%d&6]!", sapling.size()));
		
		if(safeFruit != null) {
			Logger.log(String.format("&aLoading cached fruit &6[&e%d&6]...", safeFruit.size()));
			safeFruit.forEach((x,y) -> {
				//System.out.println(y+":"+TreeType.valueOf(y));
				if(!x.getLocation().getBlock().getType().equals(Material.AIR) && !fruit.containsKey(x.getLocation()))
					fruit.put(x.getLocation(), TreeType.valueOf(y));
			});
			Logger.log(String.format("&aLoaded cached fruit &6[&e%d&6]... &cExpunged &6[&e%d&6] &cInvalid Fruits", fruit.size(), safeFruit.size()-fruit.size()));
		} else {
			Logger.log("&aLoading cached fruit &6[&e0&6]...");
		}
		
		if(safeLog != null) {
			Logger.log(String.format("&aLoading cached logs &6[&e%d&6]...", safeLog.size()));
			safeLog.forEach((x,y) -> {
				//System.out.println(y+":"+TreeType.valueOf(y));
				log.put(x.getLocation(), TreeType.valueOf(y));
			});
		} else {
			Logger.log("&aLoading cached logs &6[&e0&6]...");
		}
		
		Logger.log(String.format("&aLoaded cached logs &6[&e%d&6]!", log.size()));
		
	}
	
	@EventHandler
	public void onPlace(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		
		if(b == null) {
			return;
		}
		if(item == null) {
			return;
		}
		if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		if(b.getType().equals(Material.CAULDRON))
			return;
			
		TreeType tree = TreeType.isValidSeed(item);
		if(tree != null) {
			if(b.getType().equals(Material.GRASS_BLOCK) || b.getType().equals(Material.DIRT)) {
				Block above = b.getWorld().getBlockAt(b.getLocation().add(0, 1, 0));
				if(above.getType().equals(Material.AIR)) {
					above.setType(tree.getVanillaSapling());
					e.getItem().setAmount(e.getItem().getAmount()-1);
					sapling.put(above.getLocation(), tree);
				}
			} else {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onGrow(StructureGrowEvent e) {
		Location l = e.getLocation();
		TreeType tree = sapling.get(l);
		
		if(tree != null) {
			sapling.remove(l);
			for(BlockState b : e.getBlocks()) {
				if(b.getType().equals(tree.getStemBlock())) {
					b.setType(tree.getStemReplaceBlock());
					if(!tree.getLogDrop().getType().equals(Material.AIR)) {
						log.put(b.getLocation(), tree);
					}
				}
				if(b.getType().equals(tree.getFruitReplaceBlock())) {
					b.setType(tree.getFruitBlock());
					if(!fruit.containsKey(b.getLocation()))
						fruit.put(b.getLocation(), tree);
				}
			}
		}
	}
	
	@EventHandler
	public void onDecay(LeavesDecayEvent e) {
		if(e.isCancelled())
			return;
		TreeType t = removeMappedLocation(e.getBlock().getLocation());
		if(t != null) {
			e.getBlock().setType(Material.AIR);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if(e.isCancelled())
			return;
		TreeType t = removeMappedLocation(e.getBlock().getLocation());
		if(t != null) {
			//Jobs.getPlayerManager().getJobsPlayer(e.getPlayer()).progression.get(0).;
			e.setDropItems(false);
		}
	}
	
	@EventHandler
	public void onDestroy(BlockExplodeEvent e) {
		if(e.isCancelled())
			return;
		for(Block b: e.blockList()) {
			removeMappedLocation(b.getLocation());
		}
	}
	
	@EventHandler
	public void onDestroy(EntityExplodeEvent e) {
		if(e.isCancelled())
			return;
		for(Block b: e.blockList()) {
			removeMappedLocation(b.getLocation());
		}
	}
	
	@EventHandler
	public void onDestroy(BlockPistonExtendEvent e) {
		if(e.isCancelled())
			return;
		for(Block b: e.getBlocks()) {
			removeMappedLocation(b.getLocation());
		}
	}
	
	public TreeType removeMappedLocation(Location l) {
		if(sapling.containsKey(l)) {
			TreeType ret = sapling.get(l);
			l.getWorld().dropItemNaturally(l, sapling.get(l).getSeed());
			sapling.remove(l);
			l.getBlock().setType(Material.AIR);
			return ret;
		}
		if(fruit.containsKey(l)) {
			TreeType ret = fruit.get(l);
			if(random.nextInt(5) == 1) {
				if(random.nextBoolean()) {
						l.getWorld().dropItemNaturally(l, fruit.get(l).getDrop());
				} else {
					if(!fruit.get(l).equals(TreeType.GOLDEN_APPLE))
						l.getWorld().dropItemNaturally(l, fruit.get(l).getSeed());
				}
			}
			l.getBlock().setType(Material.AIR);
			fruit.remove(l);
			return ret;
		}
		if(log.containsKey(l)) {
			TreeType ret = log.get(l);
			l.getWorld().dropItemNaturally(l, log.get(l).getLogDrop());
			l.getWorld().dropItemNaturally(l, new ItemStack(l.getBlock().getType()));
			log.remove(l);
			l.getBlock().setType(Material.AIR);
			return ret;
		}
		return null;
	}
	
	public static HashMap<GenericLocation, String> getSerializableSaplings(){
		HashMap<GenericLocation, String> safeSapling = new HashMap<GenericLocation, String>();
		sapling.forEach((x,y) -> {
			safeSapling.put(new GenericLocation(x), y.name());
		});
		return safeSapling;
	}
	
	public static HashMap<GenericLocation, String> getSerializableFruit(){
		HashMap<GenericLocation, String> safeFruit = new HashMap<GenericLocation, String>();
		fruit.forEach((x,y) -> {
			safeFruit.put(new GenericLocation(x), y.name());
		});
		return safeFruit;
	}
	
	public static HashMap<GenericLocation, String> getSerializableLog(){
		HashMap<GenericLocation, String> safeLog = new HashMap<GenericLocation, String>();
		log.forEach((x,y) -> {
			safeLog.put(new GenericLocation(x), y.name());
		});
		return safeLog;
	}
}
