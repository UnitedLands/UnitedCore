package org.unitedlands.war.books;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.war.books.warbooks.WarBook;

import java.util.ArrayList;
import java.util.List;

import static io.github.townyadvanced.eventwar.settings.EventWarSettings.*;

public class TokenCostCalculator {
    private final WarBook book;
    private final Town targetTown;
    private final Nation targetNation;

    public TokenCostCalculator(WarBook book) {
        this.book = book;
        targetTown = book.getWarTarget().town();
        targetNation = targetTown.getNationOrNull();
    }


    public TokenCostCalculator(Town town) {
        this.book = null;
        targetTown = town;
        targetNation = town.getNationOrNull();
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

        double targetSizeStep = Math.floor(getTotalTargetSize() / riseAt);
        double targetHostilityStep = Math.floor(getTotalTargetHostility() / bonusAt);

        double riseMod = (riseStep * targetSizeStep) + 1;
        double bonusMod = (bonusStep * targetHostilityStep) + 1;


        int calculatedIncome = (int) Math.floor((baseIncome * riseMod + bonusMod));
        return Math.min(5, calculatedIncome);
    }

    public int calculateWarCost() {
        int baseCost = getBaseCost();

        // Increase token cost by 10 percent
        final double riseStep = 0.10;
        // Rise every 100 claimed chunks
        final double riseAt = 100;

        // Decrease token cost by 25 percent
        final double fallStep = 0.25;
        // Decrease every 8 hostility points in a town
        final double fallAt = 8;

        double targetSizeStep = Math.floor(getTotalTargetSize() / riseAt);
        double targetHostilityStep = Math.floor(getTotalTargetHostility() / fallAt);

        double riseMod = (riseStep * targetSizeStep) + 1;
        double fallMod = (fallStep * targetHostilityStep) + 1;


        int calculatedCost = (int) Math.floor((baseCost * riseMod) / fallMod);
        return Math.max(10, calculatedCost);
    }

    private int getBaseCost() {
        return switch (book.getType().name()) {
            case "NATIONWAR" -> nationWarTokenCost();
            case "CIVILWAR" -> civilWarTokenCost();
            case "WORLDWAR" -> worldWarTokenCost();
            default -> townWarTokenCost(); // default to town.
        };
    }

    private int getTotalTargetHostility() {
        List<Resident> residents = getTargetResidents();
        int totalHostility = 0;

        for (Resident resident : residents) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(resident.getUUID());
            PvpPlayer pvpPlayer = new PvpPlayer(offlinePlayer);

            int hostility = pvpPlayer.getHostility();
            if (hostility == 1) continue;

            totalHostility += hostility;
        }

        return totalHostility;
    }

    private List<Resident> getTargetResidents() {
        List<Resident> residents = new ArrayList<>();
        if (book.getType().isTownWar()) {
            residents = targetTown.getResidents();
        } else if (book.getType().isNationWar()) {
            residents = targetNation.getResidents();
        }
        return residents;
    }

    private int getTotalTargetSize() {
        if (book.getType().name().equals("NATIONWAR")) {
            return targetNation.getNumTownblocks();
        }
        return targetTown.getNumTownBlocks();
    }
}
