package org.unitedlands.wars.books.data;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.unitedlands.wars.war.WarType;

import java.util.UUID;

public class WarTarget {
    private final OfflinePlayer targetMayor;
    private final Town town;
    private final Nation nation;
    private final WarType type;


    public WarTarget(Town town) {
        this.town = town;
        targetMayor = Bukkit.getOfflinePlayer(town.getMayor().getUUID());
        type = WarType.TOWN;
        if (town.hasNation())
            nation = town.getNationOrNull();
        else
            nation = null;
    }

    public WarTarget(Nation nation) {
        this.nation = nation;
        this.town = nation.getCapital();
        type = WarType.NATION;
        targetMayor = Bukkit.getOfflinePlayer(town.getMayor().getUUID());
    }

    public OfflinePlayer targetMayor() {
        return targetMayor;
    }

    public Town town() {
        return town;
    }

    public Nation nation() {
        return nation;
    }

    public UUID uuid() {
        if (type == WarType.NATION)
            return nation.getUUID();
        else
            return town.getUUID();
    }
}
