package org.unitedlands.wars.war;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;
import org.unitedlands.wars.war.entities.WarringTown;
import org.unitedlands.wars.war.health.WarHealth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static org.unitedlands.wars.Utils.getTownyResident;

public class WarDatabase {

    private static final HashSet<WarringTown> WARRING_TOWNS = new HashSet<>();
    private static final HashSet<WarringNation> WARRING_NATIONS = new HashSet<>();
    private static final HashSet<War> WARS = new HashSet<>();
    private static final HashSet<War> WARS_TO_REMOVE = new HashSet<>();
    private static final UnitedWars PLUGIN = UnitedWars.getInstance();

    public static void loadSavedData() {
        FileConfiguration warConfig = PLUGIN.getWarConfig();
        ConfigurationSection section = warConfig.getConfigurationSection("wars");
        if (section == null) {
            section = warConfig.createSection("wars");
        }

        for (String warUUID : section.getKeys(false)) {
            // Create needed lists for the War object
            List<Town> towns = new ArrayList<>();
            List<Nation> nations = new ArrayList<>();
            HashSet<Resident> warResidents = new HashSet<>();

            ConfigurationSection warSection = section.getConfigurationSection(warUUID);
            WarType warType = WarType.valueOf(warSection.getString("type"));

            // Start loading the saved towns
            if (warType.equals(WarType.TOWNWAR)) {
                ConfigurationSection savedTowns = warSection.getConfigurationSection("warring-towns");
                for (String townUUID : savedTowns.getKeys(false)) {
                    Town town = UnitedWars.TOWNY_API.getTown(UUID.fromString(townUUID));
                    if (town == null)
                        continue;

                    towns.add(town);
                    warResidents.addAll(getMercenaries(savedTowns.getConfigurationSection(townUUID)));
                    warResidents.addAll(town.getResidents());
                }
            }

            // Start loading saved nations
            if (warType.equals(WarType.NATIONWAR)) {
                ConfigurationSection savedNations = warSection.getConfigurationSection("warring-nations");
                for (String nationUUID : savedNations.getKeys(false)) {
                    Nation nation = UnitedWars.TOWNY_API.getNation(UUID.fromString(nationUUID));
                    if (nation == null)
                        continue;
                    List<Resident> nationResidents = nation.getResidents();
                    ConfigurationSection nationSection = savedNations.getConfigurationSection(nationUUID);
                    // Add allied residents if any.
                    nationResidents.addAll(getAlliedResidents(nationSection));
                    nationResidents.addAll(getMercenaries(nationSection));
                    warResidents.addAll(nationResidents);
                    nations.add(nation);
                }
            }

            War war = new War(towns, nations, warResidents, warType, UUID.fromString(warUUID));

            for (WarringEntity warringEntity : war.getWarringEntities()) {
                ConfigurationSection entitySection = section.getConfigurationSection(war.getUuid() + "." + warringEntity.getPath() + "." + warringEntity.getUUID());
                // Start setting up saved mercenaries
                getMercenaries(entitySection).forEach(warringEntity::addMercenary);

                // Set up all the health stuff from the saved data
                WarHealth warHealth = generateWarHealth(entitySection.getConfigurationSection("health"), warringEntity.name());
                warHealth.setValidPlayers(getValidHealingPlayers(warringEntity));
                warringEntity.setWarHealth(warHealth);
                // Resume any previous healing
                if (warHealth.isHealing())
                    warHealth.heal();
            }

            for (WarringNation warringNation: war.getWarringNations()) {
                List<String> savedAllies = section.getStringList(war.getUuid() + "." + warringNation.getPath() + "." + warringNation.getUUID() + ".allies");
                List<UUID> uuids = new ArrayList<>();
                savedAllies.forEach(ally -> uuids.add(UUID.fromString(ally)));
                warringNation.setAllies(uuids);
            }
        }
        PLUGIN.getLogger().log(Level.INFO, "Loaded " + WARS.size() + " wars!");
    }

