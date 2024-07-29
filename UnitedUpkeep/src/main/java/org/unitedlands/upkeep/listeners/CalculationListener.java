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
import org.unitedlands.upkeep.util.TerritorialMetaController;

public class CalculationListener implements Listener {
    private final UnitedUpkeep unitedUpkeep;

    public CalculationListener(UnitedUpkeep unitedUpkeep) {
        this.unitedUpkeep = unitedUpkeep;
    }

    @EventHandler
    public void calculateTownUpkeepEvent(TownUpkeepCalculationEvent event) {
        Town town = event.getTown();

        TownUpkeepCalculator calculator = new TownUpkeepCalculator(this.unitedUpkeep, town);
        double bonusDiscount = calculator.calculateBonusBlockDiscount();
        double upkeep = bonusDiscount;
        System.out.println("upkeep currently before is " + event.getUpkeep());
        System.out.println("total amount of plots/blocks" + event.getTown().getTownBlocks().size());
        System.out.println("total amount of plots minus bonus" + (event.getTown().getTownBlocks().size() - event.getTown().getBonusBlocks()));
        System.out.println("amount of bonus blocks before :" + event.getTown().getBonusBlocks());
        System.out.println("bonus discount: " + bonusDiscount);
        if (upkeep <= 0.0) {
            upkeep = 0.0;
        }

        if (town.isNeutral()) {
            int defaultFee = 25;
            upkeep += Math.floor(upkeep * 0.1 + (double)defaultFee);
        }


        event.setUpkeep(upkeep);

        System.out.println("upkeep currently is now " + event.getUpkeep());
        System.out.println("amount of bonus blocks after :" + event.getTown().getBonusBlocks());
        System.out.println("bonus blocks cost: " + event.getTown().getBonusBlocks());
    }

    @EventHandler
    public void calculateNationUpkeepEvent(NationUpkeepCalculationEvent event) {
        Nation nation = event.getNation();
        NationUpkeepCalculator nationUpkeepCalculator = new NationUpkeepCalculator(this.unitedUpkeep, nation);
        double upkeep = nationUpkeepCalculator.calculateNationUpkeep();
        event.setUpkeep(upkeep);
    }
}
