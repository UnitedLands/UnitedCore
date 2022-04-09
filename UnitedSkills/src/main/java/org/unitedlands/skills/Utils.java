package org.unitedlands.skills;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

public class Utils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component getMessage(String message) {
        message = getUnitedSkills().getConfig().getString("messages." + message);
        return miniMessage.deserialize(message);
    }

    private static UnitedSkills getUnitedSkills() {
        return (UnitedSkills) Bukkit.getPluginManager().getPlugin("UnitedBrands");
    }
}
