package org.unitedlands.alcohol.listeners;

import com.dre.brewery.api.events.PlayerFillBottleEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.alcohol.Brand;

import java.util.ArrayList;

public class PlayerListener implements Listener {
    private final Brand brand;

    public PlayerListener(Brand brand) {
        this.brand = brand;
    }

    @EventHandler
    public void onPlayerFillBottle(PlayerFillBottleEvent event) {
        Player player = event.getPlayer();
        ItemStack bottle = event.getBottle();
        ItemMeta bottleMeta = bottle.getItemMeta();
        var bottleLore = new ArrayList<Component>();

        bottleLore.add(getBrewedByComponent(player));
        bottleLore.add(getSloganComponent(player));
        bottleMeta.lore(bottleLore);
        bottle.setItemMeta(bottleMeta);

    }

    private Component getBrewedByComponent(Player player) {
        String name = player.getName();
        if (brand.hasBrand(player)) {
            name = brand.getPlayerBrand(player);
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

    private Component getSloganComponent(Player player) {
        String slogan;

        if (!brand.hasBrand(player)) {
            return null;
        }

        String brandName = brand.getPlayerBrand(player);
        slogan = brand.getBrandSlogan(brandName);

        if (slogan == null) {
            return null;
        }

        return Component
                .text("» ", NamedTextColor.DARK_GRAY)
                .append(Component.text('"' + slogan + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED))
                .decoration(TextDecoration.ITALIC, false);
    }

}
