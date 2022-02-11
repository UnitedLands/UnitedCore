package me.obito.chromiumpvp;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.obito.chromiumpvp.commands.PvPCmd;
import me.obito.chromiumpvp.hooks.Placeholders;
import me.obito.chromiumpvp.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;

public final class ChromiumPvP extends JavaPlugin implements Listener {

    public static FileConfiguration Config;
    public static Plugin chromiumFinal = Bukkit.getPluginManager().getPlugin("ChromiumFinal");
    File customConfigFile;
    FileConfiguration customConfig;

    public static FileConfiguration getConfigur() {
        return Config;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().getPlugin("ChromiumPvP").saveDefaultConfig();
        Config = Bukkit.getPluginManager().getPlugin("ChromiumPvP").getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("pvp").setExecutor(new PvPCmd());

        // PlaceholderAPI Expansion Register
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }

        customConfigFile = new File(chromiumFinal.getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

            try {

                customConfigFile.createNewFile();
                customConfig = new YamlConfiguration();
                customConfig.load(customConfigFile);

            } catch (Exception e1) {
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        boolean enable = Config.getBoolean("PvPToggleEnabled");
        boolean isIgnoredWorld = Config.getList("IgnoredWorlds").contains(
                e.getEntity().getWorld().getName());

        if (enable && !isIgnoredWorld) {
            if (Utils.isPvP(e)) {
                Player target = (Player) e.getEntity();
                Player damager = getAttacker(e.getDamager());

                boolean pvpDamager = Utils.getPvPStatus(damager);
                boolean pvpTarget = Utils.getPvPStatus(target);


                if (!pvpTarget) {
                    e.setCancelled(true);
                    damager.sendMessage(ChatColor.RED + "That player has disabled their pvp!");
                }

                if (!pvpDamager) {
                    e.setCancelled(true);
                    damager.sendMessage(ChatColor.RED + "You have your pvp disabled!");
                }
            }
        }
    }

    @EventHandler
    public final void onPotionSplash(final PotionSplashEvent event) {
        final ThrownPotion potion = event.getPotion();
        if (event.getAffectedEntities().isEmpty() || !(potion.getShooter() instanceof Player))
            return;

        for (final PotionEffect effect : potion.getEffects())
            if (effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.HARM)) {
                for (final LivingEntity e : event.getAffectedEntities())
                    if (e instanceof Player && !Utils.getPvPStatus((Player) e)) {
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

    @EventHandler
    public final void onExplosiveDamage(final EntityDamageByEntityEvent event) {

        Entity e = event.getEntity();

        if (e instanceof Player) {
            if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                for (final Entity entity : e.getNearbyEntities(10, 10, 10))
                    if (e instanceof Player && !Utils.getPvPStatus((Player) e)) {
                        event.setDamage(0);
                    }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTownEnter(PlayerEnterTownEvent event) {

        Player player = event.getPlayer();
        Resident outlaw = TownyUniverse.getInstance().getResident(player.getUniqueId());
        Town town = event.getEnteredtown();

        if (town.hasOutlaw(outlaw)){
            Utils.setPvPStatus(player, true);
            player.sendTitle("§4§lPvP Enabled!", "§cYou are outlawed in §e" + town.getName() + "§c!", 20, 60, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.4F);
        }

    }


}
