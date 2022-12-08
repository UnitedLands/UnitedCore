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
            int amount = warringEntity.getOnlinePlayers().size();
            warringEntity.getWarHealth().increaseHealth(amount);
            // Add 3 lives for each resident, up to a max of 6.
            for (Resident resident: warringEntity.getWarParticipants()) {
                int currentLives = WarDataController.getResidentLives(resident);
                WarDataController.setResidentLives(resident,Math.max(6, currentLives + 3));
            }
        }

    }
}
