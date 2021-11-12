package me.obito.chromiumpvp;

import com.SirBlobman.combatlogx.api.event.PlayerPreTagEvent;
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

    @Override
    public void onEnable(){
        Bukkit.getPluginManager().getPlugin("ChromiumPvP").saveDefaultConfig();
        Config = Bukkit.getPluginManager().getPlugin("ChromiumPvP").getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("pvp").setExecutor(new PvPCmd());
    }

    public static FileConfiguration getConfigur(){
        return Config;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
                    boolean pvpTarget = customConfigDamager.getBoolean("PvP");

                    if(pvpDamager == false){
                        e.setCancelled(true);
                        damager.sendMessage(ChatColor.RED + "You have your pvp disabled!");
                    } else {

                        if(pvpTarget == false){
                            e.setCancelled(true);
                            damager.sendMessage(ChatColor.RED + "That player has disabled their pvp!");
                        }

                    }



                }
            }
        }



    }

}
