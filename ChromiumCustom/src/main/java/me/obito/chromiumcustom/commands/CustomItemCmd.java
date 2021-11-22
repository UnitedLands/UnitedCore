package me.obito.chromiumcustom.commands;

import me.obito.chromiumcustom.ChromiumCustom;
import me.obito.chromiumcustom.util.CustomItem;
import me.obito.chromiumcustom.util.Logger;
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
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', ChromiumCustom.getMsg("NoPerm")));
            return false;
        }


        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length == 0) {
                Logger.logNoPrefix(p, "&e&lChromium Items\n"
                        + "&a/customitem help &f| &7&oBrings this information up\n"
                        + "&a/customitem give <name> &f| &7&oGives the item if the name is similar\n"
                        + "&a/customitem listfqn &f| &7&oList all custom items by there full name (including color)\n"
                        + "&a/customitem list &f| &7&oList all custom items by there full name");
            }
            if(args.length > 0) {
                if(args[0].equals("help")) {
                    Logger.logNoPrefix(p, "&e&lChromium Items\n"
                            + "&b/customitem help &f| &7&oBrings this information up\n"
                            + "&b/customitem give <name> &f| &7&oGives the item if the name is similar\n"
                            + "&b/customitem listfqn &f| &7&oList all custom items by there full name (including color)\n"
                            + "&b/customitem list &f| &7&oList all custom items by there full name");
                    return true;
                }
                if(args[0].equals("listfqn")) {
                    StringBuilder items = new StringBuilder("&e&llChromium Items&\n");
                    for (Map.Entry<String, CustomItem> e : CustomItem.getAllItems().entrySet()) {
                        items.append(e.getValue().getItem().getItemMeta().getDisplayName()+"\n");
                    }
                    Logger.logNoPrefix(p,items.toString());

                }
                if(args[0].equals("list")) {
                    StringBuilder items = new StringBuilder("&e&llChromium Items&\n");
                    for (Map.Entry<String, CustomItem> e : CustomItem.getAllItems().entrySet()) {
                        items.append("&b"+ ChatColor.stripColor(e.getValue().getItem().getItemMeta().getDisplayName())+"\n");
                    }
                    Logger.logNoPrefix(p,items.toString());

                }
            }
            if(args.length > 1) {
                if(args[0].equals("give")) {
                    ItemStack item = CustomItem.getItemBySimilarName(args[1]);
                    if(item != null) {
                        p.getInventory().addItem(item);
                        Logger.log("&aYou received a similar item named: "+item.getItemMeta().getDisplayName());
                        return true;
                    }
                }
            }
        }


        return true;
    }
}
