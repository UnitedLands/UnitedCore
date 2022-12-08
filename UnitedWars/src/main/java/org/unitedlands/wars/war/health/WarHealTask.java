package org.unitedlands.wars.war.health;

import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.scheduler.BukkitRunnable;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringEntity;

public class WarHealTask extends BukkitRunnable {

    @Override
    public void run() {
        for (WarringEntity warringEntity : WarDatabase.getWarringEntities()) {
            int amount = 0;
            for (Resident resident: warringEntity.getWarParticipants()) {
                if (!resident.isOnline())
                    return;
                if (WarDataController.hasResidentLives(resident))
                    amount++;
            }
            warringEntity.getWarHealth().increaseHealth(amount);
        }

    }
}
