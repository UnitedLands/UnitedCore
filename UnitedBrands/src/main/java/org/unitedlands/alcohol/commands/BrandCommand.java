package org.unitedlands.alcohol.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.List;
import java.util.Set;

public class BrandCommand implements CommandExecutor {
    private final UnitedBrands unitedBrands;
    private Player player;
    Set<InviteRequest> inviteRequests = new HashSet<>();

    public BrandCommand(UnitedBrands unitedBrands) {
        this.unitedBrands = unitedBrands;
    }

    // TODO- Add tab completion

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        player = (Player) sender;

        if (args.length < 1) {
            sendHelpMessage();
            return true;
        }

        switch (args[0]) {
            case "create" -> createBrand(args);
            case "delete" -> deletePlayerBrand();
            case "invite" -> {
                if (args.length == 1) {
                    player.sendMessage(Util.getMessage("must-specify-invited-player"));
                    return true;
                }
                Player inviteReceiver = Bukkit.getPlayer(args[1]);
                invitePlayerToBrand(inviteReceiver);
            }
            case "accept" -> acceptBrandInvite();
            case "kick" -> {
                if (args.length == 1) {
                    player.sendMessage(Util.getMessage("must-specify-kicked-player"));
                    return true;
                }
                kickPlayerFromBrand(Bukkit.getPlayer(args[1]));
            }
            case "leave" -> leaveBrand();
            case "deny" -> denyRequest();
            case "slogan" -> {
                Brand brand = Util.getPlayerBrand(player);
                String slogan = extractMultiWordString(args);
                changeBrandSlogan(brand, slogan);
            }
            default -> sendHelpMessage();
        }

        return true;
    }

    private void sendHelpMessage() {
        List<String> helpMessage = unitedBrands.getConfig().getStringList("messages.help-command");
        for (String message : helpMessage) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private void createBrand(String[] args) {
        if (args.length == 1) {
            player.sendMessage(Util.getMessage("must-specify-brand-name", ""));
            return;
        }

        String brandName = String.join(" ", extractMultiWordString(args));
        Brand brand = new Brand(unitedBrands, brandName, player, null);

        if (Util.hasBrand(player)) {
            player.sendMessage(Util.getMessage("in-a-brand", Util.getPlayerBrand(player).getBrandName()));
            return;
        }

        if (Util.brandExists(brand)) {
            player.sendMessage(Util.getMessage("brand-already-exists", brandName));
            return;
        }
        brand.createBrand();
    }

    private void deletePlayerBrand() {
        Brand brand = Util.getPlayerBrand(player);

        if (brand == null) {
            player.sendMessage(Util.getMessage("must-own-brand"));
            return;
        }

        String brandName = brand.getBrandName();

        if (Util.hasBrand(player) && isBrandOwner(brand)) {
            brand.deleteBrand();
            player.sendMessage(Util.getMessage("brand-deleted", brandName));
            return;
        }

        player.sendMessage(Util.getMessage("brand-cannot-be-deleted", brandName));
    }

    private void invitePlayerToBrand(Player inviteReceiver) {

        if (inviteReceiver == null) {
            player.sendMessage(Util.getMessage("invalid-player"));
            return;
        }

        Brand brand = Util.getPlayerBrand(player);

        if (brand == null) {
            player.sendMessage(Util.getMessage("must-own-brand"));
            return;
        }

        String brandName = brand.getBrandName();

        if (player == inviteReceiver) {
            player.sendMessage(Util.getMessage("cannot-invite-self", brandName));
            return;
        }

        if (isBrandOwner(brand)) {
            InviteRequest inviteRequest = new InviteRequest(player, inviteReceiver);
            inviteRequests.add(inviteRequest);
            player.sendMessage(Util.getMessage("player-invited", brandName));
            inviteReceiver.sendMessage(Util.getMessage("brand-invite", brandName));
            return;
        }

        player.sendMessage(Util.getMessage("must-own-brand"));

    }

    private void acceptBrandInvite() {
        InviteRequest request = getRequest(player);

        if (request == null) {
            player.sendMessage(Util.getMessage("no-requests"));
            return;
        }

        Player receiver = request.getReceiver();
        Player sender = request.getSender();

        if (Util.getPlayerBrand(receiver) != null) {
            player.sendMessage(Util.getMessage("already-in-a-brand"));
            return;
        }

        Brand brand = Util.getPlayerBrand(sender);
        String brandName = brand.getBrandName();
        brand.addMember(player);

        sender.sendMessage(Util.getMessage("brand-join-sender", player));
        receiver.sendMessage(Util.getMessage("brand-join", brandName));

        inviteRequests.remove(request);
    }

    private void kickPlayerFromBrand(Player kickedPlayer) {

        if (kickedPlayer == null) {
            player.sendMessage(Util.getMessage("must-specify-kicked-player", ""));
            return;
        }

        Brand brand = Util.getPlayerBrand(player);
        String brandName = brand.getBrandName();

        if (!isBrandOwner(brand)) {
            player.sendMessage(Util.getMessage("must-own-brand", brandName));
            return;
        }

        if (kickedPlayer == player) {
            player.sendMessage(Util.getMessage("cannot-kick-self", brandName));
            return;
        }

        brand.removeMember(kickedPlayer);
        kickedPlayer.sendMessage(Util.getMessage("kicked-from-brand", brandName));
        player.sendMessage(Util.getMessage("player-kicked", brandName));
    }

    private void leaveBrand() {
        Brand brand = Util.getPlayerBrand(player);
        if (brand == null) {
            player.sendMessage(Util.getMessage("must-have-brand", ""));
            return;
        }

        String brandName = brand.getBrandName();

        if (isBrandOwner(brand)) {
            player.sendMessage(Util.getMessage("must-delete-brand", brandName));
            return;
        }

        if (Util.hasBrand(player)) {
            Player brandOwner = brand.getBrandOwner();
            brand.removeMember(player);
            player.sendMessage(Util.getMessage("brand-leave", brandName));
            brandOwner.sendMessage(Util.getMessage("player-left-brand", brandName, player));
        }

    }

    private void denyRequest() {
        InviteRequest request = getRequest(player);
        String brandName = Util.getPlayerBrand(player).getBrandName();

        if (request != null) {
            request.getSender().sendMessage(Util.getMessage("brand-deny-sender", player));
            request.getReceiver().sendMessage(Util.getMessage("brand-deny", brandName));
            inviteRequests.remove(request);
            return;
        }
        player.sendMessage(Util.getMessage("no-requests", ""));
    }

    private void changeBrandSlogan(Brand brand, String slogan) {
        if (brand == null) {
            player.sendMessage(Util.getMessage("must-have-brand"));
            return;
        }

        if (!isBrandOwner(brand)) {
            player.sendMessage(Util.getMessage("must-own-brand"));
            return;
        }

        if (slogan == null) {
            player.sendMessage(Util.getMessage("must-specify-slogan"));
        }

        brand.setSlogan(slogan);
        player.sendMessage(Util.getMessage("slogan-changed", brand.getBrandName()));
    }

    private boolean isBrandOwner(Brand brand) {
        if (Util.hasBrand(player)) {
            return brand.getBrandOwner().equals(player);
        }
        return false;
    }

    private InviteRequest getRequest(Player receiver) {
        for (InviteRequest request : inviteRequests) {
            if (request.getReceiver().equals(receiver)) {
                return request;
            }
        }
        return null;
    }

    private String extractMultiWordString(String[] args) {
        String[] arrayWithoutFirstArgument = Arrays.copyOfRange(args, 1, args.length);
        return String.join(" ", arrayWithoutFirstArgument);
    }

}
