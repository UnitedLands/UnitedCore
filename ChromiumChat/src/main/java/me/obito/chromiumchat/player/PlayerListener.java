package me.obito.chromiumchat.player;

import me.obito.chromiumchat.ChromiumChat;
import me.obito.chromiumchat.gradient.Gradient;
import me.obito.chromiumchat.gradient.GradientPresets;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import com.palmergames.bukkit.towny.TownyAPI;

public class PlayerListener implements Listener {


    @EventHandler

    public void onJoin(PlayerJoinEvent e){
        File customConfigFile;
        FileConfiguration customConfig;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(), "/players/" + e.getPlayer().getUniqueId() + ".yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

            try {

                customConfigFile.createNewFile();
                customConfig = new YamlConfiguration();
                customConfig.load(customConfigFile);
                customConfig.set("Player Name", e.getPlayer().getName());
                customConfig.set("GradientEnabled", true);
                customConfig.set("GradientStart", "#ffffff");
                customConfig.set("GradientEnd", "#000000");
                customConfig.set("GradientPreset", "none");
                customConfig.set("PvP", false);
                customConfig.save(customConfigFile);

            } catch (Exception e1){
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }
            //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);
        }

    }







    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){

        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(), "/players/" + e.getPlayer().getUniqueId() + ".yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (Exception e2){

        }

       /* if (TownyAPI.getInstance().isWilderness(e.getPlayer().getLocation())){

        }*/


        if(customConfig.getBoolean("GradientEnabled")){

            if(customConfig.getString("GradientPreset").equals("none")){
                ArrayList<String> colors = new ArrayList<>();
                try {

                    colors.add(customConfig.getString("GradientStart"));
                    colors.add(customConfig.getString("GradientEnd"));
                    Gradient g = new Gradient(colors);
                    e.setMessage(g.gradientMessage(e.getMessage(), "", false));

                } catch (Exception e1) {

                    System.out.println("Gradient error with player " + e.getPlayer().getName());
                    e.setMessage(e.getMessage());

                }
            } else {
                try {

                    Gradient g = GradientPresets.getGradient(customConfig.getString("GradientPreset"));
                    e.setMessage(g.gradientMessage(e.getMessage(), "", false));

                } catch (Exception e1) {

                    System.out.println("Gradient error with player " + e.getPlayer().getName());
                    e.setMessage(e.getMessage());

                }
            }




        } else {


            String m = ChromiumChat.getConfigur().getString("Default Message Color") + e.getMessage();
            String mm = (ChatColor.translateAlternateColorCodes('&', m));
            String mmm = mm;
            for(Player p : Bukkit.getOnlinePlayers()){
                if(m.toLowerCase().contains(p.getName().toLowerCase())){
                    mmm = mm.replace(p.getName(), (ChatColor.translateAlternateColorCodes('&', ChromiumChat.getConfigur()
                            .getString("Player Mention Color") + p.getName() + ChromiumChat.getConfigur().getString("Default Message Color"))));
                }
            }
            e.setMessage(mmm);
        }



    }


}
