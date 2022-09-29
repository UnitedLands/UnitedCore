package org.unitedlands.war;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.unitedlands.war.UnitedWars.MINI_MESSAGE;

public class Utils {
    private static final TownyAPI TOWNY_API = TownyAPI.getInstance();
    private static final FileConfiguration CONFIG = getPlugin().getConfig();

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

    public static boolean isBannedWorld(String worldName) {
        List<String> bannedWorlds = CONFIG.getStringList("banned-worlds");
        return bannedWorlds.contains(worldName);
    }

    public static void teleportPlayerToSpawn(Player player) {
        Location spawn = TOWNY_API.getResident(player).getTownOrNull().getSpawnOrNull();
        if (spawn == null) return;
        player.teleportAsync(spawn);
    }

    public static Town getPlayerTown(Player player) {
        Resident resident = getTownyResident(player);
        return resident.getTownOrNull();
    }

    public static Town getPlayerTown(UUID uuid) {
        Resident resident = getTownyResident(uuid);
        return resident.getTownOrNull();
    }

    public static Resident getTownyResident(Player player) {
        return TOWNY_API.getResident(player);
    }

    public static Resident getTownyResident(UUID uuid) {
        return TOWNY_API.getResident(uuid);
    }
}
