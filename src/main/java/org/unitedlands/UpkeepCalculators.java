package org.unitedlands;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.configuration.file.FileConfiguration;

public class UpkeepCalculators {
    public static double calculateTownUpkeep(FileConfiguration config, Town town, boolean nationDiscount) {

        int baseUpkeepPrice = config.getInt("town.baseUpkeepPrice");
        int townPlotCount = town.getTownBlocks().size();
        int townResidentCount = town.getNumResidents();

        double riseStep = config.getDouble("town.riseStep");
        int riseAt = config.getInt("town.riseAt");

        double fallStep = config.getDouble("town.fallStep");
        int fallAt = config.getInt("town.fallAt");

        int townPlotStep = townPlotCount / riseAt;
        int townResidentStep = townResidentCount / fallAt;


        double riseMod = (riseStep * townPlotStep) + 1;
        double fallMod = (fallStep * townResidentStep) + 1;

        double upkeepPerPlot;

        if (nationDiscount) {
            upkeepPerPlot = (baseUpkeepPrice * riseMod) / (fallMod + calculateNationDiscount(town));

        } else {
            upkeepPerPlot = (baseUpkeepPrice * riseMod) / fallMod;
        }

        double upkeep = Math.floor((upkeepPerPlot * townPlotCount));

        return upkeep;

    }


    public static double calculateNationUpkeep(FileConfiguration config, Nation nation) {

        int baseUpkeepPrice = config.getInt("nation.baseUpkeepPrice");
        int nationPlotCount = nation.getTownBlocks().size();
        int nationResidentCount = nation.getNumResidents();
        int townCount = nation.getNumTowns();

        double townCountMod = (float) 1 / townCount;

        double riseStep = config.getDouble("nation.riseStep");
        int riseAt = config.getInt("nation.riseAt");

        double fallStep = config.getDouble("nation.fallStep");
        int fallAt = config.getInt("nation.fallAt");

        int nationPlotStep = nationPlotCount / riseAt;
        int nationResidentStep = nationResidentCount / fallAt;


        double riseMod = (riseStep * nationPlotStep) + 1;
        double fallMod = (fallStep * nationResidentStep) + 1;

        double upkeepPerPlot = Math.floor(((baseUpkeepPrice * riseMod) / fallMod) * townCountMod);
        double upkeep = Math.floor(upkeepPerPlot * nationPlotCount);

        return upkeep;
    }

    private static double calculateNationDiscount(Town town) {

        if (!town.hasNation()) {
            return 0;
        }

        Nation nation = town.getNationOrNull();

        if (nation.getNumTowns() == 1) {
            return 0;
        }

        double claimContributionPercent = (double) town.getTownBlocks().size() / nation.getTownBlocks().size();
        double residentContributionPercent = (double) town.getNumResidents() / nation.getNumResidents();

        double nationDiscountModifier = residentContributionPercent + claimContributionPercent;

        return nationDiscountModifier;
    }

    public static double calculateBonusDiscount(FileConfiguration config, Town town) {

        int totalBlocks = town.getTownBlocks().size();
        int bonusBlocks = town.getBonusBlocks();
        double upkeepPerPlot = Math.floor(calculateTownUpkeep(config, town, false) / totalBlocks);

        if (bonusBlocks == 0) {
            return 0;
        }
        return bonusBlocks * upkeepPerPlot;

    }

}
