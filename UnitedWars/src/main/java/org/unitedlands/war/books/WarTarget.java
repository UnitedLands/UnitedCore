package org.unitedlands.war.books;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.unitedlands.war.Utils;

public class WarTarget {
    private final OfflinePlayer targetMayor;
    private final Town town;
    private final Nation nation;

    public WarTarget(OfflinePlayer targetMayor) {
        this.targetMayor = targetMayor;
        town = Utils.getPlayerTown(targetMayor.getUniqueId());
        if (town.hasNation())
            nation = town.getNationOrNull();
        else
            nation = null;
    }

    public WarTarget(Town town) {
        this.town = town;
        targetMayor = Bukkit.getOfflinePlayer(town.getMayor().getUUID());
        if (town.hasNation())
            nation = town.getNationOrNull();
        else
            nation = null;
    }

    public WarTarget(Nation nation) {
        this.nation = nation;
        this.town = nation.getCapital();
        targetMayor = town.getMayor().getPlayer();
    }

    public OfflinePlayer getTargetMayor() {
        return targetMayor;
    }

    public Town getTown() {
        return town;
    }

    public Nation getNation() {
        return nation;
    }
}
