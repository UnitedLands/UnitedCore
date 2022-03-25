package org.unitedlands.alcohol.listeners;

import com.dre.brewery.api.events.PlayerFillBottleEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.alcohol.UnitedBrands;
import org.unitedlands.alcohol.Util;
import org.unitedlands.alcohol.brand.Brand;

import java.util.ArrayList;

public class PlayerListener implements Listener {

    private final UnitedBrands unitedBrands;

    public PlayerListener(UnitedBrands unitedBrands) {
        this.unitedBrands = unitedBrands;
    }

    @EventHandler
    public void onPlayerFillBottle(PlayerFillBottleEvent event) {
        Player player = event.getPlayer();
        ItemStack bottle = event.getBottle();
        ItemMeta bottleMeta = bottle.getItemMeta();
        var bottleLore = new ArrayList<Component>();

        Brand brand = Util.getPlayerBrand(player);

        bottleLore.add(getBrewedByComponent(player));
        bottleLore.add(getSloganComponent(brand, player));
        bottleMeta.lore(bottleLore);
        bottle.setItemMeta(bottleMeta);

    }

    private Component getBrewedByComponent(Player player) {
        String name = player.getName();
        if (Util.hasBrand(player)) {
            name = Util.getPlayerBrand(player).getBrandName();
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

    private Component getSloganComponent(Brand brand, Player player) {
        String slogan;

        if (!Util.hasBrand(player)) {
            return null;
        }

        slogan = brand.getBrandSlogan();

        if (slogan == null) {
            return null;
        }

        return Component
                .text("» ", NamedTextColor.DARK_GRAY)
                .append(Component.text('"' + slogan + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED))
                .decoration(TextDecoration.ITALIC, false);
    }

}
