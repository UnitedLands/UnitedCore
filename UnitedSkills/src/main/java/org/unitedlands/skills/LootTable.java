package org.unitedlands.skills;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.skill.Skill;

import java.util.*;

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
     * @return Returns a random item from the loot table if the chance is successful, null if otherwise
     */
    public ItemStack getRandomItem() {
        ConfigurationSection lootSection = unitedSkills.getConfig().getConfigurationSection(name);
        assert lootSection != null;
        Set<String> itemIDS = lootSection.getKeys(false);
        for (String itemID : itemIDS) {
            ConfigurationSection itemSection = lootSection.getConfigurationSection(itemID);
            assert itemSection != null;
            if (isSuccessful(itemSection)) {
                return generateItem(itemSection);
            }
        }
        return null;
    }

    /**
     * @param biome The biome to check
     * @return a random item from a biome-based loot table.
     */
    public ItemStack getRandomItem(Biome biome) {
        ConfigurationSection lootSection = unitedSkills.getConfig().getConfigurationSection(name);
        assert lootSection != null;
        Set<String> itemIDS = lootSection.getKeys(false);
        for (String itemID : itemIDS) {
            ConfigurationSection itemSection = lootSection.getConfigurationSection(itemID);
            assert itemSection != null;
            @NotNull List<String> itemBiomes = itemSection.getStringList("biomes");
            if (!itemBiomes.contains(biome.name())) {
                continue;
            }
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
        assert lootSection != null;
        Set<String> itemIDS = lootSection.getKeys(false);
        for (String itemID : itemIDS) {
            ConfigurationSection itemSection = lootSection.getConfigurationSection(itemID);
            assert itemSection != null;
            if (!itemSection.getStringList("blocks").contains(block.getType().toString())) {
                continue;
            }
            if (isSuccessful(itemSection)) {
                return generateItem(itemSection);
            }
        }
        return null;
    }

    private boolean isSuccessful(ConfigurationSection itemSection) {
        if (itemSection.getInt("required-level") <= requiredSkill.getLevel()) {
            double randomPercentage = Math.random() * 100;
            double finalDropChance = getFinalDropChance(requiredSkill, itemSection);
            return randomPercentage < finalDropChance;
        }
        return false;
    }

    private ItemStack generateItem(ConfigurationSection itemSection) {
        int amount = getAmount(itemSection);

        Material itemMaterial = Material.getMaterial(Objects.requireNonNull(itemSection.getString("material")));
        if (!itemSection.getStringList("materials").isEmpty()) {
            itemMaterial = getRandomMaterial(itemSection);
        }
        if (itemMaterial == null) {
            return null;
        }
        ItemStack item = new ItemStack(itemMaterial, amount);

        addName(itemSection, item);
        addModelData(itemSection, item);
        addLore(itemSection, item);
        addDamage(itemSection, item);

        return item;
    }

    private Material getRandomMaterial(ConfigurationSection itemSection) {
        List<String> materialNames = itemSection.getStringList("materials");
        Random rand = new Random();
        String randomMaterialName = materialNames.get(rand.nextInt(materialNames.size()));
        return Material.valueOf(randomMaterialName);
    }

    private void addDamage(ConfigurationSection itemSection, ItemStack item) {
        boolean canDamage = itemSection.getBoolean("random-damage");
        if (canDamage) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageableItemMeta) {
                int damageAmount = getRandomAmount(50, item.getType().getMaxDurability() - 50);
                damageableItemMeta.setDamage(damageAmount);
                item.setItemMeta(damageableItemMeta);
            }
        }
    }

    private void addLore(ConfigurationSection itemSection, ItemStack item) {
        List<String> lore = itemSection.getStringList("lore");
        if (lore.isEmpty()) {
            return;
        }
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

    private int getAmount(ConfigurationSection itemSection) {
        int minAmount = itemSection.getInt("min");
        int maxAmount = itemSection.getInt("max");
        if (minAmount == maxAmount) {
            return minAmount;
        }
        return getRandomAmount(minAmount, maxAmount);
    }

    private int getRandomAmount(int minAmount, int maxAmount) {
        return minAmount + (int) (Math.random() * ((maxAmount - minAmount) + 1));
    }

    private double getFinalDropChance(Skill requiredSkill, ConfigurationSection itemSection) {
        double baseChance = itemSection.getDouble("chance");
        double chanceModifier = itemSection.getDouble("level-modifier") * requiredSkill.getLevel();
        return (baseChance + chanceModifier) * 100;
    }

}
