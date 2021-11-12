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

public class PvPCmd implements CommandExecutor {

    public boolean isInCombat(Player player) {
        ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        ICombatManager combatManager = plugin.getCombatManager();
        return combatManager.isInCombat(player);
    }

    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    String usage = ChatColor.YELLOW + "Use /pvp to enable/disable your PvP.";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {

            Player p = (Player) sender;

            if (args.length > 0) {
                p.sendMessage(usage);

        } else {

                if(p.hasPermission("chromium.pvp.toggle")){

                    if(isInCombat(p)){
                        p.sendMessage(ChatColor.RED + "You can't use that command while in combat.");
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
                        cooldowns.put(sender.getName(), System.currentTimeMillis());



                        if(args.length == 0){

                            Boolean pvptoggled = customConfig.getBoolean("PvP");
                            if(pvptoggled){
                                customConfig.set("PvP", false);
                                p.sendMessage(ChatColor.YELLOW + "PvP disabled.");
                                try{
                                    customConfig.save(customConfigFile);
                                }catch (Exception e1){
                                    System.out.println("Error with saving configuration for player " + p.getName());
                                    p.sendMessage("Error with saving configuration.");
                                }

                            } else {
                                customConfig.set("PvP", true);
                                p.sendMessage(ChatColor.YELLOW + "PvP enabled.");
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

        return false;

    }

}
