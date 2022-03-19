package org.unitedlands.listeners;

import com.palmergames.bukkit.towny.event.NationUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.UnitedLandsUpkeep;
import org.unitedlands.calculators.NationUpkeepCalculator;
import org.unitedlands.calculators.TownUpkeepCalculator;

public class CalculationListener implements Listener {
    private final UnitedLandsUpkeep unitedLandsUpkeep;

    public CalculationListener(UnitedLandsUpkeep unitedLandsUpkeep) {
        this.unitedLandsUpkeep = unitedLandsUpkeep;

    }

    @EventHandler
    public void calculateTownUpkeepEvent(TownUpkeepCalculationEvent event) {
        final TownUpkeepCalculator calculator = getTownUpkeepCalculator(event.getTown());

        double bonusDiscount = calculator.calculateBonusBlockDiscount();
        double upkeep = calculator.calculateDiscountedTownUpkeep() - bonusDiscount;
        if (upkeep <= 0) {
            upkeep = 0;
        }
        event.setUpkeep(upkeep);
    }

    @EventHandler
    public void calculateNationUpkeepEvent(NationUpkeepCalculationEvent event) {
        Nation nation = event.getNation();
        double upkeep = getNationUpkeepCalculator(nation).calculateNationUpkeep();
        event.setUpkeep(upkeep);
    }

    private TownUpkeepCalculator getTownUpkeepCalculator(Town town) {
        return new TownUpkeepCalculator(unitedLandsUpkeep, town);
    }

    private NationUpkeepCalculator getNationUpkeepCalculator(Nation nation) {
        return new NationUpkeepCalculator(unitedLandsUpkeep, nation);
    }
}
