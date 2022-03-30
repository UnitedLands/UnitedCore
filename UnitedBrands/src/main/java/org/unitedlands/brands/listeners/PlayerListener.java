package org.unitedlands.brands.listeners;

import com.dre.brewery.api.events.PlayerFillBottleEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.brands.Util;
import org.unitedlands.brands.brewery.Brewery;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerFillBottle(PlayerFillBottleEvent event) {
        Player player = event.getPlayer();
        ItemStack bottle = event.getBottle();
        addBrandToFilledBottle(player, bottle);
    }

    private void addBrandToFilledBottle(Player player, ItemStack bottle) {
        ItemMeta bottleMeta = bottle.getItemMeta();
        List<Component> bottleLore = bottle.lore();

        Brewery brewery = Util.getPlayerBrewery(player);

        if (bottleLore == null) {
            bottleLore = new ArrayList<>();
        }
        if (brewery != null) {
            bottleLore.add(0, getBreweryComponent(brewery));
            bottleLore.add(1, getSloganComponent(brewery));
            if (brewery.getBrewerySlogan() != null) {
                // add a space separator only if there's a slogan
                bottleLore.add(2, Component.text(""));
            }
        } else {
            bottleLore.add(0, getBrewedByComponent(player));
        }

        bottleMeta.lore(bottleLore);
        bottle.setItemMeta(bottleMeta);
    }

    private Component getBreweryComponent(Brewery brewery) {
        String name = brewery.getBreweryName();
        return Component
                .text("Product Of ", NamedTextColor.YELLOW)
                .append(Component.text(name, NamedTextColor.GOLD))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component getBrewedByComponent(Player player) {
        return Component
                .text("Brewed by ", NamedTextColor.YELLOW)
                .append(Component.text(player.getName(), NamedTextColor.GOLD))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component getSloganComponent(Brewery brewery) {
        String slogan = brewery.getBrewerySlogan();
        if (slogan == null) {
            return null;
        }
        return Component
                .text("» ", NamedTextColor.DARK_GRAY)
                .append(Component.text('"' + slogan + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED))
                .decoration(TextDecoration.ITALIC, false);
    }

}
