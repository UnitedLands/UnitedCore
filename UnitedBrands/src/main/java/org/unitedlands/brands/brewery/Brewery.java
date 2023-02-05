package org.unitedlands.brands.brewery;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.UnitedBrands;
import org.unitedlands.brands.Util;

import java.util.ArrayList;
import java.util.List;

public class Brewery {
    private final String name;
    private final OfflinePlayer owner;
    private final List<String> members;

    public Brewery(String name, OfflinePlayer owner, List<String> members) {
        this.name = name;
        this.owner = owner;
        this.members = members;
    }

    @NotNull
    public OfflinePlayer getBreweryOwner() {
        return owner;
    }

    @NotNull
    public String getBreweryName() {
        return name;
    }

    public String getBrewerySlogan() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        return breweriesConfig.getString(name + ".slogan");
    }

    public void setSlogan(String slogan) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        breweriesConfig.set(name + ".slogan", slogan);
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    public void createBrewery() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        breweriesConfig.createSection(name);
        ConfigurationSection brewerySection = breweriesConfig.getConfigurationSection(name);
        brewerySection.set("name", name);
        brewerySection.set("owner-uuid", owner.getUniqueId().toString());
        brewerySection.set("level", 0);
        brewerySection.set("brews-made", 0);
        brewerySection.set("brews-drunk", 0);
        brewerySection.set("total-stars", 0);
        brewerySection.set("average-stars", 0);
        brewerySection.set("members", new ArrayList<String>());
        getBreweriesFile().saveConfig(breweriesConfig);
        owner.getPlayer().sendMessage(Util.getMessage("brewery-created", name));
    }

    public void updateAverageStars() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        int totalStars = getBreweryStat("total-stars");
        int brewsDrunk = getBreweryStat("brews-drunk");
        double newAverage = (double) totalStars / brewsDrunk;
        breweriesConfig.set(name + ".average-stars", Math.round(newAverage));
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    public void addMemberToBrewery(Player player) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        @NotNull List<String> members = getBreweryMembers();
        members.add(player.getUniqueId().toString());
        breweriesConfig.set(name + ".members", members);
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    public void removeMemberFromBrewery(Player player) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        @NotNull List<String> members = getBreweryMembers();

        members.remove(player.getUniqueId().toString());
        breweriesConfig.set(name + ".members", members);
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    public void deleteBrewery() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        breweriesConfig.set(name, null);
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    public int getBreweryStat(String statName) {
        return getBreweriesConfig().getInt(name + "." + statName);
    }

    public void increaseStat(String statName, int increment) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        String statPath = name + "." + statName;
        int currentAmount = breweriesConfig.getInt(statPath);
        breweriesConfig.set(statPath, currentAmount + increment);
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    private FileConfiguration getBreweriesConfig() {
        return getBreweriesFile().getBreweriesConfig();
    }

    private BreweriesFile getBreweriesFile() {
        return new BreweriesFile();
    }

    public List<String> getBreweryMembers() {
        return members;
    }
}
