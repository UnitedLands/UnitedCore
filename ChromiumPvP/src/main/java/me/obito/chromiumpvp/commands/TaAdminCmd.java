package me.obito.chromiumpvp.commands;

import me.obito.chromiumpvp.ChromiumPvP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class TaAdminCmd implements CommandExecutor {

    String usage = ChatColor.YELLOW + "Use /tadmin toggle friendlyfire";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player p = (Player) sender;

            if (p.hasPermission("towny.command.admin")) {
                if (args.length > 2 || args.length < 2) {

                    p.sendMessage(usage);


                } else {

                    if (args[0].equalsIgnoreCase("toggle") && args[1].equalsIgnoreCase("friendlyfire")) {

                        File customConfigFile;
                        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumPvP").getDataFolder(),
                                "/config.yml");
                        FileConfiguration customConfig;
                        customConfig = new YamlConfiguration();
                        try {
                            customConfig.load(customConfigFile);
                        } catch (Exception e2) {
                            System.out.println("Error with loading configuration for player " + p.getName());
                            p.sendMessage("Error with loading configuration.");
                        }

                        try {
                            if (customConfig.getInt("GlobalFriendlyFire") == 0) {
                                customConfig.set("GlobalFriendlyFire", 1);
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("GlobalFFEnabled")));
                                customConfig.save(customConfigFile);
                            } else {
                                customConfig.set("GlobalFriendlyFire", 0);
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("GlobalFFDisabled")));
                                customConfig.save(customConfigFile);
                            }

                        } catch (Exception e3) {
                            System.out.println("Error with configuration for player " + p.getName());
                        }


                    } else {
                        p.sendMessage(usage);
                    }


                }
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getGlobalMsg("NoPerm")));
            }


        }

        return false;

    }
}
