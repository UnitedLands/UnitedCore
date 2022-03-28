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

    private final UnitedBrands ub;
    private final String name;
    private final OfflinePlayer owner;
    private final List<String> members;

    public Brewery(UnitedBrands ub, String name, OfflinePlayer owner, List<String> members) {
        this.ub = ub;
        this.name = name;
        this.owner = owner;
        this.members = members;
    }

    public OfflinePlayer getBreweryOwner() {
        return owner;
    }

    public String getBreweryName() {
        return name;
    }

    public String getBrewerySlogan() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        return breweriesConfig.getString("breweries." + name + ".slogan");
    }

    public void setSlogan(String slogan) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        breweriesConfig.set("breweries." + name + ".slogan", slogan);
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    public void createBrewery() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        breweriesConfig.createSection("breweries." + name);
        ConfigurationSection breweriesection = breweriesConfig.getConfigurationSection("breweries." + name);
        breweriesection.set("name", name);
        breweriesection.set("owner-uuid", owner.getUniqueId().toString());
        breweriesection.set("members", new ArrayList<String>());
        getBreweriesFile().saveConfig(breweriesConfig);

        owner.getPlayer().sendMessage(Util.getMessage("brewery-created", name));
    }

    public void addMemberToBrewery(Player player) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        @NotNull List<String> members = getBreweryMembers();
        if (members == null) {
            members = breweriesConfig.getStringList("breweries." + name + ".members");
        }
        try {
            members.add(player.getUniqueId().toString());
            breweriesConfig.set("breweries." + name + ".members", members);
            getBreweriesFile().saveConfig(breweriesConfig);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeMemberFromBrewery(Player player) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        @NotNull List<String> members = getBreweryMembers();
        if (members == null) {
            members = breweriesConfig.getStringList("breweries." + name + ".members");
        }
        try {
            members.remove(player.getUniqueId().toString());
            breweriesConfig.set("breweries." + name + ".members", members);
            getBreweriesFile().saveConfig(breweriesConfig);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBrewery() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        breweriesConfig.set("breweries." + name, null);
        getBreweriesFile().saveConfig(breweriesConfig);
    }

    private FileConfiguration getBreweriesConfig() {
        return getBreweriesFile().getBreweriesConfig();
    }

    private BreweriesFile getBreweriesFile() {
        return new BreweriesFile(ub);
    }


    public List<String> getBreweryMembers() {
        return members;
    }
}
