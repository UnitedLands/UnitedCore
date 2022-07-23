package org.unitedlands.pvp.util;

import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.Objects;

public class OldTownBlock implements Serializable {
    private final int x;
    private final int z;
    private int expiryTime;
    private String townName;
    private int searchRadius;

    public OldTownBlock(WorldCoord worldCoord, int expiryTime, int searchRadius, String townName) {
        this.x = worldCoord.getX();
        this.z = worldCoord.getZ();
        this.expiryTime = expiryTime;
        this.townName = townName;
        this.searchRadius = searchRadius;
    }

    public OldTownBlock(WorldCoord worldCoord) {
        this.x = worldCoord.getX();
        this.z = worldCoord.getZ();
        for (OldTownBlock townblock : Utils.getUnitedPvP().getTownBlocksList()) {
            if (townblock.getX() == this.x && townblock.getZ() == this.z) {
                this.expiryTime = townblock.getExpiryTime();
                this.townName = townblock.getTownName();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OldTownBlock that = (OldTownBlock) o;
        return x == that.x && z == that.z && expiryTime == that.expiryTime && townName.equals(that.townName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, expiryTime);
    }

    @Override
    public String toString() {
        return "OldTownBlock{" +
                "x=" + x +
                ", z=" + z +
                ", expiryTime=" + expiryTime +
                ", townName='" + townName + '\'' +
                '}';
    }

    public int getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(int expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public String getTownName() {
        return townName;
    }
}
