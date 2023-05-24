package org.unitedlands.wars.war;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
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

import java.text.NumberFormat;
import java.util.*;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.*;

public class War {
    private static final UnitedWars plugin = UnitedWars.getInstance();
    private final List<WarringTown> warringTowns;
    private final List<WarringNation> warringNations;
    private final HashSet<UUID> residents;
    private final WarType warType;
    private final UUID uuid;
    private final long startTime;
    private WarTimer warTimer = null;
    private WarringEntity winner;
    private WarringEntity loser;

    public War(List<Town> warringTowns, List<Nation> warringNations, HashSet<Resident> residents, WarType warType) {
        uuid = UUID.randomUUID();
        this.warringTowns = generateWarringTownList(warringTowns);
        this.warringNations = generateWarringNationList(warringNations);
        this.residents = Utils.residentToUUID(residents);
        this.warType = warType;
        this.startTime = System.currentTimeMillis();
        warTimer = new WarTimer(this);
        // Start the war immediately, since this is the first time.
        startWar();
        // Save war to database
        WarDatabase.addWar(this);
    }

    // Used for loading existing wars from the config.
    public War(List<Town> warringTowns, List<Nation> warringNations, HashSet<Resident> residents, WarType warType, long startTime, UUID uuid) {
        this.uuid = uuid;
        this.warringTowns = generateWarringTownList(warringTowns);
        this.warringNations = generateWarringNationList(warringNations);
        this.residents = Utils.residentToUUID(residents);
        this.warType = warType;
        this.startTime = startTime;
        // Save war to internal database
        WarDatabase.addWar(this);
    }

    public List<WarringEntity> getWarringEntities() {
        List<WarringEntity> warringEntities = new ArrayList<>();
        if (warType == WarType.TOWNWAR)
            warringEntities.addAll(warringTowns);
        else
            warringEntities.addAll(warringNations);

        return warringEntities;
    }

    public List<WarringTown> getWarringTowns() {
        return warringTowns;
    }

    public List<WarringNation> getWarringNations() {
        return warringNations;
    }

    public HashSet<Resident> getWarParticipants() {
        HashSet<Resident> uuidResidents = new HashSet<>();
        residents.forEach(uuid -> uuidResidents.add(Utils.getTownyResident(uuid)));
        return uuidResidents;
    }

    public void addResident(Resident resident, Town town) {
        residents.add(resident.getUUID());
        WarringEntity entity = WarDatabase.getWarringTown(town);
        if (entity == null)
            entity = WarDatabase.getWarringNation(town.getNationOrNull());

        if (entity != null) {
            entity.addResident(resident);
            WarDataController.setResidentLives(resident, 3);

            Player player = resident.getPlayer();
            if (player == null)
                return;
            if (hasActiveTimer())
                warTimer.addViewer(player);

            entity.getWarHealth().show(player);
        }
    }

    public void removeTown(Town town) {
        WarringNation warringNation = WarDatabase.getWarringNation(town.getNationOrNull());
        if (warringNation == null)
            return;
        town.getResidents().forEach(resident -> {
            // Remove the lives of the residents. No longer part of the war.
            WarDataController.removeResidentLivesMeta(resident);
            // Remove UUIDs from list
            residents.remove(resident.getUUID());
            if (resident.getPlayer() != null)
                resident.getPlayer().sendMessage(getMessage("town-deleted-removed-from-war"));
        });
    }

    public WarType getWarType() {
        return warType;
    }

    public long getStartTime() {
        return startTime;
    }
    public UUID getUuid() {
        return uuid;
    }

    public WarTimer getWarTimer() {
        return warTimer;
    }

    public HashSet<Player> getOnlinePlayers() {
        HashSet<Player> players = new HashSet<>();
        getWarParticipants().forEach(resident -> {
            if (resident.isOnline())
                players.add(resident.getPlayer());
        });
        return players;
    }

