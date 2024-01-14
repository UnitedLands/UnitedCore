package org.unitedlands.upkeep.calculators;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.configuration.file.FileConfiguration;
import org.unitedlands.upkeep.UnitedUpkeep;
import org.unitedlands.upkeep.util.NationMetaController;

public class TownUpkeepCalculator {
    private final UnitedUpkeep unitedUpkeep;
    private final Town town;

    public TownUpkeepCalculator(UnitedUpkeep unitedUpkeep, Town town) {
        this.unitedUpkeep = unitedUpkeep;
        this.town = town;
    }

    public double calculateNationDiscountedTownUpkeep() {
        double upkeepPerPlot = (double)this.getBaseTownUpkeepPrice() * this.getRiseMod() / (this.getFallMod() + this.calculateNationDiscount());
        double upkeep = Math.floor(upkeepPerPlot * (double)this.getTownPlotCount());
        return this.addOfficialNationDiscountOrNone(upkeep);
    }

    public double calculateTownUpkeep() {
        double upkeepPerPlot = (double)this.getBaseTownUpkeepPrice() * this.getRiseMod() / this.getFallMod();
        double upkeep = Math.floor(upkeepPerPlot * (double)this.getTownPlotCount());
        return this.addOfficialNationDiscountOrNone(upkeep);
    }

    private double addOfficialNationDiscountOrNone(double upkeep) {
        return this.town.hasNation() && NationMetaController.isOfficialNation(this.town.getNationOrNull()) ? Math.floor(upkeep * 0.05) : upkeep;
    }

    public double calculateBonusBlockDiscount() {
        int totalBlocks = this.getTownPlotCount();
        int bonusBlocks = this.town.getBonusBlocks();
        double upkeepPerPlot = Math.floor(this.calculateTownUpkeep() / (double)totalBlocks);
        if (bonusBlocks == 0) {
            return 0.0;
        } else {
            return bonusBlocks >= totalBlocks ? this.calculateNationDiscountedTownUpkeep() : (double)bonusBlocks * upkeepPerPlot;
        }
    }

    private double getFallMod() {
        int townResidentCount = this.town.getNumResidents();
        double fallStep = this.getConfiguration().getDouble("town.fallStep");
        int fallAt = this.getConfiguration().getInt("town.fallAt");
        int townResidentStep = townResidentCount / fallAt;
        return fallStep * (double)townResidentStep + 1.0;
    }

    private int getTownPlotCount() {
        return this.town.getTownBlocks().size();
    }

    private double getRiseMod() {
        int townPlotCount = this.getTownPlotCount();
        int riseAt = this.getConfiguration().getInt("town.riseAt");
        double riseStep = this.getConfiguration().getDouble("town.riseStep");
        int townPlotStep = townPlotCount / riseAt;
        return riseStep * (double)townPlotStep + 1.0;
    }

    private double calculateNationDiscount() {
        if (!this.town.hasNation()) {
            return 0.0;
        } else {
            Nation nation = this.town.getNationOrNull();
            if (nation.getNumTowns() == 1) {
                return 0.0;
            } else {
                double claimContributionPercent = (double)this.getTownPlotCount() / (double)nation.getTownBlocks().size();
                double residentContributionPercent = (double)this.town.getNumResidents() / (double)nation.getNumResidents();
                return residentContributionPercent + claimContributionPercent;
            }
        }
    }

    private int getBaseTownUpkeepPrice() {
        return this.getConfiguration().getInt("town.baseUpkeepPrice");
    }

    private FileConfiguration getConfiguration() {
        return this.unitedUpkeep.getConfig();
    }

    public double getNationDiscount() {
        return this.calculateTownUpkeep() - this.calculateNationDiscountedTownUpkeep();
    }

    public double getDiscountedUpkeep() {
        return Math.abs(this.calculateNationDiscountedTownUpkeep() - this.calculateBonusBlockDiscount());
    }
}
