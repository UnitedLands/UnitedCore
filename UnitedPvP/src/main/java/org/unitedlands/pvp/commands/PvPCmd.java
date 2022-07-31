package org.unitedlands.pvp.commands;

import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.util.Utils;

import java.util.List;
import java.util.Objects;

import static org.unitedlands.pvp.util.Utils.getMessage;
import static org.unitedlands.pvp.util.Utils.getUnitedPvP;

public class PvPCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setHostility")) {
                if (player.hasPermission("united.pvp.admin")) {
                    PvpPlayer pvpPlayer = new PvpPlayer(player);
                    pvpPlayer.setHostility(Integer.parseInt(args[2]));
                    pvpPlayer.updatePlayerHostility();
                    player.sendMessage("Hostility set to " + args[2]);
                } else {
                    player.sendMessage(getMessage("no-permission"));
                }
                return true;
            }
        }


        if (args.length == 2) {
            if (args[0].equals("degrade")) {
                PvpPlayer pvpPlayer = new PvpPlayer(player);
                if (args[1].equals("on")) {
                    pvpPlayer.setDegradable(true);
                    player.sendMessage(getMessage("pvp-degrade-enabled"));
                    return true;
                }
                if (args[1].equals("off")) {
                    pvpPlayer.setDegradable(false);
                    player.sendMessage(getMessage("pvp-degrade-disabled"));
                    return true;
                }
            }
        }

        if (args[0].equals("status")) {
            returnPvPStatus(player);
            return true;
        }

        return false;
    }

    private void sendHelpMessage(Player player) {
        FileConfiguration config = getUnitedPvP().getConfig();
        @NotNull List<String> configuredMessage = config.getStringList("messages.help-message");
        for (String line: configuredMessage) {
             player.sendMessage(Utils.miniMessage.deserialize(Objects.requireNonNull(line)));
        }
    }

    private void returnPvPStatus(Player player) {
        PvpPlayer pvpPlayer = new PvpPlayer(player);
        String status = pvpPlayer.getStatus().name().toLowerCase();
        TextReplacementConfig statusReplacer = TextReplacementConfig.builder()
                .match("<status>")
                .replacement(status)
                .build();
        TextReplacementConfig hostilityReplacer = TextReplacementConfig.builder()
                .match("<hostility>")
                .replacement(String.valueOf(pvpPlayer.getHostility()))
                .build();

        player.sendMessage(getMessage("pvp-status")
                .replaceText(statusReplacer)
                .replaceText(hostilityReplacer));
    }
}




