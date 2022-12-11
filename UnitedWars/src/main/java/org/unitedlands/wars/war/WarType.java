package org.unitedlands.wars.war;

public enum WarType {
    NATIONWAR(20, 7, "Nation War"),
    TOWNWAR(15, 4, "Town War");


    private final int baseCost;
    private final int cooldownInDays;
    private final String formattedName;

    WarType(int baseCost, int cooldownInDays, String formattedName) {
        this.baseCost = baseCost;
        this.cooldownInDays = cooldownInDays;
        this.formattedName = formattedName;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public long cooldown() {
        // convert to ms
        return cooldownInDays * 86_400_000L;
    }

    public String getFormattedName() {
        return formattedName;
    }
}
