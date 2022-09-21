package org.unitedlands.war;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static org.unitedlands.war.UnitedWars.MINI_MESSAGE;

public class Utils {
    @NotNull
    public static Component getMessage(String message) {
        FileConfiguration config = getPlugin().getConfig();
        String prefix = config.getString("messages.prefix");
        String configuredMessage = prefix + config.getString("messages." + message);
        return MINI_MESSAGE.deserialize(Objects.requireNonNullElseGet(configuredMessage, () -> "<red>Message <yellow>" + message + "<red> could not be found in the config file!"));
    }

    public static UnitedWars getPlugin() {
        return (UnitedWars) Bukkit.getServer().getPluginManager().getPlugin("UnitedWars");
    }
}
