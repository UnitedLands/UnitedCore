package org.unitedlands.wars.books.data;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;

import java.util.UUID;

public class WarTarget {
    private final UUID targetMayor;
    private final UUID town;
    private final UUID nation;
    private final WarType type;


    public WarTarget(Town town) {
        this.town = town.getUUID();
        targetMayor = town.getMayor().getUUID();
        type = WarType.TOWNWAR;
        if (town.hasNation())
            nation = town.getNationOrNull().getUUID();
        else
            nation = null;
    }

    public WarTarget(Nation nation) {
        this.nation = nation.getUUID();
        this.town = nation.getCapital().getUUID();
        type = WarType.NATIONWAR;
        targetMayor = town().getMayor().getUUID();
    }

    public OfflinePlayer targetMayor() {
        return Bukkit.getOfflinePlayer(targetMayor);
    }

    public Town town() {
        return UnitedWars.TOWNY_API.getTown(town);
    }

    public Nation nation() {
        return UnitedWars.TOWNY_API.getNation(nation);
    }

    public UUID uuid() {
        return type == WarType.NATIONWAR ? nation : town;
    }

    public String name() {
        return WarDatabase.getWarringEntity(targetMayor).name();
    }
}
