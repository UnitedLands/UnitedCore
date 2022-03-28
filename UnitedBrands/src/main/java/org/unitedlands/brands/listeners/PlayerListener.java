package org.unitedlands.brands.listeners;

import com.dre.brewery.Brew;
import com.dre.brewery.api.events.PlayerFillBottleEvent;
import com.dre.brewery.utility.BUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.brands.UnitedBrands;
import org.unitedlands.brands.Util;
import org.unitedlands.brands.brewery.Brewery;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

    private final UnitedBrands unitedBrands;

    public PlayerListener(UnitedBrands unitedBrands) {
        this.unitedBrands = unitedBrands;
    }

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

        bottleLore.add(0, getBrewedByComponent(player));
        bottleLore.add(1, getSloganComponent(brewery, player));
        bottleLore.add(2, Component.text(""));
        bottleMeta.lore(bottleLore);
        bottle.setItemMeta(bottleMeta);
    }

    private Component getBrewedByComponent(Player player) {
        String name = player.getName();
        if (Util.hasBrewery(player)) {
            name = Util.getPlayerBrewery(player).getBreweryName();
            return Component
                    .text("Product Of ", NamedTextColor.YELLOW)
                    .append(Component.text(name, NamedTextColor.GOLD))
                    .decoration(TextDecoration.ITALIC, false);
        }
        return Component
                .text("Brewed by ", NamedTextColor.YELLOW)
                .append(Component.text(name, NamedTextColor.GOLD))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component getSloganComponent(Brewery brewery, Player player) {
        String slogan;

        if (!Util.hasBrewery(player)) {
            return null;
        }

        slogan = brewery.getBrewerySlogan();

        if (slogan == null) {
            return null;
        }

        return Component
                .text("» ", NamedTextColor.DARK_GRAY)
                .append(Component.text('"' + slogan + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED))
                .decoration(TextDecoration.ITALIC, false);
    }

}
