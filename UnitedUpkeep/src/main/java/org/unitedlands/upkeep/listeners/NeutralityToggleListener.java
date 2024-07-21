package org.unitedlands.upkeep.listeners;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.upkeep.util.TerritorialMetaController;

public class NeutralityToggleListener implements Listener {

    @EventHandler
    public void neutralityToggled(TownToggleNeutralEvent event) {
        Town town = event.getTown();
        if(TerritorialMetaController.toggledTerritorialWars(town)) {
            TownyMessaging.sendErrorMsg(event.getSender(),"You cannot toggle neutrality while in territorial war mode.");
            event.setCancelled(true);
        }

    }
}
