package org.unitedlands.skills.brewer;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    private List<Component> getBlendButtonLore() {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        FileConfiguration config = getConfiguration();

        List<String> lore = config.getStringList("blending.blend-button-lore");
        List<Component> parsedLore = new ArrayList<>();
        for (String line : lore) {
            Component parsedLine = miniMessage.deserialize(line);
            parsedLore.add(parsedLine);
        }
        return parsedLore;
    }

    private void setBlendButton() {
        GuiItem greenGlass = ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("Blend", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(getBlendButtonLore())
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
            giveBlendedPotion(blendedPotion);
            gui.update();
        });
    }

    private void giveBlendedPotion(ItemStack blendedPotion) {
        player.getInventory().addItem(blendedPotion);
        player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
        gui.removeItem(11);
        gui.removeItem(15);
    }

    private boolean slotsArePotions() {
        Material slotOneType = potion.getType();
        Material slotTwoType = otherPotion.getType();
        if (slotOneType.equals(Material.SPLASH_POTION)) {
            return slotOneType.equals(slotTwoType);
        }
        return slotOneType.equals(Material.POTION) && slotOneType.equals(slotTwoType);
    }

    private ItemStack blendPotions() {
        if (potion == null || otherPotion == null) {
            return null;
        }

        final PotionMeta potionMeta = getPotionMeta(potion);
        final PotionMeta otherPotionMeta = getPotionMeta(otherPotion);

        PotionData potionData = potionMeta.getBasePotionData();
        PotionData otherPotionData = otherPotionMeta.getBasePotionData();

        if (getExtraEffects() == 0) {
            player.sendMessage(Component.text("You've reached the max effects on this potion!", NamedTextColor.RED));
            return null;
        }

        ItemStack blendedPotion = new ItemStack(Material.POTION);
        if (hasSplashPotions()) {
            blendedPotion = new ItemStack(Material.SPLASH_POTION);
        }
        PotionMeta blendedPotionMeta = getPotionMeta(blendedPotion);

        if (hasCustomEffects()) {
            doubleBlendPotion(potionMeta, otherPotionMeta, blendedPotionMeta);
        } else {
            blendedPotionMeta.addCustomEffect(getBlendedEffect(potionData), false);
            blendedPotionMeta.addCustomEffect(getBlendedEffect(otherPotionData), false);
            blendedPotionMeta.displayName(getBlendedName());
        }

        blendedPotionMeta.setColor(getBlendedPotionColor());
        blendedPotion.setItemMeta(blendedPotionMeta);
        return blendedPotion;
    }

    private boolean hasSplashPotions() {
        return gui.getInventory().getItem(11).getType().equals(Material.SPLASH_POTION);
    }

    private void doubleBlendPotion(PotionMeta potionMeta, PotionMeta otherPotionMeta, PotionMeta blendedPotionMeta) {
        PotionMeta doublePotionMeta;
        PotionData extraPotionData;
        PotionData potionData = potionMeta.getBasePotionData();
        PotionData otherPotionData = otherPotionMeta.getBasePotionData();
        if (potionMeta.hasCustomEffects()) {
            doublePotionMeta = potionMeta;
            extraPotionData = otherPotionData;
        } else {
            doublePotionMeta = otherPotionMeta;
            extraPotionData = potionData;
        }
        for (PotionEffect effect : doublePotionMeta.getCustomEffects()) {
            addEffect(blendedPotionMeta, effect);
            blendedPotionMeta.addCustomEffect(getBlendedEffect(extraPotionData), false);
            blendedPotionMeta.displayName(getDoubleBlendedName());
        }
    }

    private void addEffect(PotionMeta blendedPotionMeta, PotionEffect effect) {
        final PotionEffect potionEffect = new PotionEffect(effect.getType(), (int) (effect.getDuration() * 0.80), effect.getAmplifier());
        blendedPotionMeta.addCustomEffect(potionEffect, false);
    }

    private @NotNull PotionEffect getBlendedEffect(PotionData potionData) {
        return potionData.getType().getEffectType().createEffect(getNewDuration(potionData), getAmplifier(potionData));
    }

    private int getAmplifier(PotionData potionData) {
        int amplifier = 0;
        if (potionData.isUpgraded()) {
            String typeName = potionData.getType().toString();
            FileConfiguration config = getConfiguration();
            amplifier = config.getInt("potions." + typeName + ".max_amplifier");
        }
        return amplifier;
    }

    @NotNull
    private FileConfiguration getConfiguration() {
        return unitedSkills.getConfig();
    }

    private Component getBlendedName() {
        PotionMeta potionMeta = getPotionMeta(potion);
        PotionMeta otherMeta = getPotionMeta(otherPotion);
        return Component.text("Potion of ")
                .append(Component.text(getFormattedEffect(potionMeta)))
                .append(Component.text(" & "))
                .append(Component.text(getFormattedEffect(otherMeta)))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component getDoubleBlendedName() {
        TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
                .match(" & ")
                .replacement(", ")
                .once()
                .build();
        return getBlendedName().replaceText(replacementConfig);
    }

    @NotNull
    private String getFormattedEffect(PotionMeta potionMeta) {
        PotionType potionType = potionMeta.getBasePotionData().getType();
        if (potionMeta.hasCustomEffects()) {
            List<PotionEffect> effects = potionMeta.getCustomEffects();
            List<String> effectNames = new ArrayList<>();
            for (PotionEffect effect : effects) {
                String effectName = WordUtils.capitalize(effect.getType().getName().toLowerCase().replace("_", " "));
                effectNames.add(effectName);
            }
            return String.join(" & ", effectNames);
        }
        return WordUtils.capitalize(potionType.toString().toLowerCase().replace("_", " "));
    }

    private int getNewDuration(PotionData potionData) {
        String typeName = potionData.getType().toString();
        FileConfiguration config = getConfiguration();
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

    private @NotNull Color getBlendedPotionColor() {
        final double weight = 0.5;
        final Color potionColor = getPotionColor(potion);
        final Color otherPotionColor = getPotionColor(otherPotion);
        int R = (int) (weight * potionColor.getRed() + weight * otherPotionColor.getRed());
        int G = (int) (weight * potionColor.getGreen() + weight * otherPotionColor.getGreen());
        int B = (int) (weight * potionColor.getBlue() + weight * otherPotionColor.getBlue());
        return Color.fromRGB(R, G, B);
    }

    private Color getPotionColor(ItemStack potion) {
        PotionMeta potionMeta = getPotionMeta(potion);
        if (potionMeta.hasColor()) {
            return potionMeta.getColor();
        }
        return potionMeta.getBasePotionData().getType().getEffectType().getColor();
    }

    private boolean hasCustomEffects() {
        return getPotionMeta(potion).hasCustomEffects() ||  getPotionMeta(otherPotion).hasCustomEffects();
    }

    private int getExtraEffects() {
        PotionMeta potionMeta = getPotionMeta(potion);
        PotionMeta otherMeta = getPotionMeta(otherPotion);
        final boolean canDoubleBlend = player.hasPermission("united.skills.blend.2");
        if (!hasCustomEffects()) {
            if (canDoubleBlend) {
                return 3;
            }
            return 2;
        }
        int totalEffects = potionMeta.getCustomEffects().size() + otherMeta.getCustomEffects().size();
        if (canDoubleBlend && totalEffects == 2) {
            return 1;
        }
        return 0;
    }

    private PotionMeta getPotionMeta(ItemStack potion) {
        return (PotionMeta) potion.getItemMeta();
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
