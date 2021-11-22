package me.obito.chromiumcustom.commands;

import me.obito.chromiumcustom.ChromiumCustom;
import me.obito.chromiumcustom.trees.TreeType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TreeCmd implements CommandExecutor {
    //
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("chromium.custom.admin")) {
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumCustom.getMsg("NoPerm")));
            return false;
        }

        if(args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m---&r &f&lChromium Core &a&lTrees&r &7&m---\n"
                    + "&b/tree help &f| &7&oUsage of the tree command\n"
                    + "&b/tree seed &f<name> | &7&oGives a seed of the tree\n"
                    + "&b/tree info &f<name> | &7&oPrints information for a given tree\n"
                    + "&b/tree list &f| &7&oPrints all valid tree types\n"
                    + "&b/tree give <player> <name> &f| &7&oGives the player a tree seed"
            ));
        }

        if(args.length > 0) {
            if(args[0].equals("help")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m---&r &f&lChromium Core &a&lTrees&r &7&m---\n"
                        + "&b/tree help &f| &7&oUsage of the tree command\n"
                        + "&b/tree seed &f<name> | &7&oGives a seed of the tree\n"
                        + "&b/tree info &f<name> | &7&oPrints information for a given tree\n"
                        + "&b/tree list &f| &7&oPrints all valid tree types\n"
                        + "&b/tree give <player> <name> &f| &7&oGives the player a tree seed"
                ));
            }
            if(args[0].equals("list")) {
                String trees = "";
                for(TreeType t : TreeType.values()) {
                    trees += String.format("&b%s\n",t.name());
                };
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e&lTrees&r\n"+
                        trees)));
            }
            if(args.length > 1) {
                if(args[0].equals("seed")) {
                    TreeType tree = TreeType.valueOf(args[1].toUpperCase());
                    if(tree != null) {
                        if(sender instanceof Player) {
                            Player p = (Player) sender;
                            p.getInventory().addItem(TreeType.valueOf(args[1].toUpperCase()).getSeed());
                            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumCustom.getMsg("ReceivedSapling")));
                        } else {
                            sender.sendMessage("Only a player can execute this command!");
                        }
                    } else {
                        sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumCustom.getMsg("InvalidTree")));
                    }
                }
                if(args[0].equals("info")) {
                    TreeType tree = TreeType.valueOf(args[1].toUpperCase());
                    if(tree != null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e&l%s Tree&r\n"
                                        + "&bFruit: &7&o%s\n"
                                        + "&bFruit Block: &7&o%s\n"
                                        + "&bFruit Replacement Block: &7&o%s\n"
                                        + "&bStem Block: &7&o%s\n"
                                        + "&bStem Replacement Block: &7&o%s\n"
                                        + "&bSeed: &7&o%s",
                                args[1].toUpperCase(),
                                tree.getDrop().toString(),
                                tree.getFruitBlock(),
                                tree.getFruitReplaceBlock(),
                                tree.getStemBlock(),
                                tree.getStemReplaceBlock(),
                                tree.getSeed())));
                    } else {
                        sender.sendMessage("Invalid tree name.");
                    }
                }
            }
            if(args.length > 2) {
                if(args[0].equals("give")) {
                    Player p = Bukkit.getPlayer(args[1]);
                    if(p != null){
                        TreeType tree = TreeType.valueOf(args[2].toUpperCase());
                        if(tree != null) {
                            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumCustom.getMsg("ReceivedSapling")));
                            p.getInventory().addItem(tree.getSeed());
                        }
                    }
                }
            }
        }

        return true;
    }

}
