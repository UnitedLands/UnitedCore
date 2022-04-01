package org.unitedlands.brands.listeners;

import com.dre.brewery.api.events.PlayerFillBottleEvent;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.brands.Util;
import org.unitedlands.brands.brewery.Brewery;
import org.unitedlands.brands.stats.PlayerStatsFile;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {
    private final PlayerStatsFile playerStatsFile;
    private Player player;
    private Brewery brewery;

    public PlayerListener(PlayerStatsFile playerStatsFile) {
        this.playerStatsFile = playerStatsFile;
    }

    @EventHandler
    public void onPlayerFillBottle(PlayerFillBottleEvent event) {
        player = event.getPlayer();
        ItemStack bottle = event.getBottle();
        addBrandToFilledBottle(bottle);
    }

    @EventHandler
    public void onPlayerDrinkBrew(BrewDrinkEvent event) {
        ItemMeta itemMeta = event.getItemMeta();
        player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        int starAmount = event.getBrew().getQuality() / 2;

        if (player.equals(getPlayerFromItemMeta(itemMeta))) {
            return;
        } else {
            playerStatsFile.increaseStat(player, "total-stars", starAmount);
            playerStatsFile.increaseStat(player, "brews-drunk", 1);
            playerStatsFile.updateAverageStars(player);
        }

        brewery = getBreweryFromItemMeta(itemMeta);
        if (brewery == null) {
            return;
        }
        if (brewery.getBreweryMembers().contains(playerUUID) || isBreweryOwner(playerUUID)) {
            return;
        }
        brewery.increaseStat("total-stars", starAmount);
        brewery.increaseStat("brews-drunk", 1);
        brewery.updateAverageStars();
    }

    private boolean isBreweryOwner(String playerUUID) {
        return brewery.getBreweryOwner().getUniqueId().toString().equals(playerUUID);
    }

    private void addBrandToFilledBottle(ItemStack bottle) {
        ItemMeta bottleMeta = bottle.getItemMeta();
        List<Component> bottleLore = bottle.lore();

        brewery = Util.getPlayerBrewery(player);

        if (bottleLore == null) {
            bottleLore = new ArrayList<>();
        }

        if (brewery != null) {
            bottleLore.add(0, getBreweryComponent());
            bottleLore.add(1, getBrewedByComponentInBrewery());
            if (brewery.getBrewerySlogan() != null) {
                bottleLore.add(1, getSloganComponent());
                bottleLore.add(3, Component.text(""));
            }
            brewery.increaseStat("brews-made", 1);
        } else {
            bottleLore.add(0, getBrewedByComponent());
        }

        playerStatsFile.increaseStat(player, "brews-made", 1);
        bottleMeta.lore(bottleLore);
        bottle.setItemMeta(bottleMeta);
    }

    private Component getBreweryComponent() {
        String name = brewery.getBreweryName();
        return Component
                .text("Product Of ", NamedTextColor.YELLOW)
                .append(Component.text(name, NamedTextColor.GOLD))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component getBrewedByComponentInBrewery() {
        return Component
                .text("Brewed by ", NamedTextColor.DARK_GRAY)
                .append(Component.text(player.getName(), NamedTextColor.DARK_GRAY))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component getSloganComponent() {
        String slogan = brewery.getBrewerySlogan();
        return Component
                .text("» ", NamedTextColor.DARK_GRAY)
                .append(Component.text('"' + slogan + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component getBrewedByComponent() {
        return Component
                .text("Brewed by ", NamedTextColor.YELLOW)
                .append(Component.text(player.getName(), NamedTextColor.GOLD))
                .decoration(TextDecoration.ITALIC, false);
    }


    private Brewery getBreweryFromItemMeta(ItemMeta meta) {
        if (meta == null) {
            return null;
        }
        if (!meta.hasLore()) {
            return null;
        }
        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.contains("Product Of ")) {
                // "Product Of Brewery" -> "Brewery"
                String breweryName = line.replace("Product Of ", "");
                return Util.getBreweryFromName(ChatColor.stripColor(breweryName));
            }
        }

        return null;
    }

    private OfflinePlayer getPlayerFromItemMeta(ItemMeta meta) {
        if (meta == null) {
            return null;
        }
        if (!meta.hasLore()) {
            return null;
        }
        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.contains("Brewed by ")) {
                // "Brewed by Maroon28" -> "Maroon28"
                String brewerName = line.replace("Brewed by ", "");
                return Bukkit.getPlayer(ChatColor.stripColor(brewerName));
            }
        }
        return null;
    }

}
