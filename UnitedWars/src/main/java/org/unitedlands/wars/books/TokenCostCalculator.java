package org.unitedlands.wars.books;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.wars.war.WarType;

import java.util.List;

public class TokenCostCalculator {
    private final List<Resident> residents;
    private final int size;
    private final WarType type;

    public TokenCostCalculator(Town town) {
        this.residents = town.getResidents();
        this.size = town.getNumTownBlocks();
        this.type = WarType.TOWNWAR;
    }

    public TokenCostCalculator(Nation nation) {
        this.residents = nation.getResidents();
        this.size = nation.getNumTownblocks();
        this.type = WarType.TOWNWAR;
    }

    public int calculateTokenIncome() {
        int baseIncome = 2; // per day

        // Increase token income by 25 percent
        final double riseStep = 0.25;
        // Every 100 claimed chunks
        final double riseAt = 100;

        // Increase by 25 percent
        final double bonusStep = 0.25;
        // Every 8 hostility points in a town
        final double bonusAt = 8;

        double targetSizeStep = Math.floor(size / riseAt);
        double targetHostilityStep = Math.floor(getTotalTargetHostility() / bonusAt);

        double riseMod = (riseStep * targetSizeStep) + 1;
        double bonusMod = (bonusStep * targetHostilityStep) + 1;


        int calculatedIncome = (int) Math.floor((baseIncome * riseMod + bonusMod));
        return Math.min(10, calculatedIncome);
    }

    public int calculateWarCost() {
        int baseCost = type.getBaseCost();

        // Increase token cost by 25 percent
        final double riseStep = 0.25;
        // Rise every 100 claimed chunks
        final double riseAt = 100;

        // Decrease token cost by 15 percent
        final double fallStep = 0.15;
        // Decrease every 8 hostility points in a town
        final double fallAt = 8;

        double targetSizeStep = Math.floor(size / riseAt);
        double targetHostilityStep = Math.floor(getTotalTargetHostility() / fallAt);

        double riseMod = (riseStep * targetSizeStep) + 1;
        double fallMod = (fallStep * targetHostilityStep) + 1;

        int calculatedCost = (int) Math.floor((baseCost * riseMod) / fallMod);
        return Math.max(type.getBaseCost(), calculatedCost);
    }

    private int getTotalTargetHostility() {
        int totalHostility = 0;

        for (Resident resident : residents) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(resident.getUUID());
            PvpPlayer pvpPlayer = new PvpPlayer(offlinePlayer);

            if (!pvpPlayer.getPlayerFile().exists())
                continue;
            int hostility = pvpPlayer.getHostility();
            if (hostility == 1)
                continue;

            totalHostility += hostility;
        }

        return totalHostility;
    }

}
