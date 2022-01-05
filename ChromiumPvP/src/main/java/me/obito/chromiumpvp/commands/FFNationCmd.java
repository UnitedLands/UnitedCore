package me.obito.chromiumpvp.commands;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import me.obito.chromiumpvp.ChromiumPvP;
import me.obito.chromiumpvp.util.Utils;
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

public class FFNationCmd implements CommandExecutor {

    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();
    String usage = ChatColor.YELLOW + "Use /ffnation to enable/disable the friendly fire in your nation.";
    TownyUniverse towny = TownyUniverse.getInstance();

    public boolean isInCombat(Player player) {
        ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        ICombatManager combatManager = plugin.getCombatManager();
        return combatManager.isInCombat(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player p = (Player) sender;

            if (args.length > 0) {
                p.sendMessage(usage);

            } else {

                String nationName;
                if (p.hasPermission("towny.command.nation.friendlyfire")) {

                    Resident resident = towny.getResident(p.getUniqueId());
                    if (resident.hasNation()) {
                        Nation nation = resident.getNationOrNull();
                        nationName = nation.getName();
                    } else {
                        System.out.println("Error: Player that is not in nation tried to execute the ff command.");
                        p.sendMessage(Utils.color(ChromiumPvP.getMsg("NoNation")));
                        return false;
                    }

                    if (isInCombat(p)) {
                        p.sendMessage(Utils.color(ChromiumPvP.getMsg("InCombat")));
                    } else {
                        File customConfigFile;
                        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumPvP").getDataFolder(),
                                "/nations/" + nationName + ".yml");

                        if (!customConfigFile.exists()) {
                            try {
                                customConfigFile.getParentFile().mkdirs();
                                customConfigFile.createNewFile();
                                FileConfiguration customConfig;
                                customConfig = new YamlConfiguration();
                                customConfig.load(customConfigFile);
                                customConfig.set("FriendlyFire", 1);
                                customConfig.save(customConfigFile);
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("FFNationEnabled")));
                                return false;
                            } catch (Exception e1) {
                                p.sendMessage("Error with config files for nation.");
                                return false;
                            }
                        }

                        FileConfiguration customConfig;
                        customConfig = new YamlConfiguration();
                        try {
                            customConfig.load(customConfigFile);
                        } catch (Exception e2) {
                            System.out.println("Error with loading configuration for nation " + nationName);
                            p.sendMessage("Error with loading configuration.");
                        }

                        int cooldownTime = ChromiumPvP.getConfigur().getInt("FFTownCooldownTimeInSecs");
                        if (cooldowns.containsKey(sender.getName())) {
                            long secondsLeft = ((cooldowns.get(sender.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                            if (secondsLeft > 0) {
                                sender.sendMessage(ChatColor.RED + "You can use that command in " + secondsLeft + " seconds.");
                                return true;
                            }
                        }
                        cooldowns.put(sender.getName(), System.currentTimeMillis());


                        if (args.length == 0) {

                            int ff = customConfig.getInt("FriendlyFire");
                            if (ff == 0) {
                                customConfig.set("FriendlyFire", 1);
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("FFNationEnabled")));
                                try {
                                    customConfig.save(customConfigFile);
                                } catch (Exception e1) {
                                    System.out.println("Error with saving configuration for town.");
                                    p.sendMessage("Error with saving configuration.");
                                }

                            } else {
                                customConfig.set("FriendlyFire", 0);
                                p.sendMessage(Utils.color(ChromiumPvP.getMsg("FFNationDisabled")));
                                try {
                                    customConfig.save(customConfigFile);
                                } catch (Exception e1) {
                                    System.out.println("Error with saving configuration for nation.");
                                    p.sendMessage("Error with saving configuration.");
                                }
                            }

                        } else {
                            p.sendMessage(usage);
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
