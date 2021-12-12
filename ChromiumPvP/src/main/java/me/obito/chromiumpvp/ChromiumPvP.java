package me.obito.chromiumpvp;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.obito.chromiumpvp.commands.FFNationCmd;
import me.obito.chromiumpvp.commands.FFTownCmd;
import me.obito.chromiumpvp.commands.PvPCmd;
import me.obito.chromiumpvp.commands.TaAdminCmd;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ChromiumPvP extends JavaPlugin implements Listener {

    File configFile;
    public static FileConfiguration Config;
    File customConfigFile;
    FileConfiguration customConfig;

    @Override
    public void onEnable(){
        Bukkit.getPluginManager().getPlugin("ChromiumPvP").saveDefaultConfig();
        Config = Bukkit.getPluginManager().getPlugin("ChromiumPvP").getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("ffnation").setExecutor(new FFNationCmd());
        this.getCommand("tadmin").setExecutor(new TaAdminCmd());
        this.getCommand("fftown").setExecutor(new FFTownCmd());
        this.getCommand("pvp").setExecutor(new PvPCmd());


        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

            try {

                customConfigFile.createNewFile();
                customConfig = new YamlConfiguration();
                customConfig.load(customConfigFile);

            } catch (Exception e1){
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }
            //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);
        }

    }

    public static FileConfiguration getConfigur(){
        return Config;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static String getMsg(String s){
        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                "messages.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try{
            customConfig.load(customConfigFile);
        } catch (Exception e2){
            System.out.println("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("PvP").getString(s);

    }

    public static String getGlobalMsg(String s){
        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                "messages.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try{
            customConfig.load(customConfigFile);
        } catch (Exception e2){
            System.out.println("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("Global").getString(s);


    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){

        Boolean enable = Config.getBoolean("PvPToggleEnabled");

        if(e.getEntity() instanceof Player){
            if(e.getDamager() instanceof Player){
                if(enable) {
                    Player target = (Player) e.getEntity();
                    Player damager = (Player) e.getDamager();

                    File customConfigFile;
                    customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                            "/players/" + damager.getUniqueId() + ".yml");
                    FileConfiguration customConfigDamager;
                    customConfigDamager = new YamlConfiguration();
                    try {
                        customConfigDamager.load(customConfigFile);
                    } catch (Exception e2) {
                    }

                    File customConfigFile2;
                    customConfigFile2 = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                            "/players/" + target.getUniqueId() + ".yml");
                    FileConfiguration customConfigTarget;
                    customConfigTarget = new YamlConfiguration();
                    try {
                        customConfigTarget.load(customConfigFile2);
                    } catch (Exception e3) {
                    }

                    boolean pvpDamager = customConfigDamager.getBoolean("PvP");
                    boolean pvpTarget = customConfigTarget.getBoolean("PvP");

                    if (pvpTarget == false) {
                        e.setCancelled(true);
                        damager.sendMessage(ChatColor.RED + "That player has disabled their pvp!");
                    }

                    if (pvpDamager == false) {
                        e.setCancelled(true);
                        damager.sendMessage(ChatColor.RED + "You have your pvp disabled!");
                    }

                    Resident resident1 = TownyUniverse.getInstance().getResident(damager.getUniqueId());
                    Resident resident2 = TownyUniverse.getInstance().getResident(target.getUniqueId());

                    if (resident1.hasNation() && resident2.hasNation()) {
                        if (resident1.getNationOrNull().equals(resident2.getNationOrNull())) {

                            File customFFile;
                            customFFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumPvP").getDataFolder(),
                                    "/nations/" + resident1.getNationOrNull().getName() + ".yml");
                            FileConfiguration TownConfig;
                            TownConfig = new YamlConfiguration();
                            try {
                                TownConfig.load(customFFile);
                            } catch (Exception e2) {
                            }

                            if (TownConfig.getInt("FriendlyFire") == 0) {
                                e.setCancelled(true);
                                damager.sendMessage(ChatColor.RED + "Player is in same nation as you!");
                            }


                        } else {
                            if (resident1.hasTown() && resident2.hasTown()) {

                                if (resident1.getTownOrNull().equals(resident2.getTownOrNull())) {

                                    File customConfigFile5;
                                    customConfigFile5 = new File(Bukkit.getPluginManager().getPlugin("ChromiumPvP").getDataFolder(),
                                            "/config.yml");
                                    FileConfiguration customConfig5;
                                    customConfig5 = new YamlConfiguration();
                                    try{
                                        customConfig5.load(customConfigFile5);
                                    } catch (Exception e2){
                                        System.out.println("Error with loading configuration");
                                    }

                                    if(customConfig5.getInt("GlobalFriendlyFire") == 0){
                                        File customFFile;
                                        customFFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumPvP").getDataFolder(),
                                                "/towns/" + resident1.getTownOrNull().getName() + ".yml");
                                        FileConfiguration TownConfig;
                                        TownConfig = new YamlConfiguration();
                                        try {
                                            TownConfig.load(customFFile);
                                        } catch (Exception e2) {
                                        }

                                        if (TownConfig.getInt("FriendlyFire") == 0) {
                                            e.setCancelled(true);
                                            damager.sendMessage(ChatColor.RED + "Player is in same town as you!");
                                        }
                                    } else {

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

