package org.unitedlands.brands.brewery;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Brewery {
    private final String name;
    private final OfflinePlayer owner;
    private final List<String> members;
    private final UUID uuid;
    private String slogan;
    private int level;
    private int brewsMade;
    private int brewsDrunk;
    private int totalStars;
    private int averageStars;


    public Brewery(String name, OfflinePlayer owner, List<String> members, UUID uuid) {
        this.name = name;
        this.owner = owner;
        this.members = members;
        this.uuid = uuid;
    }

    public Brewery(String name, OfflinePlayer owner, List<String> members) {
        this.name = name;
        this.owner = owner;
        this.members = members;
        uuid = UUID.randomUUID();
    }

    @NotNull
    public OfflinePlayer getOwner() {
        return owner;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void updateAverageStars() {
        averageStars = totalStars / brewsDrunk;
    }

    public void addMember(Player player) {
        members.add(player.getUniqueId().toString());
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId().toString());
    }

    public int getBreweryStat(String name) {
        return switch (name) {
            case "brews-made" -> brewsMade;
            case "brews-drunk" -> brewsDrunk;
            case "total-stars" -> totalStars;
            case "average-stars" -> averageStars;
            case "level" -> level;
            default -> 0;
        };
    }

    public void increaseStat(String statName, int increment) {
        switch (statName) {
            case "brews-made" -> brewsMade += increment;
            case "brews-drunk" -> brewsDrunk += increment;
            case "total-stars" -> totalStars += increment;
            case "average-stars" -> averageStars += increment;
            case "level" -> level += increment;
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<String> getMembers() {
        return members;
    }

    public int getBrewsMade() {
        return brewsMade;
    }

    public void setBrewsMade(int brewsMade) {
        this.brewsMade = brewsMade;
    }

    public int getBrewsDrunk() {
        return brewsDrunk;
    }

    public void setBrewsDrunk(int brewsDrunk) {
        this.brewsDrunk = brewsDrunk;
    }

    public int getTotalStars() {
        return totalStars;
    }

    public void setTotalStars(int totalStars) {
        this.totalStars = totalStars;
    }

    public int getAverageStars() {
        return averageStars;
    }

    public void setAverageStars(int averageStars) {
        this.averageStars = averageStars;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

}
