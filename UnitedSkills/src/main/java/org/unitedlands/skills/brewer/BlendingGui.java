package org.unitedlands.skills.brewer;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;

import java.util.ArrayList;
import java.util.List;

public class BlendingGui {
    private Gui gui;
    private Player player;
    private ItemStack potion;
    private ItemStack otherPotion;
    private final UnitedSkills unitedSkills;

    public BlendingGui(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    public Gui createGui(Player player) {
        this.player = player;

        gui = Gui.gui()
                .title(Component.text("Potion Blender",
                        NamedTextColor.RED, TextDecoration.BOLD))
                .rows(4)
                .create();
        gui.setOpenGuiAction(event -> {
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
        });
        addGuiPattern();
        setBlendButton();
        return gui;
    }

    private void setBlendButton() {
        GuiItem greenGlass = ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("Blend", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false))
                .asGuiItem();
        gui.setItem(22, greenGlass);

        gui.addSlotAction(22, event -> {
            event.setCancelled(true);
            potion = event.getInventory().getItem(11);
            otherPotion = event.getInventory().getItem(15);

            if (!slotsArePotions()) {
                player.sendMessage(Component.text("You can only blend potions!", NamedTextColor.RED));
            }

            ItemStack blendedPotion = blendPotions();
            if (blendedPotion == null) {
                return;
            }
            player.getInventory().addItem(blendedPotion);
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
            gui.removeItem(11);
            gui.removeItem(15);
            gui.update();
        });
    }

    private boolean slotsArePotions() {
        return potion.getType().equals(Material.POTION) && otherPotion.getType().equals(Material.POTION);
    }

    private ItemStack blendPotions() {
        if (potion == null || otherPotion == null) {
            return null;
        }

        PotionType potionType = ((PotionMeta) potion.getItemMeta()).getBasePotionData().getType();
        PotionType otherPotionType = ((PotionMeta) otherPotion.getItemMeta()).getBasePotionData().getType();

        PotionData potionData = ((PotionMeta) potion.getItemMeta()).getBasePotionData();
        PotionData otherPotionData = ((PotionMeta) otherPotion.getItemMeta()).getBasePotionData();

        if (potionType.equals(otherPotionType)) {
            player.sendMessage(Component.text("You cannot blend two potions of the same type!", NamedTextColor.RED));
            return null;
        }

        if (isAlreadyBlended()) {
            player.sendMessage(Component.text("You can only blend a potion once!", NamedTextColor.RED));
            return null;
        }

        ItemStack blendedPotion = new ItemStack(Material.POTION);
        PotionMeta blendedPotionMeta = (PotionMeta) blendedPotion.getItemMeta();

        blendedPotionMeta.addCustomEffect(getBlendedEffect(potionData), false);
        blendedPotionMeta.addCustomEffect(getBlendedEffect(otherPotionData), false);

        blendedPotionMeta.setColor(getBlendedPotionColor(potionType, otherPotionType));
        blendedPotionMeta.displayName(getBlendedName(potionType, otherPotionType));

        blendedPotion.setItemMeta(blendedPotionMeta);
        return blendedPotion;
    }

    private @NotNull PotionEffect getBlendedEffect(PotionData potionData) {
        try {
        return potionData.getType().getEffectType().createEffect(getNewDuration(potionData), getAmplifier(potionData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getAmplifier(PotionData potionData) {
        int amplifier = 0;
        if (potionData.isUpgraded()) {
            String typeName = potionData.getType().toString();
            FileConfiguration config = unitedSkills.getConfig();

            amplifier = config.getInt("potions." + typeName + ".max_amplifier");
        }
        return amplifier;
    }


    private Component getBlendedName(PotionType potionType, PotionType otherPotionType) {

        String firstEffect = getFormattedEffect(potionType);
        String secondEffect = getFormattedEffect(otherPotionType);

        return Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("Blended", NamedTextColor.GOLD))
                .append(Component.text("]", NamedTextColor.GRAY))
                .append(Component.text(" Potion of ", NamedTextColor.WHITE)
                        .append(Component.text(firstEffect + " & " + secondEffect)))
                .decoration(TextDecoration.ITALIC, false);
    }

    @NotNull
    private String getFormattedEffect(PotionType potionType) {
        return WordUtils.capitalize(potionType.toString().toLowerCase().replace("_", " "));
    }


    private int getNewDuration(PotionData potionData) {
        String typeName = potionData.getType().toString();
        FileConfiguration config = unitedSkills.getConfig();
        int duration = config.getInt("potions." + typeName + ".default");

        if (potionData.isUpgraded()) {
            duration = config.getInt("potions." + typeName + ".upgraded");
        } else if (potionData.isExtended()) {
            duration = config.getInt("potions." + typeName + ".extended");
        }
        if (!(player.hasPermission("united.skills.blend.1"))) {
            duration = (int) (duration * 0.75);
        }
        return duration * 20;
    }

    private boolean isAlreadyBlended() {
        return ((PotionMeta) potion.getItemMeta()).hasCustomEffects() || ((PotionMeta) otherPotion.getItemMeta()).hasCustomEffects();
    }

    private @NotNull Color getBlendedPotionColor(PotionType potionType, PotionType otherPotionType) {
        final double weight = 0.5;
        final Color potionColor = potionType.getEffectType().getColor();
        final Color otherPotionColor = otherPotionType.getEffectType().getColor();
        int R = (int) (weight * potionColor.getRed() + weight * otherPotionColor.getRed());
        int G = (int) (weight * potionColor.getGreen() + weight * otherPotionColor.getGreen());
        int B = (int) (weight * potionColor.getBlue() + weight * otherPotionColor.getBlue());
        return Color.fromRGB(R, G, B);
    }


    private void addGuiPattern() {
        GuiItem redGlass = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                });
        GuiItem whiteGlass = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                });
        List<GuiItem> glassList = new ArrayList<>();
        glassList.add(redGlass);
        glassList.add(whiteGlass);
        gui.getFiller().fill(glassList);
        gui.removeItem(11);
        gui.removeItem(15);
    }
}
