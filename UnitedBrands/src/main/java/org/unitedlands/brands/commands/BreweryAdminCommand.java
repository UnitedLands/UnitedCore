package org.unitedlands.brands.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.UnitedBrands;
import org.unitedlands.brands.Util;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.brewery.Brewery;
import org.unitedlands.brands.stats.PlayerStatsFile;

import java.util.Arrays;
import java.util.List;

public class BreweryAdminCommand implements CommandExecutor {
    private final UnitedBrands unitedBrands;
    private final BreweriesFile breweriesFile;
    private final PlayerStatsFile playerStatsFile;
    private CommandSender sender;
    private Brewery brewery;

    public BreweryAdminCommand(UnitedBrands unitedBrands, BreweriesFile breweriesFile, PlayerStatsFile playerStatsFile) {
        this.unitedBrands = unitedBrands;
        this.breweriesFile = breweriesFile;
        this.playerStatsFile = playerStatsFile;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("united.brands.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        this.sender = sender;

        if (args.length < 1) {
            sendHelpMessage();
            return true;
        }

        if (args.length == 1) {
            if (args[0].equals("reload")) {
                reloadPlugin();
                return true;
            }

            if (args[0].equals("help")) {
                sendHelpMessage();
                return true;
            }
            sendHelpMessage();
            return true;
        }

        if (args.length == 2) {
            brewery = Util.getBreweryFromName(extractMultiWordString(args, 1));
            if (brewery == null) {
                sender.sendMessage(Util.getMessage("brewery-does-not-exist"));
                return true;
            }

            if (args[0].equals("clearslogan")) {
                return clearSlogan();
            }

            if (args[0].equals("delete")) {
                return deleteBrewery();
            }
            sendHelpMessage();
        }

        if (args.length == 3) {
            brewery = Util.getBreweryFromName(extractMultiWordString(args, 2));
            Player targetPlayer = Bukkit.getPlayer(args[1]);

            if (targetPlayer == null) {
                sender.sendMessage(Component.text("Player " + args[1] + " not recognized!", NamedTextColor.RED));
                return true;
            }

            if (args[0].equals("addmember")) {
                return addMember(targetPlayer);
            }

            if (args[0].equals("removemember")) {
                return removeMember(targetPlayer);
            }
            sendHelpMessage();
        }

        return true;
    }

    private void sendHelpMessage() {
        List<String> helpMessage = unitedBrands.getConfig().getStringList("messages.help-command-admin");
        for (String message : helpMessage) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private void reloadPlugin() {
        unitedBrands.reloadConfig();
        breweriesFile.reloadConfig();
        playerStatsFile.reloadConfig();
        sender.sendMessage(Component.text("All configurations and data reloaded!", NamedTextColor.GREEN));
    }

    private boolean deleteBrewery() {
        brewery.deleteBrewery();
        sender.sendMessage(Component.text("Brewery " + brewery.getBreweryName() + " has been successfully deleted!", NamedTextColor.GREEN));
        return true;
    }

    private boolean clearSlogan() {
        brewery.setSlogan(null);
        sender.sendMessage(Component.text(brewery.getBreweryName() + "'s slogan been successfully cleared!", NamedTextColor.GREEN));
        return true;
    }

    private boolean addMember(Player player) {
        if (brewery.getBreweryMembers().contains(player.getUniqueId().toString())) {
            sender.sendMessage(Component.text("A member with that uuid already exists in this brewery!", NamedTextColor.RED));
            return true;
        }

        if (Util.hasBrewery(player)) {
            sender.sendMessage(Component
                    .text("That player is already in a different brewery called ", NamedTextColor.RED)
                    .append(Component.text(Util.getPlayerBrewery(player).getBreweryName(), NamedTextColor.YELLOW)));
            return true;
        }
        sender.sendMessage(Component.text("Player " + player.getName() + " has been added to " + brewery.getBreweryName(), NamedTextColor.GREEN));
        brewery.addMemberToBrewery(player);
        return true;
    }

    private boolean removeMember(Player player) {
        if (!brewery.getBreweryMembers().contains(player.getUniqueId().toString())) {
            sender.sendMessage(Component.text("A member with that uuid could not be found in that brewery!", NamedTextColor.RED));
            return true;
        }
        sender.sendMessage(Component.text("Player " + player.getName() + " has been removed from " + brewery.getBreweryName(), NamedTextColor.GREEN));
        brewery.removeMemberFromBrewery(player);
        return true;
    }

    private String extractMultiWordString(String[] args, int excludedArguments) {
        String[] arrayWithoutFirstArguments = Arrays.copyOfRange(args, excludedArguments, args.length);
        return String.join(" ", arrayWithoutFirstArguments);
    }

}
