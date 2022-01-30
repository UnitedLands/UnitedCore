package me.obito.chromium.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class ChromiumMainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){


        Player player = (Player) sender;
        if(player.hasPermission("chromium.admin")){

            if(args.length == 0){
                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Chromium Admin");
                player.sendMessage(ChatColor.DARK_AQUA + "/chromium modules");
                player.sendMessage(ChatColor.DARK_AQUA + "/chromium modules <enable/disable/reload> <module>");
            } else {

                if(args[0].equalsIgnoreCase("modules")){

                    if(args.length == 1){

                        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Modules list:");

                        for(Plugin plugin1 : Bukkit.getPluginManager().getPlugins()){


                            if(plugin1.isEnabled() && plugin1.getName().contains("Chromium") && !plugin1.getName().contains("Final")){
                                player.sendMessage(ChatColor.GREEN + plugin1.getName());
                            }

                            if(!plugin1.isEnabled() && plugin1.getName().contains("Chromium") && !plugin1.getName().contains("Final")){
                                player.sendMessage(ChatColor.RED + plugin1.getName());
                            }

                        }

                    }

                    if(args.length == 2){

                        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Chromium Admin");
                        player.sendMessage(ChatColor.DARK_AQUA + "/chromium modules");
                        player.sendMessage(ChatColor.DARK_AQUA + "/chromium modules <enable/disable/reload> <module>");

                    }

                    if(args.length == 3){

                        if(args[1].equalsIgnoreCase("disable")){
                            Plugin pl = Bukkit.getPluginManager().getPlugin(args[2]);

                            Bukkit.getPluginManager().disablePlugin(pl);
                            sender.sendMessage(ChatColor.YELLOW + "Module " + ChatColor.RED + pl.getName() + ChatColor.YELLOW + " disabled.");
                        }

                        if(args[1].equalsIgnoreCase("enable")){
                            Plugin pl = Bukkit.getPluginManager().getPlugin(args[2]);

                            Bukkit.getPluginManager().enablePlugin(pl);
                            sender.sendMessage(ChatColor.YELLOW + "Module " + ChatColor.GREEN + pl.getName() + ChatColor.YELLOW + " enabled.");

                        }

                        if(args[1].equalsIgnoreCase("reload")){
                            Plugin pl = Bukkit.getPluginManager().getPlugin(args[2]);

                            Bukkit.getPluginManager().disablePlugin(pl);
                            Bukkit.getPluginManager().enablePlugin(pl);
                            sender.sendMessage(ChatColor.YELLOW + "Module " + ChatColor.GREEN + pl.getName() + ChatColor.YELLOW + " reloaded.");

                        }

                    }



                }
            }



        } else {

            player.sendMessage(ChatColor.RED + "You don't have permission.");

        }

        return false;
    }

}
