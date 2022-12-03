package org.unitedlands.wars.war.entities;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.health.WarHealth;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class WarringTown implements WarringEntity {
    private final UUID townUUID;
    private final WarHealth warHealth;
    private final List<Resident> warringResidents;
    private final UUID warUUID;

    public WarringTown(Town town, WarHealth warHealth, List<Resident> warringResidents, War war) {
        this.townUUID = town.getUUID();
        this.warHealth = warHealth;
        this.warringResidents = warringResidents;
        this.warUUID = war.getUuid();
        if (townUUID != null && warHealth != null && warUUID != null) {
            WarDatabase.addWarringTown(this);
        }
    }


    public War getWar() {
        return WarDatabase.getWar(warUUID);
    }

    public WarHealth getWarHealth() {
        return warHealth;
    }

    @Override
    public List<Resident> getWarParticipants() {
        return warringResidents;
    }

    public UUID getUUID() {
        return townUUID;
    }

    public String getPath() {
        return "warring-towns";
    }

    @Override
    public String name() {
        return getTown().getFormattedName();
    }

    public Town getTown() {
        return UnitedWars.TOWNY_API.getTown(townUUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarringTown that = (WarringTown) o;

        return Objects.equals(townUUID, that.townUUID);
    }

    @Override
    public int hashCode() {
        return townUUID != null ? townUUID.hashCode() : 0;
    }
}
