package org.unitedlands.brands.stats;

import java.util.UUID;

public class BrandPlayer {
    private final UUID uuid;
    private int brewsMade;
    private int brewsDrunk;
    private int totalStars;
    private int averageStars;

    public BrandPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
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
}