    private static List<UUID> getAllies(ConfigurationSection nationSection) {
        List<UUID> allies = new ArrayList<>();
        List<String> savedAllies = nationSection.getStringList("allies");
        for (String uuid: savedAllies) {
            Nation nation = UnitedWars.TOWNY_API.getNation(UUID.fromString(uuid));
            if (nation == null)
                continue;
            allies.add(UUID.fromString(uuid));
        }
        return allies;
    }
    private static List<Resident> getAlliedResidents(ConfigurationSection nationSection) {
        List<Resident> alliedResidents = new ArrayList<>();
        List<UUID> joinedAllies = getAllies(nationSection);
        if (!joinedAllies.isEmpty()) {
            for (UUID uuid: joinedAllies) {
                Nation ally = UnitedWars.TOWNY_API.getNation(uuid);
                alliedResidents.addAll(ally.getResidents());
            }
        }

        return alliedResidents;
    }

    private static List<Resident> getMercenaries(ConfigurationSection entitySection) {
        List<Resident> mercenaries = new ArrayList<>();
        List<String> savedMercenaries = entitySection.getStringList("mercenaries");
        if (!savedMercenaries.isEmpty()) {
            for (String uuid : savedMercenaries) {
                mercenaries.add(Utils.getTownyResident(UUID.fromString(uuid)));
            }
        }
        return mercenaries;
    }

