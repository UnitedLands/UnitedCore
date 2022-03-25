package org.unitedlands.alcohol.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.alcohol.Brand;
import org.unitedlands.alcohol.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BrandCommand implements CommandExecutor {
    private final Brand brand;
    Map<UUID, List<UUID>> invites = new HashMap<>();
    Map<UUID, BukkitTask> inviteTask = new HashMap<>();

    public BrandCommand(Brand brand) {
        this.brand = brand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player player = (Player) sender;

        if (args[0].equals("create")) {
            if (brand.hasBrand(player)) {
                player.sendMessage(Util.getMessage("in-a-brand", brand.getPlayerBrand(player)));
                return true;
            }
            brand.createBrand(args[1], player);
            return true;
        }

        if (args[0].equals("delete")) {
            String brandName = brand.getPlayerBrand(player);
            if (brand.hasBrand(player) && isBrandOwner(player, brandName)) {
                brand.deleteBrand(brandName);
                player.sendMessage(Util.getMessage("brand-deleted", brandName));
            }
        }

        if (args[0].equals("slogan")) {
            String[] rawSlogan = removeFirstArgument(args);
            String slogan = String.join(" ", rawSlogan);
            String brandName = brand.getPlayerBrand(player);
            try {
                brand.setSlogan(brandName, slogan);
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.sendMessage(Util.getMessage("slogan-changed", brandName));
        }

        if (args[0].equals("info")) {
            String brandName = brand.getPlayerBrand(player);
            String brandSlogan = brand.getBrandSlogan(brandName);
            player.sendMessage(brandName + ": " + brandSlogan);
        }

        return true;
    }

    private boolean isBrandOwner(Player player, String brandName) {
        return brand.getBrandOwner(brandName).equals(player);
    }

    private String[] removeFirstArgument(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
