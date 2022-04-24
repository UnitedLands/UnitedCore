package org.unitedlands.protection.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
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
import org.unitedlands.protection.UnitedProtection;

import java.util.List;

public class PlayerListener implements Listener {

    private final TownyAPI towny = TownyAPI.getInstance();
    private final UnitedProtection unitedProtection;

    public PlayerListener(UnitedProtection unitedProtection) {
        this.unitedProtection = unitedProtection;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!canModifyBlock(player, block)) {
            player.sendMessage(getMessage("break-deny-message"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!canModifyBlock(player, block)) {
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

    private boolean isInTown(Resident resident) {
        Player player = resident.getPlayer();
        Location location = player.getLocation();
        if (towny.isWilderness(location)) {
            return false;
        }
        Town town = towny.getTownBlock(location).getTownOrNull();
        return resident.getTownOrNull().equals(town);
    }

    private String getMessage(String message) {
        message = getConfiguration().getString(message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private boolean canModifyBlock(Player player, Block block) {
        Resident resident = towny.getResident(player.getUniqueId());
        int playerY = player.getLocation().getBlockY();
        String worldName = block.getWorld().getName();

        if (!worldName.equals("world_earth")) {
            return true;
        }

        if (isWhitelistedBlock(block)) {
            return true;
        }

        if (player.hasPermission("united.protection.bypass")) {
            return true;
        }

        return playerY >= getProtectedY() || isInTown(resident);
    }
}
