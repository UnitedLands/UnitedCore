package org.unitedlands.brands.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.BreweryDatabase;
import org.unitedlands.brands.UnitedBrands;
import org.unitedlands.brands.Util;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.brewery.Brewery;

import java.util.Arrays;
import java.util.List;

public class BreweryAdminCommand implements CommandExecutor {
    private static final UnitedBrands PLUGIN = UnitedBrands.getInstance();
    private CommandSender sender;
    private Brewery brewery;

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
            brewery = BreweryDatabase.getBreweryFromName(extractMultiWordString(args, 1));
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
            brewery = BreweryDatabase.getBreweryFromName(extractMultiWordString(args, 2));
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
        List<String> helpMessage = PLUGIN.getConfig().getStringList("messages.help-command-admin");
        MiniMessage mm = MiniMessage.miniMessage();
        for (String message : helpMessage) {
            sender.sendMessage(mm.deserialize(message));
        }
    }

    private void reloadPlugin() {
        PLUGIN.reloadConfig();
        BreweryDatabase.save();
        BreweryDatabase.load();
        sender.sendMessage(Component.text("All configurations and data reloaded!", NamedTextColor.GREEN));
    }

    private boolean deleteBrewery() {
        BreweryDatabase.delete(brewery);
        sender.sendMessage(Component.text("Brewery " + brewery.getName() + " has been successfully deleted!", NamedTextColor.GREEN));
        return true;
    }

    private boolean clearSlogan() {
        brewery.setSlogan(null);
        sender.sendMessage(Component.text(brewery.getName() + "'s slogan been successfully cleared!", NamedTextColor.GREEN));
        return true;
    }

    private boolean addMember(Player player) {
        if (brewery.getMembers().contains(player.getUniqueId().toString())) {
            sender.sendMessage(Component.text("A member with that uuid already exists in this brewery!", NamedTextColor.RED));
            return true;
        }

        if (BreweryDatabase.isInBrewery(player)) {
            sender.sendMessage(Component
                    .text("That player is already in a different brewery called ", NamedTextColor.RED)
                    .append(Component.text(BreweryDatabase.getPlayerBrewery(player).getName(), NamedTextColor.YELLOW)));
            return true;
        }
        sender.sendMessage(Component.text("Player " + player.getName() + " has been added to " + brewery.getName(), NamedTextColor.GREEN));
        brewery.addMember(player);
        return true;
    }

    private boolean removeMember(Player player) {
        if (!brewery.getMembers().contains(player.getUniqueId().toString())) {
            sender.sendMessage(Component.text("A member with that uuid could not be found in that brewery!", NamedTextColor.RED));
            return true;
        }
        sender.sendMessage(Component.text("Player " + player.getName() + " has been removed from " + brewery.getName(), NamedTextColor.GREEN));
        brewery.removeMember(player);
        return true;
    }

    private String extractMultiWordString(String[] args, int excludedArguments) {
        String[] arrayWithoutFirstArguments = Arrays.copyOfRange(args, excludedArguments, args.length);
        return String.join(" ", arrayWithoutFirstArguments);
    }

}
