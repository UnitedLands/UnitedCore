package org.unitedlands.skills;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.skills.skill.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LootTable {
    private final String name;
    private final Skill requiredSkill;
    private final UnitedSkills unitedSkills = Utils.getUnitedSkills();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LootTable(String name, Skill requiredSkill) {
        this.name = name;
        this.requiredSkill = requiredSkill;
    }

    /**
     *
     * @return Returns a random item from the loot table if the chance is successful, null if otherwise
     */
    public ItemStack getRandomItem() {
        ConfigurationSection lootSection = unitedSkills.getConfig().getConfigurationSection(name);
        Set<String> itemIDS = lootSection.getKeys(false);
        for (String itemID : itemIDS) {
            ConfigurationSection itemSection = lootSection.getConfigurationSection(itemID);
            if (isSuccessful(itemSection)) {
                return generateItem(itemSection);
            }
        }
        return null;
    }

    /**
     * @param block The block which the item should be checked against. This is used for any loot tables which drop items from blocks.
     * @return true if the item is dropped successfully
     */
    public ItemStack getRandomItem(Block block) {
        ConfigurationSection lootSection = unitedSkills.getConfig().getConfigurationSection(name);
        Set<String> itemIDS = lootSection.getKeys(false);
        for (String itemID : itemIDS) {
            ConfigurationSection itemSection = lootSection.getConfigurationSection(itemID);
            if (!itemSection.getStringList( "blocks").contains(block.getType().toString())) {
                return null;
            }
            if (isSuccessful(itemSection)) {
                return generateItem(itemSection);
            } else {
                return null;
            }
        }
        return null;
    }

    private boolean isSuccessful(ConfigurationSection itemSection) {
        double randomPercentage = Math.random() * 100;
        double finalDropChance = getFinalDropChance(requiredSkill, itemSection);
        return randomPercentage < finalDropChance;
    }

    private ItemStack generateItem(ConfigurationSection itemSection) {
        String[] amountRange = itemSection.getString( "amount-range").split("-");
        int amount = getAmount(amountRange);

        Material itemMaterial = Material.getMaterial(itemSection.getString("material"));
        ItemStack item = new ItemStack(itemMaterial, amount);

        addName(itemSection, item);
        addModelData(itemSection, item);
        addLore(itemSection, item);

        return item;
    }

    private void addLore(ConfigurationSection itemSection, ItemStack item) {
        List<String> lore = itemSection.getStringList("lore");
        List<Component> deserializedLore = new ArrayList<>();
        for (String line : lore) {
            deserializedLore.add(miniMessage.deserialize(line));
        }
        ItemMeta meta = item.getItemMeta();
        meta.lore(deserializedLore);
        item.setItemMeta(meta);
    }

    private void addModelData(ConfigurationSection itemSection, ItemStack item) {
        int customModelData = itemSection.getInt("custom-model-data");
        if (customModelData != 0) {
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
    }

    private void addName(ConfigurationSection itemSection, ItemStack item) {
        String configuredName = itemSection.getString("name");
        if (configuredName != null) {
            Component name = miniMessage.deserialize(configuredName);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(name);
            item.setItemMeta(meta);
        }
    }

    private int getAmount(String[] amountRange) {
        int minAmount = Integer.parseInt(amountRange[0]);
        int maxAmount = Integer.parseInt(amountRange[1]);
        return minAmount + (int)(Math.random() * ((maxAmount - minAmount) + 1));
    }

    private double getFinalDropChance(Skill requiredSkill, ConfigurationSection itemSection) {
        double baseChance = itemSection.getDouble( "chance");
        double chanceModifier = itemSection.getDouble( "level-modifier") * requiredSkill.getLevel();
        return (baseChance + chanceModifier) * 100;
    }

}
