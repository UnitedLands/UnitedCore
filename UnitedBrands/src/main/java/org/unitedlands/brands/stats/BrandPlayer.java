package org.unitedlands.brands.stats;

import org.unitedlands.brands.BreweryDatabase;

import java.util.Objects;
import java.util.UUID;

public class BrandPlayer {
    private final UUID uuid;
    private int brewsMade;
    private int brewsDrunk;
    private int totalStars;
    private int averageStars;

    public BrandPlayer(UUID uuid) {
        this.uuid = uuid;
        BreweryDatabase.addPlayer(this);
    }

    public UUID getUUID() {
        return uuid;
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
    public void updateAverageStars() {
        averageStars =  totalStars / brewsDrunk;
    }

    public void increaseStat(String statName, int increment) {
        switch (statName) {
            case "brews-drunk" -> brewsDrunk += increment;
            case "brews-made" -> brewsMade += increment;
            case "average-stars" -> averageStars += increment;
            case "total-stars" -> totalStars += increment;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrandPlayer that = (BrandPlayer) o;

        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}
