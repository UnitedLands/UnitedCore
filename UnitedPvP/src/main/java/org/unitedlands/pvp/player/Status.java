package org.unitedlands.pvp.player;

public enum Status {
    PASSIVE(0, "🛡" ),
    VULNERABLE(1, "🛡"),
    AGGRESSIVE( 2, "⚔"),
    HOSTILE( 12, "☠");
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
