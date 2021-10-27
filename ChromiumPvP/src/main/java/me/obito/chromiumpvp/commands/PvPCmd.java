package me.obito.chromiumpvp.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class PvPCmd implements CommandExecutor {

    String usage = ChatColor.YELLOW + "Use /pvp toggle";
    String usageAdmin = ChatColor.YELLOW + "Use /pvp toggle [player]";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){

            Player p = (Player) sender;

            if(p.hasPermission("chromium.pvp.admin")){

                if(args[0].equalsIgnoreCase("toggle") && args.length == 1){

                    File customConfigFile;
                    customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                            "/players/" + p.getUniqueId() + ".yml");
                    FileConfiguration customConfig;
                    customConfig = new YamlConfiguration();
                    try {
                        customConfig.load(customConfigFile);
                    } catch (Exception e2){
                        System.out.println("Error with loading configuration for player " + p.getName());
                        p.sendMessage("Error with loading configuration.");
                    }

                    Boolean pvptoggled = customConfig.getBoolean("PvP");
                    if(pvptoggled){
                        customConfig.set("PvP", false);
                        p.sendMessage(ChatColor.YELLOW + "PvP disabled.");
                        try{
                            customConfig.save(customConfigFile);
                        }catch (Exception e1){
                            System.out.println("Error with saving configuration for player " + p.getName());
                            p.sendMessage("Error with saving configuration.");
                        }

                    } else {
                        customConfig.set("PvP", true);
                        p.sendMessage(ChatColor.YELLOW + "PvP enabled.");
                        try{
                            customConfig.save(customConfigFile);
                        }catch (Exception e1){
                            System.out.println("Error with saving configuration for player " + p.getName());
                            p.sendMessage("Error with saving configuration.");
                        }
                    }

                } else {
                    p.sendMessage(usage);
                }

            } else {

                if(p.hasPermission("chromium.pvp.command")){

                    File customConfigFile;
                    customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                            "/players/" + p.getUniqueId() + ".yml");
                    FileConfiguration customConfig;
                    customConfig = new YamlConfiguration();
                    try {
                        customConfig.load(customConfigFile);
                    } catch (Exception e2){
                        System.out.println("Error with loading configuration for player " + p.getName());
                        p.sendMessage("Error with loading configuration.");
                    }

                    if(args[0].equalsIgnoreCase("toggle") && args.length == 1){

                        Boolean pvptoggled = customConfig.getBoolean("PvP");
                        if(pvptoggled){
                            customConfig.set("PvP", false);
                            p.sendMessage(ChatColor.YELLOW + "PvP disabled.");
                            try{
                                customConfig.save(customConfigFile);
                            }catch (Exception e1){
                                System.out.println("Error with saving configuration for player " + p.getName());
                                p.sendMessage("Error with saving configuration.");
                            }

                        } else {
                            customConfig.set("PvP", true);
                            p.sendMessage(ChatColor.YELLOW + "PvP enabled.");
                            try{
                                customConfig.save(customConfigFile);
                            }catch (Exception e1){
                                System.out.println("Error with saving configuration for player " + p.getName());
                                p.sendMessage("Error with saving configuration.");
                            }
                        }

                    } else {
                        p.sendMessage(usage);
                    }

                }

            }

        }

        return false;

    }

}
