package org.unitedlands.pvp.commands;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.pvp.util.Utils;

import static org.unitedlands.pvp.util.Utils.getMessage;

public class PvPCmd implements CommandExecutor {

    private final Utils utils;

    public PvPCmd(Utils utils) {
        this.utils = utils;
    }

    public boolean isInCombat(Player player) {
        ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        ICombatManager combatManager = plugin.getCombatManager();
        return combatManager.isInCombat(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 2) {
            sender.sendMessage(getMessage("PvPCmd"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 2) {
            if (!sender.hasPermission("united.pvp.force")) {
                sender.sendMessage(getMessage("NoPermission"));
                return true;
            }
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(getMessage("PlayerNotRecognized"));
                return true;
            }
        }

        if (isInCombat(player)) {
            player.sendMessage(getMessage("InCombat"));
            return true;
        }

        if (args[0].equals("on")) {
            if (player != sender) {
                sender.sendMessage(getMessage("PvPEnabledOP"));
            }
            enablePvP(player);
            return true;
        }

        if (args[0].equals("off")) {
            if (player != sender) {
                sender.sendMessage(getMessage("PvPDisabledOP"));
            }
            disablePvP(player);
            return true;
        }

        if (args[0].equals("status")) {
            returnPvPStatus(player);
            return true;
        }

        return false;
    }

    private void enablePvP(Player player) {
        if (utils.getPvPStatus(player)) {
            player.sendMessage(getMessage("PvPAlreadyOn"));
            return;
        }
        utils.setPvPStatus(player, true);
        player.sendMessage(getMessage("PvPEnabled"));
    }

    private void disablePvP(Player player) {
        if (!utils.getPvPStatus(player)) {
            player.sendMessage(getMessage("PvPAlreadyOff"));
            return;
        }
        utils.setPvPStatus(player, false);
        player.sendMessage(getMessage("PvPDisabled"));
    }

    private void returnPvPStatus(Player player) {
        if (utils.getPvPStatus(player)) {
            player.sendMessage(getMessage("PvPStatusOn"));
        }
        player.sendMessage(getMessage("PvPStatusOff"));
    }
}




