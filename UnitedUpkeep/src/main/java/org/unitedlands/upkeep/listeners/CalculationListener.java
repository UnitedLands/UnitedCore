package org.unitedlands.upkeep.listeners;

import com.palmergames.bukkit.towny.event.NationUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.upkeep.UnitedUpkeep;
import org.unitedlands.upkeep.calculators.NationUpkeepCalculator;
import org.unitedlands.upkeep.calculators.TownUpkeepCalculator;

public class CalculationListener implements Listener {
    private final UnitedUpkeep unitedUpkeep;

    public CalculationListener(UnitedUpkeep unitedUpkeep) {
        this.unitedUpkeep = unitedUpkeep;

    }

    @EventHandler
    public void calculateTownUpkeepEvent(TownUpkeepCalculationEvent event) {
        Town town = event.getTown();
        final TownUpkeepCalculator calculator = new TownUpkeepCalculator(unitedUpkeep, town);

        double bonusDiscount = calculator.calculateBonusBlockDiscount();
        double upkeep = calculator.calculateNationDiscountedTownUpkeep() - bonusDiscount;
        if (upkeep <= 0) {
            upkeep = 0;
        }
        if (town.isNeutral()) {
            // Neutrality fees should be 10% of the upkeep.
            int defaultFee = 25;
            upkeep += upkeep * 0.1 - defaultFee;
        }
        event.setUpkeep(upkeep);
    }

    @EventHandler
    public void calculateNationUpkeepEvent(NationUpkeepCalculationEvent event) {
        Nation nation = event.getNation();
        NationUpkeepCalculator nationUpkeepCalculator = new NationUpkeepCalculator(unitedUpkeep, nation);
        double upkeep = nationUpkeepCalculator.calculateNationUpkeep();
        event.setUpkeep(upkeep);
    }
}
