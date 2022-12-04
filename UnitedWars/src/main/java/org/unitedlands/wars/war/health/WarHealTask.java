package org.unitedlands.wars.war.health;

import org.bukkit.scheduler.BukkitRunnable;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringEntity;

public class WarHealTask extends BukkitRunnable {

    @Override
    public void run() {
        for (WarringEntity warringEntity : WarDatabase.getWarringEntities()) {
            int amount = warringEntity.getOnlinePlayers().size();
            warringEntity.getWarHealth().increaseHealth(amount);
        }

    }
}
