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
        double upkeep = UpkeepCalculators.calculateTownUpkeep(config, town, true);
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

        double upkeep = UpkeepCalculators.calculateTownUpkeep(config, town, false);
        double discountedUpkeep = UpkeepCalculators.calculateTownUpkeep(config, town, true);

        double discount = Math.floor(upkeep - discountedUpkeep);


        // Loop through the components in the status screen
        screenComponents.forEach(component -> {
            var componentContent = component.content();


            if (componentContent.contains("upkeep")) {
                // checking if there's a discount to begin with
                if (UpkeepCalculators.calculateNationDiscount(town) > 0) {
                    // Define our custom component, and replace the default one with it.
                    Component upkeepComponent = Component.text("§2Daily Upkeep: §c" + discountedUpkeep + " Gold" + " §b[Nation Discount: §6" + discount + " Gold§b]");
                    screen.replaceComponent("upkeep", (TextComponent) upkeepComponent);
                }
            }

            // Check if the component is the town size
            if (componentContent.contains("Town Size")) {

                // Replace towny's default component with our new one, without the slashes in it
                Component newComponent = Component.text("\n§2Town Size: §a" + townsize);
                screen.replaceComponent("townblocks", (TextComponent) newComponent);
            }
        });

    }


}
