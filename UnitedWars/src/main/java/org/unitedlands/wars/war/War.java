package org.unitedlands.wars.war;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;
import org.unitedlands.wars.war.entities.WarringTown;
import org.unitedlands.wars.war.health.WarHealth;

import java.util.*;

import static org.unitedlands.wars.Utils.isBannedWorld;
import static org.unitedlands.wars.Utils.teleportPlayerToSpawn;

public class War {
    private static final UnitedWars plugin = UnitedWars.getInstance();
    private final List<WarringTown> warringTowns;
    private final List<WarringNation> warringNations;
    private final HashSet<Resident> residents;
    private final WarType warType;
    private UUID uuid = UUID.randomUUID();
    private WarTimer warTimer = null;

    public War(List<Town> warringTowns, List<Nation> warringNations, HashSet<Resident> residents, WarType warType) {
        this.warringTowns = generateWarringTownList(warringTowns);
        this.warringNations = generateWarringNationList(warringNations);
        this.residents = residents;
        this.warType = warType;
        warTimer = new WarTimer(this);
        // Start the war immediately, since this is the first time.
        startWar();
        // Save war to database
        WarDatabase.addWar(this);
    }

    // Used for loading existing wars from the config.
    public War(List<Town> warringTowns, List<Nation> warringNations, HashSet<Resident> residents, WarType warType, UUID uuid) {
        this.warringTowns = generateWarringTownList(warringTowns);
        this.warringNations = generateWarringNationList(warringNations);
        this.residents = residents;
        this.warType = warType;
        this.uuid = uuid;
        // Save war to internal database
        WarDatabase.addWar(this);
    }

    public List<WarringTown> getWarringTowns() {
        return warringTowns;
    }

    public List<WarringNation> getWarringNations() {
        return warringNations;
    }

    public HashSet<Resident> getResidents() {
        return residents;
    }

    public WarType getWarType() {
        return warType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public WarTimer getWarTimer() {
        return warTimer;
    }


    public void startWar() {
        // Start the war countdowns
        warTimer.startCountdown();

        // Start player procedures
        runPlayerProcedures();

        // Make the involved entities have an active war.
        setActiveWar();
    }

    public void endWar(WarringEntity winner, WarringEntity loser) {

    }

    public void endWarTimer() {
        for (Resident resident : getResidents()) {
            Player player = resident.getPlayer();
            // Player is offline, next
            if (player == null) continue;

            // Show the health.
            WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
            warringEntity.getWarHealth().show(player);
            // Teleport them to spawn
            teleportPlayerToSpawn(player);
        }
    }

    private void runPlayerProcedures() {
        for (Resident resident : getResidents()) {
            Player player = resident.getPlayer();
            // Player is offline, next
            if (player == null) continue;

            // Teleport players to appropriate places.
            if (isBannedWorld(player.getWorld().getName()))
                teleportPlayerToSpawn(player);

            // Run start war commands
            for (String command : plugin.getConfig().getStringList("commands-on-war-start"))
                player.performCommand(command);

            // Set player lives
            WarDataController.setResidentLives(resident, 3);
        }
    }

    private void setActiveWar() {
        if (warType == WarType.TOWNWAR) {
            for (WarringTown town : warringTowns) {
                town.getTown().setActiveWar(true);
                WarDataController.setLastWarTime(town.getTown(), System.currentTimeMillis());
            }
        }

        if (warType == WarType.NATIONWAR) {
            for (WarringNation nation : warringNations) {
                nation.getNation().setActiveWar(true);
                WarDataController.setLastWarTime(nation.getNation(), System.currentTimeMillis());

                for (Nation ally : nation.getNation().getAllies()) {
                    ally.setActiveWar(true);
                }
            }
        }
    }

    public boolean hasActiveTimer() {
        if (warTimer == null)
            return false;
        else
            return warTimer.getRemainingSeconds() > 0;
    }

    private List<WarringTown> generateWarringTownList(List<Town> townList) {
        if (townList == null)
            return null;

        List<WarringTown> generatedList = new ArrayList<>();
        for (Town town : townList) {
            generatedList.add(new WarringTown(town, new WarHealth(town), town.getResidents(), this));
        }
        return generatedList;
    }

    private List<WarringNation> generateWarringNationList(List<Nation> nationList) {
        if (nationList == null)
            return null;

        List<WarringNation> generatedList = new ArrayList<>();
        for (Nation nation : nationList) {
            // Create a list with the nation residents
            List<Resident> warringResidents = new ArrayList<>(nation.getResidents());
            // add all the allies
            for (Nation ally : nation.getAllies()) {
                warringResidents.addAll(ally.getResidents());
            }
            generatedList.add(new WarringNation(nation, new WarHealth(nation), warringResidents, this));
        }
        return generatedList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        War war = (War) o;

        return Objects.equals(uuid, war.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