    public void startWar() {
        // Start the war countdowns
        warTimer.startCountdown();

        // Start player procedures
        runPlayerProcedures();

        // Make the involved entities have an active war.
        toggleActiveWar(true);
    }

    public void endWar(WarringEntity winner, WarringEntity loser) {
        this.winner = winner;
        this.loser = loser;
        WarEndEvent warEndEvent = new WarEndEvent(this, winner, loser);
        Bukkit.getServer().getPluginManager().callEvent(warEndEvent);

        // Remove health.
        hideHealth(winner);
        hideHealth(loser);
        removeLives();

        // Notify entities
        notifyWin();
        notifyLoss();
        // Give rewards
        giveWarEarnings();

        // Toggle active war
        toggleActiveWar(false);
        saveLastWarTimes();

        // Clear from database.
        WarDatabase.removeWarringEntity(winner);
        WarDatabase.removeWarringEntity(loser);
        WarDatabase.removeWar(this);
    }

    public void surrenderWar(WarringEntity winner, WarringEntity loser) {
        this.winner = winner;
        this.loser = loser;
        WarEndEvent warEndEvent = new WarEndEvent(this, winner, loser);
        Bukkit.getServer().getPluginManager().callEvent(warEndEvent);

        // Remove health.
        hideHealth(winner);
        hideHealth(loser);
        removeLives();

        // Notify
        notifySurrenderAccepted();
        notifySurrendered();
        // Toggle active war
        toggleActiveWar(false);

        // Clear from database.
        WarDatabase.removeWarringEntity(winner);
        WarDatabase.removeWarringEntity(loser);
        WarDatabase.removeWar(this);
    }

    public void tieWar(WarringEntity first, WarringEntity second) {
        // Call event
        WarEndEvent warEndEvent = new WarEndEvent(this, first, second);
        Bukkit.getServer().getPluginManager().callEvent(warEndEvent);

        // Remove health.
        hideHealth(first);
        hideHealth(second);
        removeLives();

        // Notify
        notifyTie();
        // Toggle active war
        toggleActiveWar(false);

        // Clear from database.
        WarDatabase.removeWarringEntity(first);
        WarDatabase.removeWarringEntity(second);
        WarDatabase.removeWar(this);
    }

    // Called inside WarTimer.
    public void endWarTimer() {
        for (Player player : getOnlinePlayers()) {
            // Remove for again for safe measures
            warTimer.removeViewer(player);

            // Show the health.
            WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
            warringEntity.getWarHealth().show(player);
            // Teleport them to spawn
            teleportPlayerToSpawn(player);
        }
        warTimer = null;
    }

    public void broadcast(Component message) {
        getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    private void notifyTie() {
        Component message = getMessage("war-end-tie");
        Title title = getTitle("<gold><bold>TIE", "<yellow>No one won the war...");
        getOnlinePlayers().forEach(player -> {
            player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, 1f, 1f);
            player.sendMessage(message);
            player.showTitle(title);
        });
    }

