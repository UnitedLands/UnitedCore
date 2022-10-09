package org.unitedlands.war.books;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.unitedlands.war.Utils;

public class Declarer {
    private final Player declaringPlayer;
    private final Town town;
    private final Nation nation;


    public Declarer(Player declaringPlayer) {
        this.declaringPlayer = declaringPlayer;
        town = Utils.getPlayerTown(declaringPlayer);
        if (town.hasNation())
            nation = town.getNationOrNull();
        else
            nation = null;
    }

    public Declarer(Town town) {
        this.town = town;
        this.declaringPlayer = town.getMayor().getPlayer();
        if (town.hasNation())
            nation = town.getNationOrNull();
        else
            nation = null;
    }

    public Player player() {
        return declaringPlayer;
    }

    public Town town() {
        return town;
    }

    public Nation nation() {
        return nation;
    }

}
