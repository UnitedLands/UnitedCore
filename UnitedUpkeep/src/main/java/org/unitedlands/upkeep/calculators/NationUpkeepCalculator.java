package org.unitedlands.upkeep.calculators;

import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.configuration.file.FileConfiguration;
import org.unitedlands.upkeep.UnitedUpkeep;
import org.unitedlands.upkeep.util.NationMetaController;

public class NationUpkeepCalculator {
    private final UnitedUpkeep unitedUpkeep;
    private final Nation nation;

    public NationUpkeepCalculator(UnitedUpkeep unitedUpkeep, Nation nation) {
        this.unitedUpkeep = unitedUpkeep;
        this.nation = nation;
    }

    public double calculateNationUpkeep() {
        int townCount = this.nation.getNumTowns();
        double townCountMod = (double)(1.0F / (float)townCount);
        double upkeepPerPlot = Math.floor((double)this.getNationBaseUpkeep() * this.getRiseMod() / this.getFallMod() * townCountMod);
        double upkeep = Math.floor(upkeepPerPlot * (double)this.getNationPlotCount());
        return NationMetaController.isOfficialNation(this.nation) ? Math.floor(upkeep * 0.2) : upkeep;
    }

    private double getRiseMod() {
        double riseStep = this.getConfiguration().getDouble("nation.riseStep");
        int riseAt = this.getConfiguration().getInt("nation.riseAt");
        int nationPlotStep = this.getNationPlotCount() / riseAt;
        return riseStep * (double)nationPlotStep + 1.0;
    }

    private double getFallMod() {
        double fallStep = this.getConfiguration().getDouble("nation.fallStep");
        int fallAt = this.getConfiguration().getInt("nation.fallAt");
        int nationResidentStep = this.getNationResidentCount() / fallAt;
        return fallStep * (double)nationResidentStep + 1.0;
    }

    private int getNationResidentCount() {
        return this.nation.getNumResidents();
    }

    private int getNationPlotCount() {
        return this.nation.getTownBlocks().size();
    }

    private int getNationBaseUpkeep() {
        return this.getConfiguration().getInt("nation.baseUpkeepPrice");
    }

    private FileConfiguration getConfiguration() {
        return this.unitedUpkeep.getConfig();
    }
}