    public static void saveWarData() {
        PLUGIN.getLogger().log(Level.INFO, "Saving war data...");
        FileConfiguration warConfig = PLUGIN.getWarConfig();
        if (warConfig == null) {
            PLUGIN.getLogger().log(Level.INFO, "War config is null!");
            return;
        }

        for (War war : WARS) {
            // Create a war section with the UUID of the war.
            ConfigurationSection warSection = warConfig.createSection("wars." + war.getUuid().toString());
            // Save the War type
            warSection.set("type", war.getWarType().toString());
            // Create a unified list of all war participants.
            List<WarringEntity> warringEntities = new ArrayList<>();
            if (war.getWarType() == WarType.TOWNWAR) {
                warringEntities.addAll(war.getWarringTowns());
            }
            if (war.getWarType() == WarType.NATIONWAR) {
                warringEntities.addAll(war.getWarringNations());
            }
            // Loop through each involved Entity in the war.
            for (WarringEntity warringEntity : warringEntities) {
                // Create a section for that entity
                ConfigurationSection entitySection = warSection.createSection(warringEntity.getPath() + "." + warringEntity.getUUID());
                saveHealth(warringEntity, entitySection);

                if (!warringEntity.getMercenaries().isEmpty()) {
                    entitySection.set("mercenaries", convertUUIDToString(warringEntity.getMercenaries()));
                }

                if (warringEntity instanceof WarringNation warringNation) {
                    if (!warringNation.getJoinedAllies().isEmpty()) {
                        entitySection.set("allies", convertUUIDToString(warringNation.getJoinedAllies()));
                    }
                }

            }
        }

        // Wars ready for removal
        // Either ended or purged.
        for (War war : WARS_TO_REMOVE) {
            warConfig.set("wars." + war.getUuid(), null);
        }

        // Save the file.
        try {
            warConfig.save(new File(PLUGIN.getDataFolder(), "wars.yml"));
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.INFO, "Failed to save data!");
        }
    }

    private static List<String> convertUUIDToString(List<UUID> list) {
        List<String> strings = new ArrayList<>(list.size());
        for (UUID uuid: list)
            strings.add(uuid.toString());

        return strings;
    }
    private static void saveHealth(WarringEntity warringEntity, ConfigurationSection entitySection) {
        // Create an inner health section to save health data.
        ConfigurationSection healthSection = entitySection.createSection("health");
        WarHealth health = warringEntity.getWarHealth();
        healthSection.set("max", health.getMaxHealth());
        healthSection.set("current", health.getValue());
        if (health.isHealing()) {
            healthSection.set("healing", true);
            healthSection.set("start-time", health.getHealerStartTime());
        }
    }

    public static void addWar(War war) {
        WARS.add(war);
    }

    public static void addWarringTown(WarringTown warringTown) {
        WARRING_TOWNS.add(warringTown);
    }

    public static void addWarringNation(WarringNation warringNation) {
        WARRING_NATIONS.add(warringNation);
    }

    public static void removeWar(War war) {
        WARS.remove(war);
        WARS_TO_REMOVE.add(war);
    }

    public static void removeWarringEntity(WarringEntity warringEntity) {
        if (warringEntity instanceof WarringTown)
            WARRING_TOWNS.remove(warringEntity);
        else if (warringEntity instanceof WarringNation)
            WARRING_NATIONS.remove(warringEntity);
    }

    public static void removeWarringTown(Town town) {
        WarringTown warringTown = getWarringTown(town);
        removeWarringEntity(warringTown);
    }

    public static void removeWarringNation(Nation nation) {
        WarringNation warringNation = getWarringNation(nation);
        removeWarringEntity(warringNation);
    }

    public static WarringEntity getWarringEntity(Player player) {
        Resident resident = Utils.getTownyResident(player);
        return getWarringEntity(resident);
    }

    public static WarringEntity getWarringEntity(UUID uuid) {
        Resident resident = Utils.getTownyResident(uuid);
        return getWarringEntity(resident);
    }

    public static WarringEntity getWarringEntity(Resident resident) {
        HashSet<WarringEntity> warringEntities = getWarringEntities();
        for (WarringEntity warringEntity : warringEntities) {
            if (warringEntity.getWarParticipants().contains(resident)) {
                return warringEntity;
            }
        }
        return null;
    }

    public static WarringEntity getWarringEntity(WarHealth warHealth) {
        for (WarringEntity warringEntity : getWarringEntities()) {
            if (warringEntity.getWarHealth().equals(warHealth))
                return warringEntity;
        }
        return null;
    }

    public static WarringTown getWarringTown(Town town) {
        for (WarringTown warringTown : WARRING_TOWNS) {
            if (warringTown.getTown().equals(town)) return warringTown;
        }
        return null;
    }

    public static WarringNation getWarringNation(Nation nation) {
        for (WarringNation warringNation : WARRING_NATIONS) {
            if (warringNation.getNation().equals(nation)) return warringNation;
        }
        return null;
    }

    public static War getWar(UUID uuid) {
        for (War war : WARS) {
            if (war.getUuid().equals(uuid))
                return war;
        }
        return null;
    }

    public static War getWar(Player player) {
        for (War war : WARS) {
            Resident resident = getTownyResident(player);
            if (war.getWarParticipants().contains(resident)) {
                return war;
            }
        }
        return null;
    }

    public static War getWar(Town town) {
        for (War war : WARS) {
            if (war.getWarParticipants().contains(town.getMayor())) {
                return war;
            }
        }
        return null;
    }

    public static boolean hasTown(Town town) {
        return getWarringTown(town) != null;
    }

    public static boolean hasNation(Nation nation) {
        return getWarringNation(nation) != null;
    }

    public static boolean hasWar(Player player) {
        if (WARS.isEmpty())
            return false;
        return getWar(player) != null;
    }

    public static boolean hasWar(Town town) {
        return hasTown(town) || hasNation(town.getNationOrNull());
    }


    public static HashSet<WarringTown> getWarringTowns() {
        return WARRING_TOWNS;
    }

    public static HashSet<WarringNation> getWarringNations() {
        return WARRING_NATIONS;
    }

    public static HashSet<War> getWars() {
        return WARS;
    }

    @NotNull
    public static HashSet<WarringEntity> getWarringEntities() {
        HashSet<WarringEntity> warringEntities = new HashSet<>(WARRING_TOWNS);
        warringEntities.addAll(WARRING_NATIONS);
        return warringEntities;
    }

    public static void cleanUpBossBars() {
        for (War war : WarDatabase.getWars()) {
            if (war.hasActiveTimer()) {
                war.getOnlinePlayers().forEach(player -> war.getWarTimer().removeViewer(player));
            }

            war.getOnlinePlayers().forEach(player -> {
                WarringEntity warringEntity = getWarringEntity(player);
                player.hideBossBar(warringEntity.getWarHealth().getBossBar());
            });
        }
    }

    public static void clearSets() {
        WARRING_TOWNS.clear();
        WARRING_NATIONS.clear();
        WARS_TO_REMOVE.addAll(WARS);
        WARS.clear();
    }

    private static WarHealth generateWarHealth(ConfigurationSection healthSection, String name) {
        WarHealth warHealth = new WarHealth(name);
        warHealth.setMaxHealth(healthSection.getInt("max"));
        warHealth.setHealth(healthSection.getInt("current"));
        warHealth.setHealing(healthSection.getBoolean("healing"));
        warHealth.setHealerStartTime(healthSection.getLong("start-time"));
        return warHealth;
    }

    private static int getValidHealingPlayers(WarringEntity entity) {
        int amount = 0;
        for (Player p : entity.getOnlinePlayers()) {
            if (!WarDataController.hasResidentLives(Utils.getTownyResident(p)) || p.isInvisible())
                continue;
            amount++;
        }
        return amount;
    }
}
