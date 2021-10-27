package me.obito.chromiumchat.commands;

import me.obito.chromiumchat.gradient.Gradient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class GradientCmd implements CommandExecutor {

    String usage = ChatColor.YELLOW + "Use /gradient <toggle> | <hexcolor1> <hexcolor2>";
    String usageAdmin = ChatColor.YELLOW + "Use /gradient <toggle> | <hexcolor1> <hexcolor2> [player]";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){

            Player p = (Player) sender;
            if(p.hasPermission("chromium.chat.admin")){

                if(args.length == 1){
                    if(!args[0].equalsIgnoreCase("toggle")){
                        p.sendMessage(usage);
                    } else {
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

                        try{
                            if(customConfig.getBoolean("GradientEnabled")){
                                customConfig.set("GradientEnabled", false);
                                p.sendMessage(ChatColor.YELLOW + "Gradient disabled.");
                                customConfig.save(customConfigFile);
                            } else {
                                customConfig.set("GradientEnabled", true);
                                p.sendMessage(ChatColor.YELLOW + "Gradient enabled.");
                                customConfig.save(customConfigFile);
                            }

                        } catch (Exception e3){
                            System.out.println("Error with configuration for player " + p.getName());
                            p.sendMessage("Error with configuration..");
                        }
                    }
                }

                if(args.length < 1 || args.length > 3){
                    p.sendMessage(usageAdmin);
                } else {

                    if(args.length == 2){

                        if(args[0].startsWith("#") && args[1].startsWith("#")){

                            File customConfigFile;
                            customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                    "/players/" + p.getUniqueId() + ".yml");
                            FileConfiguration customConfig;
                            customConfig = new YamlConfiguration();
                            try{
                                customConfig.load(customConfigFile);
                            } catch (Exception e2){
                                System.out.println("Error with loading configuration for player " + p.getName());
                                p.sendMessage("Error with loading configuration.");
                            }

                            try{
                                customConfig.set("GradientStart", args[0].toLowerCase());
                                customConfig.set("GradientEnd", args[1].toLowerCase());
                                p.sendMessage(ChatColor.YELLOW + "Gradient changed.");
                                customConfig.save(customConfigFile);
                            } catch (Exception e3){
                                System.out.println("Error with configuration for player " + p.getName());
                                p.sendMessage("Error with configuration..");
                            }

                        } else {
                            p.sendMessage(ChatColor.RED + "You must use hex codes for colors.");
                        }

                    }

                    if(args.length == 3){

                        try{
                            Player pla = (Player) Bukkit.getServer().getPlayer(args[2]);
                            if(args[1].startsWith("#") && args[2].startsWith("#")){

                                File customConfigFile;
                                customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                        "/players/" + pla.getUniqueId() + ".yml");
                                FileConfiguration customConfig;
                                customConfig = new YamlConfiguration();
                                try{
                                    customConfig.load(customConfigFile);
                                } catch (Exception e2){
                                    System.out.println("Error with loading configuration for player " + pla.getName());
                                    p.sendMessage("Error with loading configuration.");
                                }

                                try{
                                    customConfig.set("GradientStart", args[1].toLowerCase());
                                    customConfig.set("GradientEnd", args[2].toLowerCase());
                                    p.sendMessage(ChatColor.YELLOW + "Gradient changed.");
                                    customConfig.save(customConfigFile);
                                } catch (Exception e3){
                                    System.out.println("Error with configuration for player " + pla.getName());
                                    p.sendMessage("Error with configuration..");
                                }

                            } else {
                                p.sendMessage(ChatColor.RED + "You must use hex codes for colors.");
                            }
                        } catch (Exception npe){
                            p.sendMessage(usageAdmin);
                        }

                    }

                }

            } else {

                if(p.hasPermission("chromium.chat.gradient")){
                    if(args.length == 1){
                        if(!args[0].equalsIgnoreCase("toggle")){
                            p.sendMessage(usage);
                        } else {
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

                            try{
                                if(customConfig.getBoolean("GradientEnabled")){
                                    customConfig.set("GradientEnabled", false);
                                    p.sendMessage(ChatColor.YELLOW + "Gradient disabled.");
                                    customConfig.save(customConfigFile);
                                } else {
                                    customConfig.set("GradientEnabled", true);
                                    p.sendMessage(ChatColor.YELLOW + "Gradient enabled.");
                                    customConfig.save(customConfigFile);
                                }

                            } catch (Exception e3){
                                System.out.println("Error with configuration for player " + p.getName());
                                p.sendMessage("Error with configuration..");
                            }
                        }
                    }
                    if(args.length == 2){

                        if(args[0].startsWith("#") && args[1].startsWith("#")){

                            File customConfigFile;
                            customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                    "/players/" + p.getUniqueId() + ".yml");
                            FileConfiguration customConfig;
                            customConfig = new YamlConfiguration();
                            try{
                                customConfig.load(customConfigFile);
                            } catch (Exception e2){
                                System.out.println("Error with loading configuration for player " + p.getName());
                                p.sendMessage("Error with loading configuration.");
                            }

                            try{
                                customConfig.set("GradientStart", args[0].toLowerCase());
                                customConfig.set("GradientEnd", args[1].toLowerCase());
                                p.sendMessage(ChatColor.YELLOW + "Gradient changed.");
                                customConfig.save(customConfigFile);
                            } catch (Exception e3){
                                System.out.println("Error with configuration for player " + p.getName());
                                p.sendMessage("Error with configuration..");
                            }

                        } else {
                            p.sendMessage(ChatColor.RED + "You must use hex codes for colors.");
                        }

                    } else {
                        p.sendMessage(usage);
                    }

                } else {

                    p.sendMessage(ChatColor.RED + "You don't have permission.");

                }

            }

        } else {



        }
        return false;
    }

}
