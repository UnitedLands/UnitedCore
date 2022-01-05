package me.obito.chromiumpvp.commands;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
import me.obito.chromiumpvp.ChromiumPvP;
import me.obito.chromiumpvp.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PvPCmd implements CommandExecutor {

    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();


    public boolean isInCombat(Player player) {
        ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        ICombatManager combatManager = plugin.getCombatManager();
        return combatManager.isInCombat(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        int cooldownTime = ChromiumPvP.getConfigur().getInt("CooldownTimeInSecs");
        String usage = "§eUse /pvp <on/off> | <status>.";
        String usageAdmin = "§eUse /pvp <on/off> | <status> | <player>";


        if (sender instanceof Player) {

            Player p = (Player) sender;
            boolean pvp = Utils.getPvPStatus(p);

            if (p.hasPermission("chromium.pvp.admin")) {
                if (args.length > 2 || args.length < 1) {
                    p.sendMessage(usageAdmin);
                } else {
                    if (args.length == 1) {

                        if (args[0].equalsIgnoreCase("status")) {

                            if (pvp == true) {
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPStatusOn")));
                            } else {
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPStatusOff")));
                            }


                        } else {
                            if (isInCombat(p)) {
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("InCombat")));
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

                                    if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                                        if (args[0].equalsIgnoreCase("on")) {
                                            if (pvp == false) {
                                                Utils.setPvPStatus(p, true);
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPEnabled")));
                                            } else {
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPAlreadyOn")));
                                            }

                                        }
                                        if (args[0].equalsIgnoreCase("off")) {
                                            if (pvp == true) {
                                                Utils.setPvPStatus(p, false);
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPDisabled")));
                                            } else {
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPAlreadyOff")));
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
                        try {
                            p = Bukkit.getServer().getPlayer(args[1]);
                        } catch (Exception e1) {
                            sender.sendMessage(Utils.color(ChromiumPvP.getGlobalMsg("PlayerNotRecognized")));
                            return false;
                        }

                        if (p == null) {
                            sender.sendMessage(Utils.color(ChromiumPvP.getGlobalMsg("PlayerNotRecognized")));
                            return false;
                        }

                        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                            if (args[0].equalsIgnoreCase("on")) {
                                if (pvp == false) {
                                    Utils.setPvPStatus(p, true);
                                    sender.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPEnabledOp")));
                                    p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPEnabledByAdmin")));
                                } else {
                                    sender.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPAlreadyOn")));
                                }

                            }
                            if (args[0].equalsIgnoreCase("off")) {
                                if (pvp == true) {
                                    Utils.setPvPStatus(p, false);
                                    sender.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPDisabledOp")));
                                    p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPDisabledByAdmin")));
                                } else {
                                    sender.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPAlreadyOff")));
                                }

                            }
                        }
                    }
                }
            } else {

                if (p.hasPermission("chromium.pvp.toggle")) {

                    if (args.length == 1) {

                        if (args[0].equalsIgnoreCase("status")) {

                            if (pvp == true) {
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPStatusOn")));
                            } else {
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPStatusOff")));
                            }


                        } else {
                            if (isInCombat(p)) {
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("InCombat")));
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

                                    if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                                        if (args[0].equalsIgnoreCase("on")) {
                                            if (pvp == false) {
                                                Utils.setPvPStatus(p, true);
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPEnabled")));
                                            } else {
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPAlreadyOn")));
                                            }

                                        }
                                        if (args[0].equalsIgnoreCase("off")) {
                                            if (pvp == true) {
                                                Utils.setPvPStatus(p, false);
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPDisabled")));
                                            } else {
                                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("PvPAlreadyOff")));
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
                    p.sendMessage(Utils.color(ChromiumPvP.getGlobalMsg("NoPerm")));
                }

            }

        }
        return false;
    }
}


