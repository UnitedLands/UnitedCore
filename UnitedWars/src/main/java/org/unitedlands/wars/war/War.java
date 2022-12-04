package org.unitedlands.wars.war;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.events.WarEndEvent;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;
import org.unitedlands.wars.war.entities.WarringTown;
import org.unitedlands.wars.war.health.WarHealth;

import java.util.*;

import static net.kyori.adventure.text.Component.text;
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
    private WarringEntity winner;
    private WarringEntity loser;

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
        this.winner = winner;
        this.loser = loser;
        // Call event. Handle rewarding in WarListener
        WarEndEvent warEndEvent = new WarEndEvent(this, winner, loser);
        Bukkit.getServer().getPluginManager().callEvent(warEndEvent);

        // Remove health.
        hideHealth(winner);
        hideHealth(loser);

        // Notify entities
        notifyWin();
        notifyLoss();
        // Give rewards
        giveWarEarnings();

        // Clear from database.
        WarDatabase.removeWarringEntity(winner);
        WarDatabase.removeWarringEntity(loser);
        WarDatabase.removeWar(this);
    }

    // Called inside WarTimer.
    public void endWarTimer() {
        for (Resident resident : getResidents()) {
            Player player = resident.getPlayer();
            // Player is offline, next
            if (player == null) continue;
            // Remove for again for safe measures
            warTimer.removeViewer(player);

            // Show the health.
            WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
            warringEntity.getWarHealth().show(player);
            // Teleport them to spawn
            teleportPlayerToSpawn(player);
        }
    }

    private void runPlayerProcedures() {
        for (Resident resident : getResidents()) {
            // Set player lives
            WarDataController.setResidentLives(resident, 3);

            Player player = resident.getPlayer();
            // Player is offline, next
            if (player == null) continue;

            // Teleport players to appropriate places.
            if (isBannedWorld(player.getWorld().getName()))
                teleportPlayerToSpawn(player);

            // Run start war commands
            for (String command : plugin.getConfig().getStringList("commands-on-war-start"))
                player.performCommand(command);

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

    private void hideHealth(WarringEntity warringEntity) {
        warringEntity.getWarParticipants().forEach(resident -> {
            Player player = resident.getPlayer();
            if (player != null)
                warringEntity.getWarHealth().hide(player);
        });
    }

    private void notifyWin() {
        Title title = Utils.getTitle("<dark_green>VICTORY!", "<green>The war has ended!");
        winner.getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
         //   player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1, 1F, 1F);
            player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            player.sendMessage(Utils.getMessage("war-won", getWonAndLostPlaceholders()));
        });
    }

    private void notifyLoss() {
        Title title = Utils.getTitle("<dark_red>WAR LOST!", "<red>The war has ended!");
        loser.getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
         //   player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, 1F, 1F);
            player.sendMessage(Utils.getMessage("war-lost", getWonAndLostPlaceholders()));
        });
    }

    private TagResolver.Single[] getWonAndLostPlaceholders() {
        return new TagResolver.Single[] {
                Placeholder.component("money-amount", text(calculateWonMoney())),
                Placeholder.component("bonus-claims", text(calculateBonusBlocks())),
                Placeholder.component("winner", text(winner.name())),
                Placeholder.component("loser", text(loser.name()))
        };
    }


    private void giveWarEarnings() {
        if (winner instanceof WarringTown town) {
            town.getTown().getAccount().deposit(calculateWonMoney(), "Won a war against " + loser.name());
            town.getTown().addBonusBlocks(calculateBonusBlocks());
        } else {
            WarringNation winningNation = (WarringNation) winner;
            winningNation.getNation().getCapital().getAccount().deposit(calculateWonMoney(), "Won a war against" + loser.name());
            winningNation.getNation().getCapital().addBonusBlocks(calculateBonusBlocks());
        }
    }

    private int calculateBonusBlocks() {
        if (loser instanceof WarringTown warringTown) {
            return (int) (warringTown.getTown().getNumTownBlocks() * 0.25);
        } else {
            WarringNation losingNation = (WarringNation) loser;
            return (int) (losingNation.getNation().getNumTownblocks() * 0.25);
        }
    }

    private double calculateWonMoney() {
        if (loser instanceof WarringTown warringTown) {
            return warringTown.getTown().getAccount().getHoldingBalance() * 0.5;
        } else {
            double total = 0;
            WarringNation losingNation = (WarringNation) loser;
            for (Town town : losingNation.getNation().getTowns()) {
                total += town.getAccount().getHoldingBalance() * 0.5;
            }
            total += losingNation.getNation().getAccount().getHoldingBalance() * 0.5;
            return total;
        }
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
