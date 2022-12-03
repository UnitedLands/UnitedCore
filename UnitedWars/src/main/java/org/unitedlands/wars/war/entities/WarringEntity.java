package org.unitedlands.wars.war.entities;

import com.palmergames.bukkit.towny.object.Resident;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.health.WarHealth;

import java.util.List;
import java.util.UUID;

public interface WarringEntity {
    War getWar();

    UUID getUUID();

    WarHealth getWarHealth();

    List<Resident> getWarParticipants();

    String getPath();

    String name();
}
