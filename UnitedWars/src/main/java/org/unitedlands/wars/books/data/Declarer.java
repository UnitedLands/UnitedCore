package org.unitedlands.wars.books.data;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.WarDatabase;

import java.util.UUID;

public class Declarer {
    private final UUID declaringPlayer;
    private final UUID town;
    private final UUID nation;


    public Declarer(Player declaringPlayer) {
        this.declaringPlayer = declaringPlayer.getUniqueId();
        town = Utils.getPlayerTown(declaringPlayer).getUUID();
        if (town().hasNation())
            nation = town().getNationOrNull().getUUID();
        else
            nation = null;
    }

    public Declarer(Town town) {
        this.town = town.getUUID();
        this.declaringPlayer = town().getMayor().getUUID();
        if (town.hasNation())
            nation = town().getNationOrNull().getUUID();
        else
            nation = null;
    }

    public Player player() {
        return Bukkit.getPlayer(declaringPlayer);
    }

    public Town town() {
        return UnitedWars.TOWNY_API.getTown(town);
    }

    public Nation nation() {
        return UnitedWars.TOWNY_API.getNation(nation);
    }

    public String name() {
        return WarDatabase.getWarringEntity(declaringPlayer).name();
    }

}
