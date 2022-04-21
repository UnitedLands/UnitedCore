package org.unitedlands.skills;

import com.gamingmesh.jobs.Jobs;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Utils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @NotNull
    public static Component getMessage(String message) {
        String configMessage = getUnitedSkills().getConfig().getString("messages." + message);
        if (configMessage == null) {
            return miniMessage.deserialize("<red>Message " + message + "could not be found in the config file!");
        }
        return miniMessage.deserialize(configMessage);
    }

    private static UnitedSkills getUnitedSkills() {
        return (UnitedSkills) Bukkit.getPluginManager().getPlugin("UnitedSkills");
    }

    public static void multiplyItem(Player player, ItemStack item, int multiplier) {
        for (int i = 0; i < multiplier; i++) {
            player.getInventory().addItem(item);
        }
    }
    public static boolean takeItemFromMaterial(@NotNull Player player, @NotNull Material material) {
        int slot = player.getInventory().first(material);
        if (slot < 0) return false;

        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType().isAir()) return false;

        item.setAmount(item.getAmount() - 1);
        return true;
    }

    public static boolean takeItem(@NotNull Player player, @NotNull ItemStack item) {
        Inventory inventory = player.getInventory();

        int slot = inventory.first(item.getType());
        if (slot < 0) return false;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemInInventory = inventory.getItem(i);
            if (itemInInventory == null || itemInInventory.getType().isAir()) continue;
            if (itemInInventory.getItemMeta().equals(item.getItemMeta())) {
                itemInInventory.setAmount(itemInInventory.getAmount() - 1);
                return true;
            }
        }
        return false;
    }

    public static Jobs getJobs() {
        return (Jobs) Bukkit.getPluginManager().getPlugin("Jobs");
    }

}
