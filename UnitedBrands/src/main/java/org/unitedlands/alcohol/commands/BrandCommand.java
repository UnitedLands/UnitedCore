package org.unitedlands.alcohol.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.alcohol.InviteRequest;
import org.unitedlands.alcohol.UnitedBrands;
import org.unitedlands.alcohol.Util;
import org.unitedlands.alcohol.brand.Brand;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BrandCommand implements CommandExecutor {
    private final UnitedBrands unitedBrands;
    Set<InviteRequest> inviteRequests = new HashSet<>();

    public BrandCommand(UnitedBrands unitedBrands) {
        this.unitedBrands = unitedBrands;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        Brand brand = Util.getPlayerBrand(player);

        if (args[0].equals("create")) {

            if (args[1] == null) {
                player.sendMessage(Util.getMessage("must-specify-brand-name", ""));
                return true;
            }

            String[] rawBrandName = removeFirstArgument(args);
            String brandName = String.join(" ", rawBrandName);

            brand = new Brand(unitedBrands, brandName, player, null);
            if (Util.hasBrand(player)) {
                player.sendMessage(Util.getMessage("in-a-brand", brand.getBrandName()));
                return true;
            }
            brand.createBrand();
            return true;
        }

        if (args[0].equals("delete")) {

            if (args[1] == null) {
                player.sendMessage(Util.getMessage("must-specify-brand-name", ""));
                return true;
            }

            brand = new Brand(unitedBrands, args[1], player, null);
            if (Util.hasBrand(player) && isBrandOwner(player, brand)) {
                brand.deleteBrand();
                player.sendMessage(Util.getMessage("brand-deleted", brand.getBrandName()));
                return true;
            }
            player.sendMessage(Util.getMessage("brand-cannot-be-deleted", brand.getBrandName()));
            return true;
        }

        String brandName = "";

        if (Util.hasBrand(player)) {
            brandName = Util.getPlayerBrand(player).getBrandName();
        }
        if (args[0].equals("invite")) {

            if (args[1] == null) {
                player.sendMessage(Util.getMessage("must-specify-invited-player", ""));
                return true;
            }

            Player receiver = Bukkit.getPlayer(args[1]);
            brand = Util.getPlayerBrand(player);

            if (player == receiver) {
                player.sendMessage(Util.getMessage("cannot-invite-self", brandName));
                return true;
            }

            if (Util.hasBrand(player) && isBrandOwner(player, brand)) {
                InviteRequest inviteRequest = new InviteRequest(player, receiver);
                inviteRequests.add(inviteRequest);
                player.sendMessage(Util.getMessage("player-invited", brandName));
                receiver.sendMessage(Util.getMessage("brand-invite", brandName));
                return true;
            }
            receiver.sendMessage(Util.getMessage("must-own-brand", ""));
            return true;
        }

        if (args[0].equals("accept")) {
            InviteRequest request = getRequest(player);

            if (request == null) {
                player.sendMessage(Util.getMessage("no-requests", ""));
                return true;
            }

            if (Util.getPlayerBrand(request.getReceiver()) != null) {
                player.sendMessage(Util.getMessage("already-in-a-brand", ""));
                return true;
            }

            brand = Util.getPlayerBrand(request.getSender());
            brandName = brand.getBrandName();
            brand.addMember(player);

            request.getSender().sendMessage(Util.getMessage("brand-join-sender", brandName));
            request.getReceiver().sendMessage(Util.getMessage("brand-join", brandName));

            inviteRequests.remove(request);
            return true;

        }

        if (args[0].equals("kick")) {

            if (args[1] == null) {
                player.sendMessage(Util.getMessage("must-specify-kicked-player", ""));
                return true;
            }

            if (!isBrandOwner(player, brand)) {
                player.sendMessage(Util.getMessage("must-own-brand", brandName));
                return true;
            }
            Player kickedPlayer = Bukkit.getPlayer(args[1]);

            if (kickedPlayer == player) {
                player.sendMessage(Util.getMessage("cannot-kick-self", brandName));
                return true;
            }

            brand.removeMember(kickedPlayer);
            kickedPlayer.sendMessage(Util.getMessage("kicked-from-brand", brandName));
            player.sendMessage(Util.getMessage("player-kicked", brandName));
            return true;
        }

        if (args[0].equals("leave")) {
            if (isBrandOwner(player, brand)) {
                player.sendMessage(Util.getMessage("must-delete-brand", brandName));
                return true;
            }
            if (Util.hasBrand(player)) {
                brand.removeMember(player);
                player.sendMessage(Util.getMessage("brand-leave", brandName));
                return true;
            }
            player.sendMessage(Util.getMessage("must-have-brand", brandName));
        }


        if (args[0].equals("deny")) {
            InviteRequest request = getRequest(player);
            if (request != null) {
                request.getSender().sendMessage(Util.getMessage("brand-deny-sender".replace("<player>", request.getSender().getName())
                        , brandName));
                request.getReceiver().sendMessage(Util.getMessage("brand-deny", brandName));
                inviteRequests.remove(request);
                return true;
            }
            player.sendMessage(Util.getMessage("no-requests", ""));
            return true;
        }

        if (args[0].equals("slogan")) {
            String[] rawSlogan = removeFirstArgument(args);
            String slogan = String.join(" ", rawSlogan);
            brand = new Brand(unitedBrands, brandName, player, null);
            try {
                brand.setSlogan(slogan);
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.sendMessage(Util.getMessage("slogan-changed", brandName));
            return true;
        }

        if (args[0].equals("info")) {
            brand = new Brand(unitedBrands, brandName, player, null);
            String brandSlogan = brand.getBrandSlogan();
            player.sendMessage(brandName + ": " + brandSlogan);
            return true;
        }

        return true;
    }


    private InviteRequest getRequest(Player receiver) {
        for (InviteRequest request : inviteRequests) {
            if (request.getReceiver().equals(receiver)) {
                return request;
            }
        }
        return null;
    }

    private boolean isBrandOwner(Player player, Brand brand) {
        try {
            return brand.getBrandOwner().equals(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String[] removeFirstArgument(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
