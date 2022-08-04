package org.unitedlands.pvp.player;

public enum Status {
    DEFENSIVE(1, "🛡"),
    AGGRESSIVE( 7, "⚔"),
    HOSTILE( 15, "☠");
    private final int startingValue;
    private final String icon;

    Status(int startingValue, String icon) {
        this.startingValue = startingValue;
        this.icon = icon;
    }

    public int getStartingValue() {
        return startingValue;
    }

    public String getIcon() {
        return icon;
    }
}
