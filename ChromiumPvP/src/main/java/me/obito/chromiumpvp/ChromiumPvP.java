package me.obito.chromiumpvp;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.obito.chromiumpvp.commands.FFTownCmd;
import me.obito.chromiumpvp.commands.PvPCmd;
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
        this.getCommand("pvp").setExecutor(new PvPCmd());
        //this.getCommand("fftown").setExecutor(new FFTownCmd());

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

        return customConfig.getString(s);

    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){

        Boolean enable = Config.getBoolean("PvPToggleEnabled");

        if(e.getEntity() instanceof Player){
            if(e.getDamager() instanceof Player){
                if(enable){
                    Player target = (Player) e.getEntity();
                    Player damager = (Player) e.getDamager();

                    File customConfigFile;
                    customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                            "/players/" + damager.getUniqueId() + ".yml");
                    FileConfiguration customConfigDamager;
                    customConfigDamager = new YamlConfiguration();
                    try {
                        customConfigDamager.load(customConfigFile);
                    } catch (Exception e2){
                    }

                    File customConfigFile2;
                    customConfigFile2 = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                            "/players/" + target.getUniqueId() + ".yml");
                    FileConfiguration customConfigTarget;
                    customConfigTarget = new YamlConfiguration();
                    try {
                        customConfigTarget.load(customConfigFile2);
                    } catch (Exception e3){
                    }

                    boolean pvpDamager = customConfigDamager.getBoolean("PvP");
                    boolean pvpTarget = customConfigTarget.getBoolean("PvP");

                    if(pvpTarget == false){
                        e.setCancelled(true);
                        damager.sendMessage(ChatColor.RED + "That player has disabled their pvp!");
                    }

                    if(pvpDamager == false){
                        e.setCancelled(true);
                        damager.sendMessage(ChatColor.RED + "You have your pvp disabled!");
                    }

                            Resident resident1 = TownyUniverse.getInstance().getResident(damager.getUniqueId());
                            Resident resident2 = TownyUniverse.getInstance().getResident(target.getUniqueId());

                            if (resident1.hasTown() && resident2.hasTown()) {

                                if(resident1.getTownOrNull().equals(resident2.getTownOrNull())){

                                    e.setCancelled(true);
                                    damager.sendMessage(ChatColor.RED + "Player is in same town as you!");


                            }


                        }

                    }



                }
            }
        }



    }

