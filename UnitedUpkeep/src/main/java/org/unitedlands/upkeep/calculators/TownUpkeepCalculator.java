package org.unitedlands.upkeep.calculators;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.configuration.file.FileConfiguration;
import org.unitedlands.upkeep.UnitedUpkeep;

import static org.unitedlands.upkeep.util.NationMetaController.isOfficialNation;

public class TownUpkeepCalculator {

    private final UnitedUpkeep unitedUpkeep;
    private final Town town;

    public TownUpkeepCalculator(UnitedUpkeep unitedUpkeep, Town town) {
        this.unitedUpkeep = unitedUpkeep;
        this.town = town;
    }

    public double calculateNationDiscountedTownUpkeep() {
        double upkeepPerPlot = (getBaseTownUpkeepPrice() * getRiseMod()) / (getFallMod() + calculateNationDiscount());
        double upkeep = Math.floor((upkeepPerPlot * getTownPlotCount()));
        return addOfficialNationDiscountOrNone(upkeep);

    }

    public double calculateTownUpkeep() {
        double upkeepPerPlot = (getBaseTownUpkeepPrice() * getRiseMod()) / getFallMod();
        double upkeep = Math.floor((upkeepPerPlot * getTownPlotCount()));
        return addOfficialNationDiscountOrNone(upkeep);
    }


    private double addOfficialNationDiscountOrNone(double upkeep) {
        if (town.hasNation()) {
            if (isOfficialNation(town.getNationOrNull()))
                return Math.floor(upkeep * 0.05);
        }
        return upkeep;
    }

    public double calculateBonusBlockDiscount() {
        int totalBlocks = getTownPlotCount();
        int bonusBlocks = town.getBonusBlocks();
        double upkeepPerPlot = Math.floor(calculateTownUpkeep() / totalBlocks);

        if (bonusBlocks == 0) {
            return 0;
        }

        if (bonusBlocks >= totalBlocks) {
            return calculateNationDiscountedTownUpkeep();
        }

        return bonusBlocks * upkeepPerPlot;

    }

    private double getFallMod() {
        int townResidentCount = town.getNumResidents();

        double fallStep = getConfiguration().getDouble("town.fallStep");
        int fallAt = getConfiguration().getInt("town.fallAt");

        int townResidentStep = townResidentCount / fallAt;

        return (fallStep * townResidentStep) + 1;
    }

    private int getTownPlotCount() {
        return town.getTownBlocks().size();
    }

    private double getRiseMod() {
        int townPlotCount = getTownPlotCount();

        int riseAt = getConfiguration().getInt("town.riseAt");
        double riseStep = getConfiguration().getDouble("town.riseStep");

        int townPlotStep = townPlotCount / riseAt;

        return (riseStep * townPlotStep) + 1;
    }

    private double calculateNationDiscount() {
        if (!town.hasNation()) {
            return 0;
        }
        Nation nation = town.getNationOrNull();
        if (nation.getNumTowns() == 1) {
            return 0;
        }

        double claimContributionPercent = (double) getTownPlotCount() / nation.getTownBlocks().size();
        double residentContributionPercent = (double) town.getNumResidents() / nation.getNumResidents();

        return residentContributionPercent + claimContributionPercent;
    }

    private int getBaseTownUpkeepPrice() {
        return getConfiguration().getInt("town.baseUpkeepPrice");
    }

    private FileConfiguration getConfiguration() {
        return unitedUpkeep.getConfig();
    }

    public double getNationDiscount() {
        return calculateTownUpkeep() - calculateNationDiscountedTownUpkeep();
    }

    public double getDiscountedUpkeep() {
        return Math.abs(calculateNationDiscountedTownUpkeep() - calculateBonusBlockDiscount());
    }

}
