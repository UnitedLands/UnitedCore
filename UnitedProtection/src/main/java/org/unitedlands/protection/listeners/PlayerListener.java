package org.unitedlands.protection.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.protection.UnitedProtection;
import org.unitedlands.wars.war.WarDatabase;

import java.util.List;

public class PlayerListener implements Listener {

    private final TownyAPI towny = TownyAPI.getInstance();
    private final UnitedProtection unitedProtection;
    private Player player;

    public PlayerListener(UnitedProtection unitedProtection) {
        this.unitedProtection = unitedProtection;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        player = event.getPlayer();
        Block block = event.getBlock();

        if (isProtectedBlock(block)) {
            player.sendMessage(getMessage("break-deny-message"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        player = event.getPlayer();
        Block block = event.getBlock();

        if (isProtectedBlock(block)) {
            player.sendMessage(getMessage("place-deny-message"));
            event.setCancelled(true);
        }
    }

    private int getProtectedY() {
        return getConfiguration().getInt("protect-anything-below");
    }

    private List<String> getWhitelistedBlocks() {
        return getConfiguration().getStringList("block-whitelist");
    }

    @NotNull
    private FileConfiguration getConfiguration() {
        return unitedProtection.getConfig();
    }

    private boolean isWhitelistedBlock(Block block) {
        return getWhitelistedBlocks().contains(block.getType().toString());
    }

    private boolean isInTown(Block block) {
        Location blockLocation = block.getLocation();
        if (towny.isWilderness(blockLocation)) {
            return false;
        }

        Resident resident = towny.getResident(player);
        @Nullable TownBlock townBlock = towny.getTownBlock(blockLocation);
        if (townBlock == null) return false;
        Town town = townBlock.getTownOrNull();
        if (town == null || !resident.hasTown()) {
            return false;
        }
        if (WarDatabase.hasWar(town)) {
            return false;
        }
        return resident.getTownOrNull().equals(town) || town.getTrustedResidents().contains(resident);
    }

    private String getMessage(String message) {
        message = getConfiguration().getString(message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private boolean isProtectedBlock(Block block) {
        int blockY = block.getLocation().getBlockY();
        String worldName = block.getWorld().getName();

        if (!worldName.equals("world_earth")) {
            return false;
        }

        if (isWhitelistedBlock(block)) {
            return false;
        }

        if (player.hasPermission("united.protection.bypass")) {
            return false;
        }

        return blockY < getProtectedY() && !isInTown(block);
    }
}
