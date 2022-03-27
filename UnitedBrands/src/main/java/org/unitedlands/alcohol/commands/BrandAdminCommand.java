package org.unitedlands.alcohol.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.alcohol.UnitedBrands;
import org.unitedlands.alcohol.Util;
import org.unitedlands.alcohol.brand.Brand;
import org.unitedlands.alcohol.brand.BrandsFile;

import java.util.Arrays;
import java.util.List;

public class BrandAdminCommand implements CommandExecutor {
    private CommandSender sender;
    private final UnitedBrands unitedBrands;
    private final BrandsFile brandsFile;
    private Brand brand;

    public BrandAdminCommand(UnitedBrands unitedBrands, BrandsFile brandsFile) {
        this.unitedBrands = unitedBrands;
        this.brandsFile = brandsFile;
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
            brand = Util.getBrandFromName(extractMultiWordString(args, 1));
            if (brand == null) {
                sender.sendMessage(Util.getMessage("brand-does-not-exist"));
                return true;
            }

            if (args[0].equals("clearslogan")) {
                return clearSlogan();
            }

            if (args[0].equals("delete")) {
                return deleteBrand();
            }
            sendHelpMessage();
        }

        if (args.length == 3) {
            brand = Util.getBrandFromName(extractMultiWordString(args, 2));
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
        brandsFile.reloadConfig();
        sender.sendMessage(Component.text("All configurations and data reloaded!", NamedTextColor.GREEN));
    }

    private boolean deleteBrand() {
        brand.deleteBrand();
        sender.sendMessage(Component.text("Brand " + brand.getBrandName() + " has been successfully deleted!", NamedTextColor.GREEN));
        return true;
    }

    private boolean clearSlogan() {
        brand.setSlogan(null);
        sender.sendMessage(Component.text(brand.getBrandName() + "'s slogan been successfully cleared!", NamedTextColor.GREEN));
        return true;
    }

    private boolean addMember(Player player) {
        if (brand.getMembers().contains(player.getUniqueId().toString())) {
            sender.sendMessage(Component.text("A member with that uuid already exists in this brand!", NamedTextColor.RED));
            return true;
        }

        if (Util.hasBrand(player)) {
            sender.sendMessage(Component
                    .text("That player is already in a different called ", NamedTextColor.RED)
                    .append(Component.text(Util.getPlayerBrand(player).getBrandName(), NamedTextColor.YELLOW)));
            return true;
        }
        sender.sendMessage(Component.text("Player " + player.getName() + " has been added to " + brand.getBrandName(), NamedTextColor.GREEN));
        brand.addMember(player);
        return true;
    }

    private boolean removeMember(Player player) {
        if (!brand.getMembers().contains(player.getUniqueId().toString())) {
            sender.sendMessage(Component.text("A member with that uuid could not be found in that brand!", NamedTextColor.RED));
            return true;
        }
        sender.sendMessage(Component.text("Player " + player.getName() + " has been removed from " + brand.getBrandName(), NamedTextColor.GREEN));
        brand.removeMember(player);
        return true;
    }

    private String extractMultiWordString(String[] args, int excludedArguments) {
        String[] arrayWithoutFirstArguments = Arrays.copyOfRange(args, excludedArguments, args.length);
        return String.join(" ", arrayWithoutFirstArguments);
    }

}