    private void runPlayerProcedures() {
        for (Resident resident : getWarParticipants()) {
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

    private void toggleActiveWar(boolean toggle) {
        for (WarringEntity entity : getWarringEntities()) {
            entity.getGovernment().setActiveWar(toggle);
            if (entity.getGovernment() instanceof Nation nation)
                nation.getTowns().forEach(town -> town.setActiveWar(toggle));
        }
    }

    private void saveLastWarTimes() {
        WarDataController.setLastWarTime(winner.getGovernment(), System.currentTimeMillis());
        WarDataController.setLastWarTime(loser.getGovernment(), System.currentTimeMillis());
    }

    public boolean hasActiveTimer() {
        if (warTimer == null)
            return false;
        else
            return warTimer.getRemainingSeconds() > 0;
    }

    private void hideHealth(WarringEntity warringEntity) {
        WarHealth warHealth = warringEntity.getWarHealth();
        warringEntity.getOnlinePlayers().forEach(warHealth::hide);
    }

    private void notifySurrendered() {
        Title title = Utils.getTitle("<dark_red><bold>WAR LOST!", "<red>You've surrendered from the war!");
        loser.getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
            player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, 1F, 1F);
            player.sendMessage(Utils.getMessage("war-lost-surrender", getWonAndLostPlaceholders()));
        });
    }

    private void notifySurrenderAccepted() {
        Title title = Utils.getTitle("<dark_green><bold>VICTORY!", "<green>Your enemy has surrendered!");
        winner.getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
            player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1, 1F, 1F);
            player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        });
    }

    private void notifyWin() {
        Title title = Utils.getTitle("<dark_green><bold>VICTORY!", "<green>The war has ended!");
        winner.getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
            player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1, 1F, 1F);
            player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            player.sendMessage(Utils.getMessage("war-won", getWonAndLostPlaceholders()));
        });
    }

    private void notifyLoss() {
        Title title = Utils.getTitle("<dark_red><bold>WAR LOST!", "<red>The war has ended!");
        loser.getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
            player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, 1F, 1F);
            player.sendMessage(Utils.getMessage("war-lost", getWonAndLostPlaceholders()));
        });
    }

    private void removeLives() {
        getWarParticipants().forEach(WarDataController::removeResidentLivesMeta);
    }

    private TagResolver.Single[] getWonAndLostPlaceholders() {
        return new TagResolver.Single[] {
                Placeholder.component("money-amount", text(NumberFormat.getInstance().format(calculateWonMoney()))),
                Placeholder.component("bonus-claims", text(calculateBonusBlocks())),
                Placeholder.component("winner", text(winner.name())),
                Placeholder.component("loser", text(loser.name()))
        };
    }


    private void giveWarEarnings() {
        Government loserGov = loser.getGovernment();
        double lostMoney = loserGov.getAccount().getHoldingBalance() * 0.5;
        loserGov.getAccount().payTo(lostMoney,  winner.getGovernment().getAccount(), "Won a war against " + loser.name());

        if (loserGov instanceof Nation loserNation) {
            for (Town town : loserNation.getTowns()) {
                double townAmount = town.getAccount().getHoldingBalance() * 0.5;
                town.getAccount().payTo(townAmount, winner.getGovernment().getAccount(), "Lost a war against " + winner.name());
            }
        }


        if (winner instanceof WarringTown) {
            WarringTown town = (WarringTown) winner;
            town.getTown().addBonusBlocks(calculateBonusBlocks());
        } else if (winner instanceof WarringNation) {
            WarringNation nation = (WarringNation) winner;
            nation.getNation().getCapital().addBonusBlocks(calculateBonusBlocks());
        }
    }

    private int calculateBonusBlocks() {
        return (int) (loser.getGovernment().getTownBlocks().size() * 0.25);
    }

    private double calculateWonMoney() {
        double total = 0;
        total += loser.getGovernment().getAccount().getHoldingBalance() * 0.5;

        if (loser.getGovernment() instanceof Nation nation) {
            for (Town town : nation.getTowns()) {
                total += town.getAccount().getHoldingBalance() * 0.5;
            }
        }
        return total;
    }
    private List<WarringTown> generateWarringTownList(List<Town> townList) {
        if (townList == null)
            return null;

        List<WarringTown> generatedList = new ArrayList<>();
        for (Town town : townList) {
            WarringTown entity = new WarringTown(town, new WarHealth(town), town.getResidents(), new ArrayList<>(), this);
            entity.getWarHealth().setValidPlayers(Utils.playerToUUID(entity.getOnlinePlayers()));
            generatedList.add(entity);
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
            WarringNation entity = new WarringNation(nation, new WarHealth(nation), warringResidents, new ArrayList<>(), this);
            entity.getWarHealth().setValidPlayers(Utils.playerToUUID(entity.getOnlinePlayers()));
            generatedList.add(entity);
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
