package org.unitedlands.wars.war.entities;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.health.WarHealth;

import java.util.*;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.getMessage;
import static org.unitedlands.wars.Utils.getTitle;

public class WarringNation implements WarringEntity {
    private final UUID nationUUID;
    private WarHealth warHealth;
    private final List<UUID> mercenaries;
    private List<UUID> joinedAllies = new ArrayList<>();
    private final HashSet<UUID> warringResidents;
    private final UUID warUUID;

    public WarringNation(Nation nation, WarHealth warHealth, List<Resident> warringResidents, List<UUID> mercenaries, War war) {
        this.nationUUID = nation.getUUID();
        this.warHealth = warHealth;
        this.warringResidents = Utils.residentToUUID(warringResidents);
        this.warUUID = war.getUuid();
        this.mercenaries = mercenaries;
        WarDatabase.addWarringNation(this);
    }


    public War getWar() {
        return WarDatabase.getWar(warUUID);
    }

    public WarHealth getWarHealth() {
        return warHealth;
    }
    @Override
    public HashSet<Resident> getWarParticipants() {
        HashSet<Resident> residents = new HashSet<>();
        warringResidents.forEach(uuid -> residents.add(Utils.getTownyResident(uuid)));
        return residents;
    }

    @Override
    public HashSet<Player> getOnlinePlayers() {
        HashSet<Player> players = new HashSet<>();
        getWarParticipants().forEach(resident -> {
            if (resident.getPlayer() != null) {
                players.add(resident.getPlayer());
            }
        });
        return players;
    }
    @Override
    public void addResident(Resident resident) {
        warringResidents.add(resident.getUUID());
    }

    @Override
    public void addMercenary(Resident resident) {
        mercenaries.add(resident.getUUID());
        warringResidents.add(resident.getUUID());
    }

    @Override
    public List<UUID> getMercenaries() {
        return mercenaries;
    }

    @Override
    public Government getGovernment() {
        return getNation();
    }
    @Override
    public Resident getLeader() {
        return getNation().getKing();
    }

    public UUID getUUID() {
        return nationUUID;
    }

    public String getPath() {
        return "warring-nations";
    }

    @Override
    public WarringEntity getEnemy() {
        for (WarringEntity warringEntity : getWar().getWarringNations()) {
            if (warringEntity.equals(this))
                continue;
            return warringEntity;
        }
        return null;
    }

    public void addAlly(Nation ally) {
        Title joinTitle = getTitle("<dark_red><bold>JOINED WAR", "<gold>You've joined the war as an ally!");
        War war = getWar();
        for (Resident resident : ally.getResidents()) {
            war.addResident(resident, getNation().getCapital());
            if (resident.getPlayer() == null)
                continue;
            resident.getPlayer().showTitle(joinTitle);
            resident.getPlayer().playSound(resident.getPlayer(), Sound.EVENT_RAID_HORN, 75, 1);
        }
        war.broadcast(getMessage("ally-joined-war",
                Placeholder.component("ally", text(ally.getName())),
                Placeholder.component("nation", text(name()))));
        ally.setActiveWar(true);
        ally.getTowns().forEach(town -> town.setActiveWar(true));
        joinedAllies.add(ally.getUUID());
    }

    public void setAllies(List<UUID> joinedAllies) {
        this.joinedAllies = joinedAllies;
    }

    public List<UUID> getJoinedAllies() {
        return joinedAllies;
    }

    @Override
    public String name() {
        return getNation().getFormattedName();
    }

    @Override
    public void setWarHealth(WarHealth health) {
        this.warHealth = health;
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
