package org.unitedlands.wars.hooks;

import com.palmergames.bukkit.towny.object.Resident;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;

public class Placeholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "unitedwars";
    }

    @Override
    public @NotNull String getAuthor() {
        return "maroon28";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equals("lives")) {
            // Don't bother if there are no wars on.
            if (WarDatabase.getWars().isEmpty())
                return "";

            // Empty string if player doesn't have a war.
            if (!WarDatabase.hasWar(player.getPlayer()))
                return "";

            Resident resident = Utils.getTownyResident(player.getUniqueId());
            String colorChar;
            int lives = WarDataController.getResidentLives(resident);
            colorChar = getColorChar(lives);
            return colorChar + "§l" + lives + "§r" + colorChar + " ❤";
        }
        return null; // Not recognized.
    }

    @NotNull
    private String getColorChar(int lives) {
        String colorChar;
        if (lives >= 3) {
            colorChar = "§a";
        } else if (lives == 2) {
            colorChar = "§6";
        } else if (lives == 1){
            colorChar = "§c";
        } else {
            colorChar = "§7"; // 0 lives, gray.
        }
        return colorChar;
    }
}
