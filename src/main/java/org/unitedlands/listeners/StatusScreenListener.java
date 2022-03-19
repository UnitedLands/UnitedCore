package org.unitedlands.listeners;

import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.TextComponent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.UnitedLandsUpkeep;
import org.unitedlands.calculators.TownUpkeepCalculator;

public class StatusScreenListener implements Listener {
    private final UnitedLandsUpkeep unitedLandsUpkeep;

    public StatusScreenListener(UnitedLandsUpkeep unitedLandsUpkeep) {
        this.unitedLandsUpkeep = unitedLandsUpkeep;
    }


    @EventHandler
    public void sendStatusScreen(TownStatusScreenEvent event) {
        var screen = event.getStatusScreen();
        var screenComponents = screen.getComponents();
        Town town = event.getTown();
        int townsize = town.getTownBlocks().size();
        final TownUpkeepCalculator calculator = getTownUpkeepCalculator(town);

        double undiscountedUpkeep = calculator.calculateTownUpkeep();
        double upkeepWithNationDiscount = calculator.calculateDiscountedTownUpkeep();

        double nationDiscount = undiscountedUpkeep - upkeepWithNationDiscount;
        double bonusBlockDiscount = calculator.calculateBonusBlockDiscount();

        double discountedUpkeep = Math.abs(upkeepWithNationDiscount - bonusBlockDiscount);

        // Loop through the components in the status screen
        screenComponents.forEach(component -> {
            var componentContent = component.content();
            // Check if the component is the town size
            if (componentContent.contains("Town Size")) {
                // Replace towny's default component with our new one, without the slashes in it
                Component newComponent = Component.text("\n§2Town Size: §a" + townsize);
                screen.replaceComponent("townblocks", (TextComponent) newComponent);
            }

            if (componentContent.contains("Upkeep")) {
                Component upkeepComponent;

                // Checking the least likely scenario to begin with. Bonus discount may be negative, so not the same check as nation discount
                if (nationDiscount > 0 && bonusBlockDiscount != 0) {
                    upkeepComponent = Component.text("§2Upkeep: §7§m" + undiscountedUpkeep + "§r§c " +
                            discountedUpkeep + " Gold" + " §2[Bonus Discount: §a" + bonusBlockDiscount + " Gold§2]" +
                            " [Nation Discount: §a" + nationDiscount + " Gold§2]");
                    screen.replaceComponent("upkeep", (TextComponent) upkeepComponent);
                } else if (nationDiscount > 0) {
                    // Define our custom component, and replace the default one with it.
                    upkeepComponent = Component.text("§2Upkeep: §7§m" + undiscountedUpkeep + "§r§c " + discountedUpkeep +
                            " Gold" + " §2[Nation Discount: §a" + nationDiscount + " Gold§2]");
                    screen.replaceComponent("upkeep", (TextComponent) upkeepComponent);
                } else if (bonusBlockDiscount != 0) {
                    upkeepComponent = Component.text("§2Upkeep: §7§m" + undiscountedUpkeep + "§r§c " + discountedUpkeep +
                            " Gold" + " §2[Bonus Discount: §a" + bonusBlockDiscount + " Gold§2]");
                    screen.replaceComponent("upkeep", (TextComponent) upkeepComponent);
                }
            }
        });

    }

    private TownUpkeepCalculator getTownUpkeepCalculator(Town town) {
        return new TownUpkeepCalculator(unitedLandsUpkeep, town);
    }
}
