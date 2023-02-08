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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.brands.BreweryDatabase;
import org.unitedlands.brands.brewery.Brewery;
import org.unitedlands.brands.stats.BrandPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {
    private BrandPlayer brandPlayer;
    private Player player;
    private Brewery brewery;

    @EventHandler
    public void onPlayerFillBottle(PlayerFillBottleEvent event) {
        player = event.getPlayer();
        brandPlayer = BreweryDatabase.getBrandPlayer(player);
        ItemStack bottle = event.getBottle();
        brandBottle(bottle);
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
            brandPlayer = BreweryDatabase.getBrandPlayer(player);
            brandPlayer.increaseStat( "total-stars", starAmount);
            brandPlayer.increaseStat("brews-drunk", 1);
            brandPlayer.updateAverageStars();
        }

        brewery = getBreweryFromItemMeta(itemMeta);
        if (brewery == null) {
            return;
        }
        if (brewery.getMembers().contains(playerUUID) || isBreweryOwner(playerUUID)) {
            return;
        }
        brewery.increaseStat("total-stars", starAmount);
        brewery.increaseStat("brews-drunk", 1);
        brewery.updateAverageStars();
    }

    private boolean isBreweryOwner(String playerUUID) {
        return brewery.getOwner().getUniqueId().toString().equals(playerUUID);
    }

    private void brandBottle(ItemStack bottle) {
        ItemMeta bottleMeta = bottle.getItemMeta();
        List<Component> bottleLore = bottle.lore();

        brewery = BreweryDatabase.getPlayerBrewery(player);

        if (bottleLore == null) {
            bottleLore = new ArrayList<>();
        }

        if (brewery != null) {
            bottleLore.add(0, getBreweryComponent());
            bottleLore.add(1, getBrewedByComponentInBrewery());
            if (!brewery.getSlogan().equals("")) {
                bottleLore.add(1, getSloganComponent());
                bottleLore.add(3, Component.text(""));
            }
            brewery.increaseStat("brews-made", 1);
        } else {
            bottleLore.add(0, getBrewedByComponent());
        }

        brandPlayer.increaseStat( "brews-made", 1);
        bottleMeta.lore(bottleLore);
        bottle.setItemMeta(bottleMeta);
    }

    private Component getBreweryComponent() {
        String name = brewery.getName();
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
        String slogan = brewery.getSlogan();
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
                return BreweryDatabase.getBreweryFromName(ChatColor.stripColor(breweryName));
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
