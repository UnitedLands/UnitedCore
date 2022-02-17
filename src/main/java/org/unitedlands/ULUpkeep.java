package org.unitedlands;

import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.TextComponent;
import com.palmergames.bukkit.towny.event.NationUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ULUpkeep extends JavaPlugin implements Listener {

    FileConfiguration config = getConfig();


    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void calculateTownUpkeepEvent(TownUpkeepCalculationEvent event) {
        Town town = event.getTown();
        double bonusDiscount = UpkeepCalculators.calculateBonusDiscount(config, town);
        double upkeep = UpkeepCalculators.calculateTownUpkeep(config, town, true) - bonusDiscount;
        // set the upkeep to 0 if it's negative. This is in case a town has more/equal bonus blocks than/to plots.
        if (upkeep <= 0) {
            upkeep = 0;
        }
        event.setUpkeep(upkeep);
    }

    @EventHandler
    public void calculateNationUpkeepEvent(NationUpkeepCalculationEvent event) {
        Nation nation = event.getNation();
        double upkeep = UpkeepCalculators.calculateNationUpkeep(config, nation);
        event.setUpkeep(upkeep);
    }

    @EventHandler
    public void sendStatusScreen(TownStatusScreenEvent event) {
        var screen = event.getStatusScreen();
        var screenComponents = screen.getComponents();
        Town town = event.getTown();
        int townsize = town.getTownBlocks().size();

        double undiscountedUpkeep = UpkeepCalculators.calculateTownUpkeep(config, town, false);
        double upkeepWithNationDiscount = UpkeepCalculators.calculateTownUpkeep(config, town, true);

        double nationDiscount = undiscountedUpkeep - upkeepWithNationDiscount;
        double bonusDiscount = UpkeepCalculators.calculateBonusDiscount(config, town);

        double discountedUpkeep = Math.abs(upkeepWithNationDiscount - bonusDiscount);

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
                if (nationDiscount > 0 && bonusDiscount != 0) {
                    upkeepComponent = Component.text("§2Upkeep: §7§m" + undiscountedUpkeep + "§r§c " +
                            discountedUpkeep + " Gold" + " §2[Bonus Discount: §a" + bonusDiscount + " Gold§2]" +
                            " [Nation Discount: §a" + nationDiscount + " Gold§2]");
                    screen.replaceComponent("upkeep", (TextComponent) upkeepComponent);
                } else if (nationDiscount > 0) {
                    // Define our custom component, and replace the default one with it.
                    upkeepComponent = Component.text("§2Upkeep: §7§m" + undiscountedUpkeep + "§r§c " + discountedUpkeep +
                            " Gold" + " §2[Nation Discount: §a" + nationDiscount + " Gold§2]");
                    screen.replaceComponent("upkeep", (TextComponent) upkeepComponent);
                } else if (bonusDiscount != 0) {
                    upkeepComponent = Component.text("§2Upkeep: §7§m" + undiscountedUpkeep + "§r§c " + discountedUpkeep +
                            " Gold" + " §2[Bonus Discount: §a" + bonusDiscount + " Gold§2]");
                    screen.replaceComponent("upkeep", (TextComponent) upkeepComponent);
                }
            }
        });

    }


}
