package org.unitedlands.wars.war.health;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.events.WarHealthChangeEvent;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.UUID;

public class WarHealth {
    private final String name;
    private int health = 100;
    private int maxHealth = 100;
    private boolean isHealing = false;
    private final Healer healer = new Healer(this);
    private final BossBar bossBar = generateBossBar();
    private HashSet<UUID> healingPlayers = new HashSet<>();

    public WarHealth(String name) {
        this.name = name;
    }

    public WarHealth(Town town) {
        this.name = town.getFormattedName();
    }

    public WarHealth(Nation nation) {
        this.name = nation.getFormattedName();
    }


    public BossBar getBossBar() {
        // make sure the health bar is updated
        updateHealthBar();
        return bossBar;
    }


    public boolean isHealing() {
        return isHealing;
    }

    public void setHealing(boolean healing) {
        isHealing = healing;
    }

    private BossBar generateBossBar() {
        String mainColor = getMainColor();
        String bracketColor = getBracketColor();
        Component name = getTitle(MessageFormat.format("{3}{0} <bold>HP: {4}[</bold>{3}{1}/{2}<bold>{4}]",
                this.name, this.health, this.maxHealth, mainColor, bracketColor));
        return BossBar.bossBar(name, 1F, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_10);
    }

    public void updateHealthBar() {
        String mainColor = getMainColor();
        String bracketColor = getBracketColor();
        String name = MessageFormat.format("{3}{0} <bold>HP: {4}[</bold>{3}{1}/{2}<bold>{4}]",
                this.name, this.health, this.maxHealth, mainColor, bracketColor);
        bossBar.name(getTitle(name));
        bossBar.progress((float) health / 100F);
        bossBar.color(getBossBarColor());
    }

    private BossBar.Color getBossBarColor() {
        String mainColor = getMainColor();
        return switch (mainColor) {
            case "<gold>" -> BossBar.Color.YELLOW;
            case "<dark_red>" -> BossBar.Color.RED;
            default -> BossBar.Color.GREEN;
        };
    }

    public int getValue() {
        return health;
    }

    public long getHealerStartTime() {
        return healer.getStartTime();
    }

    public void setHealerStartTime(long t) {
        healer.setStartTime(t);
    }

    public void show(Player player) {
        player.showBossBar(getBossBar());
        if (isHealing) {
            getHealer().showTimer(player);
        }
    }

    public void hide(Player player) {
        player.hideBossBar(getBossBar());
        if (isHealing) {
            getHealer().hideTimer(player);
        }
    }

    public void increaseHealth(int increment) {
        int newHealth = Math.min(maxHealth, health + increment);
        WarHealthChangeEvent whce = new WarHealthChangeEvent(this, newHealth, maxHealth);
        Bukkit.getServer().getPluginManager().callEvent(whce);
        this.health = newHealth;
        updateHealthBar();
    }

    public void decreaseHealth(int decrement) {
        int newHealth = Math.max(0, health - decrement);
        WarHealthChangeEvent whce = new WarHealthChangeEvent(this, newHealth, maxHealth);
        Bukkit.getServer().getPluginManager().callEvent(whce);
        this.health = newHealth;
        updateHealthBar();
        if (!isHealing())
            heal();
    }

    public void setHealth(int health) {
        this.health = health;
        updateHealthBar();
    }

    private Healer getHealer() {
        return this.healer;
    }

    private Component getTitle(String message) {
        return UnitedWars.MINI_MESSAGE.deserialize(message);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        WarHealthChangeEvent whce = new WarHealthChangeEvent(this, health, Math.max(0, maxHealth));
        Bukkit.getServer().getPluginManager().callEvent(whce);
        if (health == this.maxHealth) {
            setHealth(maxHealth);
        }
        this.maxHealth = Math.max(0, maxHealth);
        updateHealthBar();
    }
    public HashSet<UUID> getHealingPlayers() {
        if (healingPlayers == null) {
            healingPlayers = new HashSet<>();
        }
        return healingPlayers;
    }
    public int getHealingRate() {
        if (getHealingPlayers().size() == 0)
            return 0;
        return 20 / getHealingPlayers().size();
    }

    public void setValidPlayers(HashSet<UUID> healingPlayers) {
        this.healingPlayers.addAll(healingPlayers);
    }

    public void addHealingPlayer(UUID uuid) {
        if (healingPlayers.size() == 0)
            heal();
        healingPlayers.add(uuid);
    }

    public void removeHealingPlayer(UUID uuid) {
        healingPlayers.remove(uuid);
    }

    public void decreaseMaxHealth(int decrease) {
        setMaxHealth(this.maxHealth - decrease);
        if (!isHealing())
            heal();
    }

    public void flash() {
        BossBar.Color current = getBossBarColor();
        BossBar.Color flashTo = BossBar.Color.RED;
        if (current == BossBar.Color.GREEN)
            flashTo = BossBar.Color.YELLOW;
        bossBar.color(flashTo);
        Bukkit.getServer().getScheduler().runTaskLater(UnitedWars.getInstance(), () -> bossBar.color(current), 10);
    }

    private String getMainColor() {
        if (health > 60) {
            return "<green>";
        } else if (health > 35) {
            return "<gold>";
        } else {
            return "<dark_red>";
        }
    }

    private String getBracketColor() {
        if (health > 60) {
            return "<dark_green>";
        } else if (health > 35) {
            return "<yellow>";
        } else {
            return "<dark_red>";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarHealth warHealth = (WarHealth) o;

        if (health != warHealth.health) return false;
        if (maxHealth != warHealth.maxHealth) return false;
        if (!name.equals(warHealth.name)) return false;
        return bossBar.equals(warHealth.bossBar);
    }

    @Override
    public int hashCode() {
        int result = health;
        result = 31 * result + maxHealth;
        result = 31 * result + name.hashCode();
        result = 31 * result + bossBar.hashCode();
        return result;
    }

    public void heal() {
        healer.start();
    }
}
