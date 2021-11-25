package me.obito.chromiumpvp.commands;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
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
import java.util.HashMap;

public class    PvPCmd implements CommandExecutor {

    int cooldownTime = ChromiumPvP.getConfigur().getInt("CooldownTimeInSecs");

    public boolean isInCombat(Player player) {
        ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        ICombatManager combatManager = plugin.getCombatManager();
        return combatManager.isInCombat(player);
    }

    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    String usage = ChatColor.YELLOW + "Use /pvp <on/off> | <status>";
    String usageAdmin = ChatColor.YELLOW + "Use /pvp <on/off> <player> | <status>";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player p = (Player) sender;

            if(p.hasPermission("chromium.pvp.admin")){
                if(args.length > 2 || args.length < 1) {
                    p.sendMessage(usageAdmin);
                } else {
                    if (args.length == 1) {

                        if (args[0].equalsIgnoreCase("status")) {
                            File customConfigFile;
                            customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                    "/players/" + p.getUniqueId() + ".yml");
                            FileConfiguration customConfig;
                            customConfig = new YamlConfiguration();
                            try {
                                customConfig.load(customConfigFile);
                            } catch (Exception e2) {
                                System.out.println("Error with loading configuration for player " + p.getName());
                                p.sendMessage("Error with loading configuration.");
                            }

                            if (customConfig.getBoolean("PvP") == true) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPStatusOn")));
                            } else {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPStatusOff")));
                            }


                        } else {
                            if (isInCombat(p)) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("InCombat")));
                            } else {
                                if (cooldowns.containsKey(sender.getName())) {
                                    long secondsLeft = ((cooldowns.get(sender.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                                    if (secondsLeft > 0) {
                                        sender.sendMessage(ChatColor.RED + "You can use that command in " + secondsLeft + " seconds.");
                                        return true;
                                    }
                                } else {
                                    if (!p.hasPermission("chromium.pvp.cooldown.ignore")) {
                                        cooldowns.put(sender.getName(), System.currentTimeMillis());
                                    }
                                    File customConfigFile;
                                    customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                            "/players/" + p.getUniqueId() + ".yml");
                                    FileConfiguration customConfig;
                                    customConfig = new YamlConfiguration();
                                    try {
                                        customConfig.load(customConfigFile);
                                    } catch (Exception e2) {
                                        System.out.println("Error with loading configuration for player " + p.getName());
                                        p.sendMessage("Error with loading configuration.");
                                    }

                                    if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                                        if (args[0].equalsIgnoreCase("on")) {
                                            if (customConfig.getBoolean("PvP") == false) {
                                                customConfig.set("PvP", true);
                                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPEnabled")));
                                                try {
                                                    customConfig.save(customConfigFile);
                                                } catch (Exception e1) {
                                                    System.out.println("Error with saving configuration for player " + p.getName());
                                                    p.sendMessage("Error with saving configuration.");
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPAlreadyOn")));
                                            }

                                        }
                                        if (args[0].equalsIgnoreCase("off")) {
                                            if (customConfig.getBoolean("PvP") == true) {
                                                customConfig.set("PvP", false);
                                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPDisabled")));
                                                try {
                                                    customConfig.save(customConfigFile);
                                                } catch (Exception e1) {
                                                    System.out.println("Error with saving configuration for player " + p.getName());
                                                    p.sendMessage("Error with saving configuration.");
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPAlreadyOff")));
                                            }

                                        }
                                    } else {
                                        p.sendMessage(usageAdmin);
                                    }

                                }
                            }
                        }


                    }
                    if (args.length == 2) {
                        try{
                            p = (Player) Bukkit.getServer().getPlayer(args[1]);
                        } catch (Exception e1){
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PlayerNotRecognized")));
                            return false;
                        }

                        if(p == null){
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PlayerNotRecognized")));
                            return false;
                        }

                        File customConfigFile;
                        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                "/players/" + p.getUniqueId() + ".yml");
                        FileConfiguration customConfig;
                        customConfig = new YamlConfiguration();
                        try {
                            customConfig.load(customConfigFile);
                        } catch (Exception e2) {
                            System.out.println("Error with loading configuration for player " + p.getName());
                            p.sendMessage("Error with loading configuration.");
                        }

                        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                            if (args[0].equalsIgnoreCase("on")) {
                                if (customConfig.getBoolean("PvP") == false) {
                                    customConfig.set("PvP", true);
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPEnabledOp")));
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPEnabledByAdmin")));
                                    try {
                                        customConfig.save(customConfigFile);
                                    } catch (Exception e1) {
                                        System.out.println("Error with saving configuration for player " + p.getName());
                                        p.sendMessage("Error with saving configuration.");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPAlreadyOn")));
                                }

                            }
                            if (args[0].equalsIgnoreCase("off")) {
                                if (customConfig.getBoolean("PvP") == true) {
                                    customConfig.set("PvP", false);
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPDisabledOp")));
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPDisabledByAdmin")));
                                    try {
                                        customConfig.save(customConfigFile);
                                    } catch (Exception e1) {
                                        System.out.println("Error with saving configuration for player " + p.getName());
                                        p.sendMessage("Error with saving configuration.");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPAlreadyOff")));
                                }

                            }
                        }
                    }
                }
            } else {

                if(p.hasPermission("chromium.pvp.toggle")) {

                    if (args.length == 1) {

                        if(args[0].equalsIgnoreCase("status")){
                            File customConfigFile;
                            customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                    "/players/" + p.getUniqueId() + ".yml");
                            FileConfiguration customConfig;
                            customConfig = new YamlConfiguration();
                            try {
                                customConfig.load(customConfigFile);
                            } catch (Exception e2) {
                                System.out.println("Error with loading configuration for player " + p.getName());
                                p.sendMessage("Error with loading configuration.");
                            }

                            if(customConfig.getBoolean("PvP") == true){
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPStatusOn")));
                            } else {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPStatusOff")));
                            }


                        } else {
                        if (isInCombat(p)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("InCombat")));
                        } else {
                            if (cooldowns.containsKey(sender.getName())) {
                                long secondsLeft = ((cooldowns.get(sender.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                                if (secondsLeft > 0) {
                                    sender.sendMessage(ChatColor.RED + "You can use that command in " + secondsLeft + " seconds.");
                                    return true;
                                }
                            } else {
                                if (!p.hasPermission("chromium.pvp.cooldown.ignore")) {
                                    cooldowns.put(sender.getName(), System.currentTimeMillis());
                                }
                                File customConfigFile;
                                customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                                        "/players/" + p.getUniqueId() + ".yml");
                                FileConfiguration customConfig;
                                customConfig = new YamlConfiguration();
                                try {
                                    customConfig.load(customConfigFile);
                                } catch (Exception e2) {
                                    System.out.println("Error with loading configuration for player " + p.getName());
                                    p.sendMessage("Error with loading configuration.");
                                }

                                if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                                    if (args[0].equalsIgnoreCase("on")) {
                                        if (customConfig.getBoolean("PvP") == false) {
                                            customConfig.set("PvP", true);
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPEnabled")));
                                            try {
                                                customConfig.save(customConfigFile);
                                            } catch (Exception e1) {
                                                System.out.println("Error with saving configuration for player " + p.getName());
                                                p.sendMessage("Error with saving configuration.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPAlreadyOn")));
                                        }

                                    }
                                    if (args[0].equalsIgnoreCase("off")) {
                                        if (customConfig.getBoolean("PvP") == true) {
                                            customConfig.set("PvP", false);
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPDisabled")));
                                            try {
                                                customConfig.save(customConfigFile);
                                            } catch (Exception e1) {
                                                System.out.println("Error with saving configuration for player " + p.getName());
                                                p.sendMessage("Error with saving configuration.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPAlreadyOff")));
                                        }

                                    }
                                } else {
                                    p.sendMessage(usage);
                                }

                            }
                        }
                    }
                }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("NoPerm")));
                }

            }


        }
        return false;
    }
}

            /*if (args.length > 0) {
                p.sendMessage(usage);

        } else {

                if(p.hasPermission("chromium.pvp.toggle")){

                    if(isInCombat(p)){
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("InCombat")));
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

                        int cooldownTime = ChromiumPvP.getConfigur().getInt("CooldownTimeInSecs");
                        if(cooldowns.containsKey(sender.getName())) {
                            long secondsLeft = ((cooldowns.get(sender.getName())/1000)+cooldownTime) - (System.currentTimeMillis()/1000);
                            if(secondsLeft>0) {
                                sender.sendMessage(ChatColor.RED + "You can use that command in " + secondsLeft + " seconds.");
                                return true;
                            }
                        }
                        if(!p.hasPermission("chromium.pvp.cooldown.ignore")){
                            cooldowns.put(sender.getName(), System.currentTimeMillis());
                        }




                        if(args.length == 0){

                            Boolean pvptoggled = customConfig.getBoolean("PvP");
                            if(pvptoggled){
                                customConfig.set("PvP", false);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPDisabled")));
                                try{
                                    customConfig.save(customConfigFile);
                                }catch (Exception e1){
                                    System.out.println("Error with saving configuration for player " + p.getName());
                                    p.sendMessage("Error with saving configuration.");
                                }

                            } else {
                                customConfig.set("PvP", true);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumPvP.getMsg("PvPEnabled")));
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

        }
*/


