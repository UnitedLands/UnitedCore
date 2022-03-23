package org.unitedlands.pvp.listeners;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
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
import org.unitedlands.pvp.UnitedPvP;
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

        if (!player.hasPlayedBefore() || unitedPvP.getPlayerConfig(player) == null) {
            unitedPvP.createPlayerFile(player);
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

            boolean pvpDamager = utils.getPvPStatus(damager);
            boolean pvpTarget = utils.getPvPStatus(target);

            if (!pvpTarget) {
                event.setCancelled(true);
                damager.sendMessage(utils.getMessage("TargetPvPDisabled"));
            }

            if (!pvpDamager) {
                event.setCancelled(true);
                damager.sendMessage(utils.getMessage("OwnPvPDisabled"));
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
                    if (e instanceof Player && !utils.getPvPStatus((Player) e)) {
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


    @EventHandler(priority = EventPriority.NORMAL)
    public void onTownEnter(PlayerEnterTownEvent event) {

        Player player = event.getPlayer();
        Resident outlaw = TownyUniverse.getInstance().getResident(player.getUniqueId());
        Town town = event.getEnteredtown();

        if (town.hasOutlaw(outlaw)) {
            utils.setPvPStatus(player, true);
            player.showTitle(getOutlawWarningTitle(town));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.4F);
        }
    }

    public static Title getOutlawWarningTitle(Town town) {
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000));

        final Component mainTitle = Component.text("PvP Enabled!", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component subtitle = Component
                .text("You are outlawed in ", NamedTextColor.RED)
                .append(Component.text(town.getName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.RED));
        return Title.title(mainTitle, subtitle, times);
    }
}
