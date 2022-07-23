package org.unitedlands.pvp.listeners;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import com.palmergames.bukkit.towny.event.player.PlayerKilledPlayerEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.UnitedPvP;
import org.unitedlands.pvp.player.Status;
import org.unitedlands.pvp.util.Utils;

import java.time.Duration;


public class PlayerListener implements Listener {
    private final UnitedPvP unitedPvP;
    private final Utils utils;

    public PlayerListener(UnitedPvP unitedPvP, Utils utils) {
        this.unitedPvP = unitedPvP;
        this.utils = utils;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PvpPlayer file = new PvpPlayer(player);

        if (!player.hasPlayedBefore() || !file.getPlayerFile().exists()) {
            PvpPlayer pvpPlayer = new PvpPlayer(player);
            pvpPlayer.createFile();
        }

    }


    @EventHandler
    public void onPlayerKillPlayer(PlayerKilledPlayerEvent event) {
        Player killer = event.getKiller();
        Player victim = event.getVictim();
        PvpPlayer killerPvP = new PvpPlayer(killer);
        PvpPlayer victimPvP = new PvpPlayer(victim);

        // If both are vulnerable, the killer is being hostile. Increase their hostility.
        if (killerPvP.isVulnerable() && victimPvP.isVulnerable()) {
            killerPvP.setHostility(killerPvP.getHostility() + 1);
        }

        // If the killer is vulnerable and the victim is aggressive or hostile, it was likely self-defense.
        if (killerPvP.isVulnerable() && (victimPvP.isAggressive() || victimPvP.isHostile())) {
            return;
        }
        // If the killer is already hostile/aggressive, and they kill a vulnerable player
        // that signifies a higher level of hostility, therefore increase by 2 points.
        if ((killerPvP.isAggressive() || killerPvP.isHostile()) && victimPvP.isVulnerable()) {
            killerPvP.setHostility(killerPvP.getHostility() + 2);
            return;
        }
        // if the victim is aggressive, and the killer is aggressive, killer becomes more aggressive.
        if (killerPvP.isAggressive() || killerPvP.isHostile()) {
            killerPvP.setHostility(killerPvP.getHostility() + 1);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        World world = event.getDamager().getWorld();

        if (isIgnoredWorld(world)) {
            return;
        }

        if (utils.isPvP(event)) {
            Player target = (Player) event.getEntity();
            Player damager = getAttacker(event.getDamager());

            PvpPlayer pvpDamager = new PvpPlayer(damager);
            PvpPlayer pvpTarget = new PvpPlayer(target);

            if (pvpTarget.getStatus().equals(Status.PASSIVE)) {
                event.setCancelled(true);
                damager.sendMessage(Utils.getMessage("target-pvp-disabled"));
            }

            if (pvpDamager.getStatus().equals(Status.PASSIVE)) {
                event.setCancelled(true);
                damager.sendMessage(Utils.getMessage("own-pvp-disabled"));
            }
        }
    }

    @NotNull
    private FileConfiguration getConfiguration() {
        return unitedPvP.getConfig();
    }

    private boolean isIgnoredWorld(World world) {
        return getConfiguration().getList("IgnoredWorlds").contains(
                world.getName());
    }

    @EventHandler
    public final void onPotionSplash(final PotionSplashEvent event) {
        final ThrownPotion potion = event.getPotion();
        if (event.getAffectedEntities().isEmpty() || !(potion.getShooter() instanceof Player))
            return;

        for (final PotionEffect effect : potion.getEffects())
            if (effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.HARM)) {
                for (final LivingEntity e : event.getAffectedEntities())
                    if (e instanceof Player && utils.getPvPStatus((Player) e) == 0) {
                        event.setIntensity(e, 0);
                    }
                return;
            }
    }

    private Player getAttacker(final Entity damager) {
        if (damager instanceof Projectile) {
            return (Player) ((Projectile) damager).getShooter();
        }
        return (Player) damager;
    }
}
