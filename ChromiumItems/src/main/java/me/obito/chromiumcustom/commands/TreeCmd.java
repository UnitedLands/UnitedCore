package me.obito.chromiumcustom.commands;

import me.obito.chromiumcustom.ChromiumItems;
import me.obito.chromiumcustom.util.TreeType;
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
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumItems.getGlobalMsg("NoPerm")));
            return false;
        }

        if(args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m---&r &f&lChromium Core &a&lTrees&r &7&m---\n"
                    + "&e/tree help &f| &7&oUsage of the tree command\n"
                    + "&e/tree seed &f<name> | &7&oGives a seed of the tree\n"
                    + "&e/tree info &f<name> | &7&oPrints information for a given tree\n"
                    + "&e/tree list &f| &7&oPrints all valid tree types\n"
                    + "&e/tree give <player> <name> &f| &7&oGives the player a tree seed"
            ));
        }

        if(args.length > 0) {
            if(args[0].equals("help")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m---&r &f&lChromium Core &a&lTrees&r &7&m---\n"
                        + "&e/tree help\n"
                        + "&e/tree seed <name>\n"
                        + "&e/tree info <name>\n"
                        + "&e/tree list\n"
                        + "&e/tree give <player> <name>"
                ));
            }
            if(args[0].equals("list")) {
                String trees = "";
                for(TreeType t : TreeType.values()) {
                    trees += String.format("&a%s\n",t.name());
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
                            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumItems.getMsg("ReceivedSapling")));
                        } else {
                            sender.sendMessage("Only a player can execute this command!");
                        }
                    } else {
                        sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumItems.getMsg("InvalidTree")));
                    }
                }
                if(args[0].equals("info")) {
                    TreeType tree = null;
                    try{
                        tree = TreeType.valueOf("MANGO");
                    } catch (Exception e1){
                        sender.sendMessage("NE");
                        return false;
                    }

                    if(tree != null) {
                        sender.sendMessage("MANGO");
                      /* sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e&l%s Tree&r\n"
                                        + "&eFruit: &7&o%s\n"
                                        + "&eFruit Block: &7&o%s\n"
                                        + "&eFruit Replacement Block: &7&o%s\n"
                                        + "&eStem Block: &7&o%s\n"
                                        + "&eStem Replacement Block: &7&o%s\n"
                                        + "&esSeed: &7&o%s",
                                args[1].toUpperCase(),
                                tree.getDrop().toString(),
                                tree.getFruitBlock(),
                                tree.getFruitReplaceBlock(),
                                tree.getStemBlock(),
                                tree.getStemReplaceBlock(),
                                tree.getSeed())));*/
                    } else {
                        sender.sendMessage("Invalid tree name.");
                    }
                }
            }
            if(args.length > 2) {
                if(args[0].equals("give")) {
                    Player p = Bukkit.getPlayer(args[1]);
                    if(p != null){
                        try{
                            TreeType tree = TreeType.valueOf(args[2].toUpperCase());
                            if(tree != null) {
                                sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumItems.getMsg("ReceivedSapling")));
                                p.getInventory().addItem(tree.getSeed());
                            }
                        } catch (Exception e1){
                            e1.printStackTrace();
                        }

                    }
                }
            }
        }

        return true;
    }

}
