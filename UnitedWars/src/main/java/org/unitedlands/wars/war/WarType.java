package org.unitedlands.wars.war;

public enum WarType {
    NATION(15, 7),
    TOWN(10, 4);


    private final int baseCost;
    private final int cooldownInDays;

    WarType(int baseCost, int cooldownInDays) {
        this.baseCost = baseCost;
        this.cooldownInDays = cooldownInDays;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public long cooldown() {
        // convert to ms
        return cooldownInDays * 86_400_000L;
    }
}
