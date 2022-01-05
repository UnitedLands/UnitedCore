package me.obito.chromiumpvp;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.obito.chromiumpvp.commands.FFNationCmd;
import me.obito.chromiumpvp.commands.FFTownCmd;
import me.obito.chromiumpvp.commands.PvPCmd;
import me.obito.chromiumpvp.commands.TaAdminCmd;
import me.obito.chromiumpvp.hooks.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ChromiumPvP extends JavaPlugin implements Listener {

    public static FileConfiguration Config;
    static Plugin chromiumFinal = Bukkit.getPluginManager().getPlugin("ChromiumFinal");
    File customConfigFile;
    FileConfiguration customConfig;
    TownyUniverse towny = TownyUniverse.getInstance();

    public static FileConfiguration getConfigur() {
        return Config;
    }

    public static String getMsg(String s) {
        File customConfigFile;
        customConfigFile = new File(chromiumFinal.getDataFolder(), "messages.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (Exception e2) {
            System.out.println("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("PvP").getString(s);

    }

    public static String getGlobalMsg(String s) {
        File customConfigFile;
        customConfigFile = new File(chromiumFinal.getDataFolder(), "messages.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (Exception e2) {
            System.out.println("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("Global").getString(s);
    }

    public static boolean getPvPStatus(Player player) {

        boolean isCitizensNPC = player.hasMetadata("NPC");

        if (isCitizensNPC) {
            return false;
        }

        File customConfigFile = new File(chromiumFinal.getDataFolder(),
                "/players/" + player.getUniqueId() + ".yml");

        FileConfiguration playerConfig;
        playerConfig = new YamlConfiguration();
        try {
            playerConfig.load(customConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerConfig.getBoolean("PvP");


    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().getPlugin("ChromiumPvP").saveDefaultConfig();
        Config = Bukkit.getPluginManager().getPlugin("ChromiumPvP").getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("ffnation").setExecutor(new FFNationCmd());
        this.getCommand("tadmin").setExecutor(new TaAdminCmd());
        this.getCommand("fftown").setExecutor(new FFTownCmd());
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
            //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);
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
        boolean isCitizensNPC = e.getEntity().hasMetadata("NPC");
        if (isCitizensNPC) {
            return;
        }

        if (e.getEntity() instanceof Player) {
            if (e.getDamager() instanceof Player) {
                if (enable && !isIgnoredWorld) {
                    Player target = (Player) e.getEntity();
                    Player damager = (Player) e.getDamager();
                    Plugin chromiumPvP = Bukkit.getPluginManager().getPlugin("ChromiumPvP");

                    boolean pvpDamager = getPvPStatus((Player) e.getDamager());
                    boolean pvpTarget = getPvPStatus((Player) e.getEntity());

                    if (!pvpTarget) {
                        e.setCancelled(true);
                        damager.sendMessage(ChatColor.RED + "That player has disabled their pvp!");
                    }

                    if (!pvpDamager) {
                        e.setCancelled(true);
                        damager.sendMessage(ChatColor.RED + "You have your pvp disabled!");
                    }

                    Resident damagerResident = towny.getResident(damager.getUniqueId());
                    Resident targetResident = towny.getResident(target.getUniqueId());

                    if (damagerResident.hasNation() && targetResident.hasNation()) {

                        Nation nation = damagerResident.getNationOrNull();

                        if (nation.getResidents().contains(targetResident)) {

                            File customFFile;
                            customFFile = new File(chromiumPvP.getDataFolder(),
                                    "/nations/" + nation.getName() + ".yml");
                            FileConfiguration TownConfig;
                            TownConfig = new YamlConfiguration();
                            try {
                                TownConfig.load(customFFile);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }

                            if (TownConfig.getInt("FriendlyFire") == 0) {
                                e.setCancelled(true);
                                damager.sendMessage(ChatColor.RED + "Player is in same nation as you!");
                            }


                        } else {
                            if (damagerResident.hasTown() && targetResident.hasTown()) {
                                Town town = damagerResident.getTownOrNull();

                                if (town.getResidents().contains(targetResident)) {

                                    File customConfigFile5;
                                    customConfigFile5 = new File(chromiumPvP.getDataFolder(), "/config.yml");
                                    FileConfiguration customConfig5;
                                    customConfig5 = new YamlConfiguration();
                                    try {
                                        customConfig5.load(customConfigFile5);
                                    } catch (Exception e2) {
                                        System.out.println("Error with loading configuration");
                                    }

                                    if (customConfig5.getInt("GlobalFriendlyFire") == 0) {
                                        File customFFile;
                                        customFFile = new File(chromiumPvP.getDataFolder(),
                                                "/towns/" + town.getName() + ".yml");
                                        FileConfiguration TownConfig;
                                        TownConfig = new YamlConfiguration();
                                        try {
                                            TownConfig.load(customFFile);
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        }

                                        if (TownConfig.getInt("FriendlyFire") == 0) {
                                            e.setCancelled(true);
                                            damager.sendMessage(ChatColor.RED + "Player is in same town as you!");
                                        }
                                    }


                                }


                            }
                        }
                    }


                }


            }
        }
    }


}
