package me.obito.chromiumcustom.commands;

import me.obito.chromiumcustom.ChromiumItems;
import me.obito.chromiumcustom.util.CustomItem;
import me.obito.chromiumcustom.util.Logger;
import me.obito.chromiumcustom.util.TreeType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CustomItemCmd implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("chromium.custom.admin")) {
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumItems.getGlobalMsg("NoPerm")));
            return false;
        }


        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length == 0) {
                Logger.logNoPrefix(p, "&e&lChromium Items\n"
                        + "&a/customitem help\n"
                        + "&a/customitem give <name>\n"
                        + "&a/customitem listfqn\n"
                        + "&a/customitem list");
            }
            if(args.length > 0) {

                if(args[0].equals("tree")){
                    TreeType drvo = TreeType.APPLE;
                }

                if(args[0].equals("help")) {
                    Logger.logNoPrefix(p, "&e&lChromium Items\n"
                            + "&b/customitem help\n"
                            + "&b/customitem give <name>\n"
                            + "&b/customitem listfqn\n"
                            + "&b/customitem list");
                    return true;
                }
                if(args[0].equals("listfqn")) {
                    StringBuilder items = new StringBuilder("&e&lChromium Items&r\n");
                    for (Map.Entry<String, CustomItem> e : CustomItem.getAllItems().entrySet()) {
                        items.append(e.getValue().getItem().getItemMeta().getDisplayName()+"\n");
                    }
                    Logger.logNoPrefix(p,items.toString());

                }
                if(args[0].equals("list")) {
                    StringBuilder items = new StringBuilder("&e&lChromium Items\n");
                    for (Map.Entry<String, CustomItem> e : CustomItem.getAllItems().entrySet()) {
                        items.append("&b"+ ChatColor.stripColor(e.getValue().getItem().getItemMeta().getDisplayName())+"\n");
                    }
                    Logger.logNoPrefix(p,items.toString());

                }
            }
            if(args.length > 1) {
                if(args[0].equals("give")) {
                    try{
                        ItemStack item = CustomItem.getItemBySimilarName(args[1]);
                        if(item != null) {
                            p.getInventory().addItem(item);
                            Logger.log("&aYou received a similar item named: "+item.getItemMeta().getDisplayName());
                            return true;
                        }
                    } catch (Exception e1){
                        p.sendMessage("Contact developer.");
                        return true;
                    }

                }
            }
        }


        return true;
    }
}
