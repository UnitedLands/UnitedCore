package me.obito.chromiumchat.commands;

import me.obito.chromiumchat.ChromiumChat;
import me.obito.chromiumchat.gradient.Gradient;
import me.obito.chromiumchat.gradient.GradientPresets;
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

    String usage = ChatColor.YELLOW + "Use /gradient <toggle> | <preset> | <hexcolor1> <hexcolor2>";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){

            Player p = (Player) sender;

                if(p.hasPermission("chromium.chat.gradient")){

                    if(args.length < 1 || args.length > 2){
                        p.sendMessage(usage);
                    }

                    if(args.length == 1){
                        if(!args[0].equalsIgnoreCase("toggle")){
                            try {

                                Gradient g = GradientPresets.getGradient(args[0]);

                                    File customConfigFile;
                                    customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                            "/players/" + p.getUniqueId() + ".yml");
                                    FileConfiguration customConfig;
                                    customConfig = new YamlConfiguration();
                                    try{
                                        customConfig.load(customConfigFile);
                                    } catch (Exception e2){
                                        System.out.println("Error with loading configuration for player " + p.getName());
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getGlobalMsg("ConfError")));
                                    }

                                    try{
                                        customConfig.set("GradientStart", "none");
                                        customConfig.set("GradientEnd", "none");
                                        customConfig.set("GradientPreset", args[0]);
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getMsg("GradientChanged")));
                                        customConfig.save(customConfigFile);
                                    } catch (Exception e3){
                                        System.out.println("Error with configuration for player " + p.getName());
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getGlobalMsg("ConfError")));
                                    }

                                }
                            catch(Exception exx){
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getMsg("GradientUnknownPreset")));
                            }

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
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getGlobalMsg("ConfError")));
                            }

                            try{
                                if(customConfig.getBoolean("GradientEnabled")){
                                    customConfig.set("GradientEnabled", false);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getMsg("GradientOff")));
                                    customConfig.save(customConfigFile);
                                } else {
                                    customConfig.set("GradientEnabled", true);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getMsg("GradientOn")));
                                    customConfig.save(customConfigFile);
                                }

                            } catch (Exception e3){
                                System.out.println("Error with configuration for player " + p.getName());
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getGlobalMsg("ConfError")));
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
                                customConfig.set("GradientPreset", "none");
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getMsg("GradientChanged")));
                                customConfig.save(customConfigFile);
                            } catch (Exception e3){
                                System.out.println("Error with configuration for player " + p.getName());
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getGlobalMsg("ConfError")));
                            }

                        } else {
                            p.sendMessage(ChatColor.RED + "You must use hex codes for colors.");
                        }

                    }

                } else {

                    p.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', ChromiumChat.getGlobalMsg("NoPerm")));

                }

            }

        return false;
    }

}
