package org.unitedlands.wars;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.unitedlands.wars.UnitedWars.MINI_MESSAGE;

public class Utils {
    private static final TownyAPI TOWNY_API = TownyAPI.getInstance();
    private static final FileConfiguration CONFIG = UnitedWars.getInstance().getConfig();

    @NotNull
    public static Component getMessage(String message) {
        String prefix = CONFIG.getString("messages.prefix");
        String configuredMessage = CONFIG.getString("messages." + message);
        if (configuredMessage == null) {
            return MINI_MESSAGE.deserialize("<red>Message <yellow>" + message + "<red> could not be found in the config file!");
        }
        return MINI_MESSAGE.deserialize(prefix + configuredMessage);
    }

    public static Component getMessage(String message, TagResolver.Single... resolvers) {
        String prefix = CONFIG.getString("messages.prefix");
        String configuredMessage = CONFIG.getString("messages." + message);
        if (configuredMessage == null) {
            return MINI_MESSAGE.deserialize("<red>Message <yellow>" + message + "<red> could not be found in the config file!");
        }
        return MINI_MESSAGE.deserialize(prefix + configuredMessage, resolvers);
    }

    public static List<Component> getMessageList(String key) {
        List<String> configuredMessages = CONFIG.getStringList("messages." + key);
        List<Component> parsedMessages = new ArrayList<>();
        configuredMessages.forEach(msg -> parsedMessages.add(MINI_MESSAGE.deserialize(msg)));
        return parsedMessages;
    }

    public static String getMessageRaw(String message) {
        return CONFIG.getString("messages." + message);
    }

    public static boolean isBannedWorld(String worldName) {
        List<String> bannedWorlds = CONFIG.getStringList("banned-worlds");
        return bannedWorlds.contains(worldName);
    }

    public static void teleportPlayerToSpawn(Player player) {
        Location spawn = TOWNY_API.getResident(player).getTownOrNull().getSpawnOrNull();
        if (spawn == null) return;
        player.teleportAsync(spawn, PlayerTeleportEvent.TeleportCause.SPECTATE);
    }

    public static Town getPlayerTown(Player player) {
        Resident resident = getTownyResident(player);
        return resident.getTownOrNull();
    }

    public static Title getTitle(String main, String sub) {
        Component mainTitle = UnitedWars.MINI_MESSAGE.deserialize(main);
        Component subTitle = UnitedWars.MINI_MESSAGE.deserialize(sub);
        return Title.title(mainTitle, subTitle);
    }

    public static Town getPlayerTown(UUID uuid) {
        Resident resident = getTownyResident(uuid);
        return resident.getTownOrNull();
    }

    public static HashSet<UUID> residentToUUID(Collection<Resident> residents) {
        HashSet<UUID> uuids = new HashSet<>();
        for (Resident resident : residents) {
            uuids.add(resident.getUUID());
        }
        return uuids;
    }
    
    public static HashSet<UUID> playerToUUID(Collection<Player> players) {
        HashSet<UUID> uuids = new HashSet<>();
        for (Player p : players) {
            uuids.add(p.getUniqueId());
        }
        return uuids;
    }

    public static NamespacedKey getKey(String name) {
        return new NamespacedKey(UnitedWars.getInstance(), name);
    }

    public static Resident getTownyResident(Player player) {
        return TOWNY_API.getResident(player);
    }

    public static Resident getTownyResident(UUID uuid) {
        return TOWNY_API.getResident(uuid);
    }
}
