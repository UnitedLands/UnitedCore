package org.unitedlands.unitedchat.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.gradient.Gradient;
import org.unitedlands.unitedchat.gradient.GradientPresets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class GradientCmd implements CommandExecutor {


    //String usageAdmin = (ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradAdminCommand")));

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){

            String usageGrad = (ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradCommand")));

            Player p = (Player) sender;

            String usage = PlaceholderAPI.setPlaceholders(p, usageGrad);

                if (p.hasPermission("united.chat.gradient")) {

                    if (args.length < 1 || args.length > 2) {
                        p.sendMessage(usage);
                    }

                    if (args.length == 1) {
                        if (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
                            try {

                                Gradient g = GradientPresets.getGradient(args[0]);

                                File customConfigFile;
                                customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedCore").getDataFolder(),
                                        "/players/" + p.getUniqueId() + ".yml");
                                FileConfiguration customConfig;
                                customConfig = new YamlConfiguration();
                                try {
                                    customConfig.load(customConfigFile);
                                } catch (Exception e2) {
                                    System.out.println("Error with loading configuration for player " + p.getName());
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("ConfError")));
                                }

                                if(customConfig.getBoolean("GradientEnabled") == false){
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientIsOff")));
                                    return false;
                                }

                                try {
                                    String preset = args[0].toLowerCase();
                                    Gradient gr = GradientPresets.getGradient(preset);
                                    if(gr == null){
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientUnknownPreset")));
                                        return false;
                                    }
                                    if (p.hasPermission("united.chat.gradient." + preset) || p.hasPermission("united.chat.gradient.all")) {

                                            if(gr == null){
                                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientUnknownPreset")));
                                                return false;
                                            }

                                        customConfig.set("GradientStart", "none");
                                        customConfig.set("GradientEnd", "none");
                                        customConfig.set("GradientPreset", args[0]);
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientChanged")));
                                        customConfig.save(customConfigFile);
                                    } else {
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("NoPerm")));
                                    }

                                } catch (Exception e3) {
                                    System.out.println("Error with configuration for player " + p.getName());
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("ConfError")));
                                }


                            } catch (Exception exx) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientUnknownPreset")));
                            }

                        } else {
                            File customConfigFile;
                            customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedCore").getDataFolder(),
                                    "/players/" + p.getUniqueId() + ".yml");
                            FileConfiguration customConfig;
                            customConfig = new YamlConfiguration();
                            try {
                                customConfig.load(customConfigFile);
                            } catch (Exception e2) {
                                System.out.println("Error with loading configuration for player " + p.getName());
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("ConfError")));
                            }

                            try {
                                if (args[0].equalsIgnoreCase("off")) {
                                    customConfig.set("GradientEnabled", false);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientOff")));
                                    customConfig.save(customConfigFile);
                                } else {
                                    customConfig.set("GradientEnabled", true);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientOn")));
                                    customConfig.save(customConfigFile);
                                }

                            } catch (Exception e3) {
                                System.out.println("Error with configuration for player " + p.getName());
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("ConfError")));
                            }
                        }
                    }
                    if (args.length == 2) {

                        if (args[0].startsWith("#") && args[1].startsWith("#")) {

                            if(p.hasPermission("unitedchat.gradient.all")){
                                File customConfigFile;
                                customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedCore").getDataFolder(),
                                        "/players/" + p.getUniqueId() + ".yml");
                                FileConfiguration customConfig;
                                customConfig = new YamlConfiguration();
                                try {
                                    customConfig.load(customConfigFile);
                                } catch (Exception e2) {
                                    System.out.println("Error with loading configuration for player " + p.getName());
                                    p.sendMessage("Error with loading configuration.");
                                }

                                if(customConfig.getBoolean("GradientEnabled") == false){
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientIsOff")));
                                    return false;
                                }

                                try {
                                    customConfig.set("GradientStart", args[0].toLowerCase());
                                    customConfig.set("GradientEnd", args[1].toLowerCase());
                                    customConfig.set("GradientPreset", "none");
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("GradientChanged")));
                                    customConfig.save(customConfigFile);
                                } catch (Exception e3) {
                                    System.out.println("Error with configuration for player " + p.getName());
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("ConfError")));
                                }

                            } else {
                                p.sendMessage(ChatColor.RED + "You must use hex codes for colors.");
                            }
                            }

                    }

                } else {

                    p.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("NoPerm")));

                }
            }



        return false;
    }

}
