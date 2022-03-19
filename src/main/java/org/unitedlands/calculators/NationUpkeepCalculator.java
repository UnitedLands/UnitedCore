package org.unitedlands.calculators;

import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.configuration.file.FileConfiguration;
import org.unitedlands.UnitedLandsUpkeep;

public class NationUpkeepCalculator {
    private final UnitedLandsUpkeep unitedLandsUpkeep;
    private final Nation nation;

    public NationUpkeepCalculator(UnitedLandsUpkeep unitedLandsUpkeep, Nation nation) {
        this.unitedLandsUpkeep = unitedLandsUpkeep;
        this.nation = nation;
    }

    public NationUpkeepCalculator getNationCalculator(Nation nation) {
        return new NationUpkeepCalculator(unitedLandsUpkeep, nation);
    }

    public double calculateNationUpkeep() {
        int townCount = nation.getNumTowns();
        double townCountMod = (float) 1 / townCount;

        double upkeepPerPlot = Math.floor(((getNationBaseUpkeep() * getRiseMod()) / getFallMod()) * townCountMod);

        return Math.floor(upkeepPerPlot * getNationPlotCount());
    }

    private double getRiseMod() {
        double riseStep = getConfiguration().getDouble("nation.riseStep");
        int riseAt = getConfiguration().getInt("nation.riseAt");

        int nationPlotStep = getNationPlotCount() / riseAt;

        return (riseStep * nationPlotStep) + 1;
    }

    private double getFallMod() {
        double fallStep = getConfiguration().getDouble("nation.fallStep");
        int fallAt = getConfiguration().getInt("nation.fallAt");

        int nationResidentStep = getNationResidentCount() / fallAt;

        return (fallStep * nationResidentStep) + 1;
    }

    private int getNationResidentCount() {
        return nation.getNumResidents();
    }

    private int getNationPlotCount() {
        return nation.getTownBlocks().size();
    }

    private int getNationBaseUpkeep() {
        return getConfiguration().getInt("nation.baseUpkeepPrice");
    }

    private FileConfiguration getConfiguration() {
        return unitedLandsUpkeep.getConfig();
    }

}
