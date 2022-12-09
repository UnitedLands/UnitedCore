package org.unitedlands.wars.war.entities;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.health.WarHealth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class WarringNation implements WarringEntity {
    private final UUID nationUUID;
    private final WarHealth warHealth;
    private final List<Resident> warringResidents;
    private final UUID warUUID;

    public WarringNation(Nation nation, WarHealth warHealth, List<Resident> warringResidents, War war) {
        this.nationUUID = nation.getUUID();
        this.warHealth = warHealth;
        this.warringResidents = warringResidents;
        this.warUUID = war.getUuid();
        WarDatabase.addWarringNation(this);
    }


    public War getWar() {
        return WarDatabase.getWar(warUUID);
    }

    public WarHealth getWarHealth() {
        return warHealth;
    }

    public List<Resident> getWarParticipants() {
        return warringResidents;
    }

    @Override
    public List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        getWarParticipants().forEach(resident -> {
            if (resident.getPlayer() != null) {
                players.add(resident.getPlayer());
            }
        });
        return players;
    }

    @Override
    public Government getGovernment() {
        return getNation();
    }

    public UUID getUUID() {
        return nationUUID;
    }

    public String getPath() {
        return "warring-nations";
    }

    @Override
    public String name() {
        return getNation().getFormattedName();
    }

    public Nation getNation() {
        return UnitedWars.TOWNY_API.getNation(nationUUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarringNation that = (WarringNation) o;

        return Objects.equals(nationUUID, that.nationUUID);
    }

    @Override
    public int hashCode() {
        return nationUUID != null ? nationUUID.hashCode() : 0;
    }
}
