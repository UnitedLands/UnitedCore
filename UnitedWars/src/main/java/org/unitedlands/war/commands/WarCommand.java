package org.unitedlands.war.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.NameUtil;
import io.github.townyadvanced.eventwar.db.WarMetaDataController;
import io.github.townyadvanced.eventwar.events.EventWarDeclarationEvent;
import io.github.townyadvanced.eventwar.instance.War;
import io.github.townyadvanced.eventwar.objects.DeclarationOfWar;
import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import io.github.townyadvanced.eventwar.settings.EventWarSettings;
import io.github.townyadvanced.eventwar.util.WarUtil;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.books.Declarer;
import org.unitedlands.war.books.WarTarget;
import org.unitedlands.war.books.WritableDeclaration;

import java.util.*;

import static org.unitedlands.war.Utils.*;

public class WarCommand implements TabExecutor {
    private static final List<String> warTabCompletes = Arrays.asList("declare", "book");
    private static final List<String> optionsTabCompletes = Arrays.asList("town", "nation");
    private CommandSender sender;

    public WarCommand() {
        AddonCommand warCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN, "war", this);
        TownyCommandAddonAPI.addSubCommand(warCommand);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args[0].equalsIgnoreCase("book")) {
            if (args.length == 2) {
                return NameUtil.filterByStart(optionsTabCompletes, args[1]);
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("town")) {
                    return BaseCommand.getTownyStartingWith(args[2], "t");
                }

                if (args[1].equalsIgnoreCase("nation")) {
                    return BaseCommand.getTownyStartingWith(args[2], "n");
                }
                return Collections.emptyList();
            }
        }
        return args.length == 1 ? NameUtil.filterByStart(warTabCompletes, args[0]) : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            sender = commandSender;

            if (args[0].equalsIgnoreCase("declare")) {
                parseDeclareCommand();
            }

            if (args[0].equalsIgnoreCase("book")) {
                // [t war] book (1) town (2) Cheese (3)
                if (args.length < 3) {
                    sender.sendMessage(getMessage("must-specify-target"));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "town" -> parseTownBookCreationCommand(args[2]);
                    case "nation" -> parseNationBookCreationCommand(args[2]);
                }
            }
        }
        return true;
    }

    private void parseDeclareCommand() {
        WarType warType = getWarType();
        if (warType == null) {
            sender.sendMessage(getMessage("invalid-declaration-book"));
            return;
        }
        switch (warType.name().toLowerCase()) {
            case "townwar" -> {
                try {
                    parseTownWar();
                } catch (TownyException e) {
                    TownyMessaging.sendErrorMsg(sender, e.getMessage());
                }
            }
            case "nationwar" -> {
                try {
                    parseNationWarCommand();
                } catch (TownyException e) {
                    TownyMessaging.sendErrorMsg(sender, e.getMessage());
                }
            }
        }
    }

    private void parseTownBookCreationCommand(@NotNull String target) {
        Player player = (Player) sender;
        Resident resident = getTownyResident(player);
        if (!resident.hasTown()) {
            player.sendMessage(getMessage("must-have-town"));
            return;
        }
        if (!resident.isMayor()) {
            player.sendMessage(getMessage("must-be-mayor"));
            return;
        }
        Town declaringTown = getPlayerTown(player);
        if (declaringTown.isNeutral()) {
            player.sendMessage(getMessage("must-not-be-neutral"));
            return;
        }

        if (declaringTown.hasActiveWar()) {
            player.sendMessage(getMessage("ongoing-war"));
            return;
        }

        Town targetTown = UnitedWars.TOWNY_API.getTown(target);
        if (targetTown == null) {
            player.sendMessage(getMessage("invalid-town-name"));
            return;
        }

        if (targetTown.isNeutral()) {
            player.sendMessage(getMessage("must-not-be-neutral-target"));
            return;
        }

        if (targetTown.hasActiveWar()) {
            player.sendMessage(getMessage("ongoing-war-target"));
            return;
        }

        WarType type = WarTypeEnum.TOWNWAR.getType();
        int cost = type.tokenCost();
        TextReplacementConfig costReplacer = TextReplacementConfig.builder().match("<cost>").replacement(String.valueOf(cost)).build();

        if (WarMetaDataController.getWarTokens(declaringTown) < cost) {
            player.sendMessage(getMessage("not-enough-tokens").replaceText(costReplacer));
            return;
        }

        Confirmation.runOnAccept(() -> {
            if (WarMetaDataController.getWarTokens(declaringTown) < cost) {
                player.sendMessage(getMessage("not-enough-tokens").replaceText(costReplacer));
                return;
            }
            WritableDeclaration writableDeclaration = new WritableDeclaration(new Declarer(player), new WarTarget(targetTown), type);
            player.getInventory().addItem(writableDeclaration.getWritableBook());

            int remainder = WarMetaDataController.getWarTokens(declaringTown) - cost;
            WarMetaDataController.setTokens(declaringTown, remainder);
            TownyMessaging.sendPrefixedTownMessage(declaringTown, Translatable.of("msg_town_purchased_declaration_of_type", declaringTown, type.name()));

        }).setTitle(Translatable.of("msg_you_are_about_to_purchase_a_declaration_of_war_of_type_for_x_tokens", type.name(), cost)).sendTo(player);
    }

    private void parseNationBookCreationCommand(@NotNull String target) {

    }

    // Heavily copied from EventWar as to not break shit. Thanks Llmdl, xoxo.
    private void parseTownWar() throws TownyException {
        Player player = (Player) this.sender;
        Town targetTown = getTargetFromBook().getTown();
        if (targetTown == null) {
            throw new TownyException(Translatable.of("msg_invalid_name"));
        }
        Confirmation.runOnAccept(() -> {
            try {
                this.testBookRequirementsAreMet(WarTypeEnum.TOWNWAR);
            } catch (TownyException exception) {
                TownyMessaging.sendErrorMsg(this.sender, exception.getMessage());
                return;
            }

            if (targetTown.isNeutral()) {
                TownyMessaging.sendErrorMsg(this.sender, new Translatable[]{Translatable.of("msg_err_cannot_declare_war_on_neutral")});
                return;
            }
            Resident resident = UnitedWars.TOWNY_API.getResident(player);
            Town town = resident.getTownOrNull();
            if (!townsHaveEnoughOnline(targetTown, town)) {
                TownyMessaging.sendErrorMsg(this.sender, new Translatable[]{Translatable.of("msg_err_not_enough_people_online_for_townwar", EventWarSettings.townWarMinOnline())});
            }
            List<Town> towns = new ArrayList<>();
            List<Resident> residents = new ArrayList<>();
            towns.add(town);
            towns.add(targetTown);
            residents.addAll(town.getResidents());
            residents.addAll(targetTown.getResidents());
            DeclarationOfWar dow = new DeclarationOfWar(player, WarTypeEnum.TOWNWAR.getType(), getDOWPurchaser(player));
            EventWarDeclarationEvent ewde = new EventWarDeclarationEvent(dow, null, towns, residents);
            Bukkit.getServer().getPluginManager().callEvent(ewde);
            if (ewde.isCancelled()) {
                return;
            }
            new War(null, towns, residents, WarTypeEnum.TOWNWAR.getType(), dow);
            removeHeldBook(player);
        }).setTitle(Translatable.of("player_are_you_sure_you_want_to_start_a_townwar", targetTown)).sendTo(player);
    }

    private void parseNationWarCommand() throws TownyException {
        Player player = (Player) this.sender;
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (!resident.hasNation()) {
            throw new TownyException(Translatable.of("msg_err_dont_belong_nation"));
        }
        Nation declaringNation = resident.getNationOrNull();
        Nation targetNation = getTargetFromBook().getNation();
        if (targetNation == null) {
            throw new TownyException(Translatable.of("msg_target_has_no_nation"));
        }
        Confirmation.runOnAccept(() -> {
            try {
                this.testBookRequirementsAreMet(WarTypeEnum.NATIONWAR);
            } catch (TownyException var10) {
                TownyMessaging.sendErrorMsg(this.sender, var10.getMessage());
                return;
            }

            if (targetNation.isNeutral()) {
                TownyMessaging.sendErrorMsg(this.sender, new Translatable[]{Translatable.of("msg_err_cannot_declare_war_on_neutral")});
                return;
            }
            if (WarUtil.nationHasEnoughOnline(targetNation, WarTypeEnum.NATIONWAR.getType()) && WarUtil.nationHasEnoughOnline(declaringNation, WarTypeEnum.NATIONWAR.getType())) {
                List<Nation> nations = new ArrayList<>();
                List<Resident> residents = new ArrayList<>();
                nations.add(declaringNation);
                nations.add(targetNation);

                residents.addAll(declaringNation.getResidents());
                residents.addAll(targetNation.getResidents());
                DeclarationOfWar dow = new DeclarationOfWar(player, WarTypeEnum.NATIONWAR.getType(), this.getDOWPurchaser(player));
                EventWarDeclarationEvent ewde = new EventWarDeclarationEvent(dow, nations, null, residents);
                Bukkit.getServer().getPluginManager().callEvent(ewde);
                if (ewde.isCancelled()) {
                    return;
                }
                new War(nations, null, residents, WarTypeEnum.NATIONWAR.getType(), dow);
                removeHeldBook(player);
            } else {
                TownyMessaging.sendErrorMsg(this.sender, new Translatable[]{Translatable.of("msg_err_not_enough_people_online_for_nationwar", EventWarSettings.nationWarMinOnline())});
            }
        }).setTitle(Translatable.of("player_are_you_sure_you_want_to_start_a_nationwar", targetNation)).sendTo(player);

    }

    private WarTarget getTargetFromBook() {
        PersistentDataContainer pdc = getHeldBookData();
        NamespacedKey targetKey = NamespacedKey.fromString("unitedwars.book.target");

        if (pdc.has(targetKey)) {
            UUID targetUUID = UUID.fromString(pdc.get(targetKey, PersistentDataType.STRING));
            if (isTownWarBook(pdc)) {
                return new WarTarget(UnitedWars.TOWNY_API.getTown(targetUUID));
            } else if (isNationWarBook(pdc)) {
                return new WarTarget(UnitedWars.TOWNY_API.getNation(targetUUID));
            }
        }
        return null;
    }

    @NotNull
    private PersistentDataContainer getHeldBookData() {
        Player player = (Player) this.sender;
        ItemStack book = player.getInventory().getItemInMainHand();
        return book.getItemMeta().getPersistentDataContainer();
    }

    private WarType getWarType() {
       PersistentDataContainer pdc = getHeldBookData();
       String storedTypeName = pdc.get(NamespacedKey.fromString("eventwar.dow.book.type"), PersistentDataType.STRING);
        try {
            return WarTypeEnum.parseType(storedTypeName).getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Default to town war
        return null;
    }

    private boolean isNationWarBook(PersistentDataContainer pdc) {
        return pdc.get(getTypeKey(), PersistentDataType.STRING).equalsIgnoreCase("NATIONWAR");
    }

    private boolean isTownWarBook(PersistentDataContainer pdc) {
        return pdc.get(getTypeKey(), PersistentDataType.STRING).equalsIgnoreCase("TOWNWAR");
    }

    private void testBookRequirementsAreMet(WarTypeEnum wartype) throws TownyException {
        Player player = (Player) this.sender;
        Resident resident = UnitedWars.TOWNY_API.getResident(player);
        if (!resident.hasTown()) {
            throw new TownyException(Translatable.of("msg_err_must_belong_town"));
        }
        List<String> error = new ArrayList<>(1);
        if (!WarUtil.isTownAllowedToWar(resident.getTownOrNull(), error, wartype.getType())) {
            throw new TownyException(error.get(0));
        }
        ItemStack book = player.getInventory().getItemInMainHand();
        if (!book.getType().equals(Material.WRITTEN_BOOK)) {
            throw new TownyException(Translatable.of("msg_err_you_are_not_holding_dow"));
        }
        PersistentDataContainer pdc = book.getItemMeta().getPersistentDataContainer();
        if (pdc.isEmpty()) {
            throw new TownyException(Translatable.of("msg_err_you_are_not_holding_dow"));
        }
        if (!pdc.has(getTypeKey(), PersistentDataType.STRING)) {
            throw new TownyException(Translatable.of("msg_err_you_are_not_holding_dow"));
        }
        String type = pdc.get(getTypeKey(), PersistentDataType.STRING);
        if (!type.equalsIgnoreCase(wartype.name())) {
            throw new TownyException(Translatable.of("msg_err_you_are_not_holding_correct_dow"));
        }
        Town townWhoBoughtDOW = getDOWPurchaser(player);
        if (townWhoBoughtDOW == null) {
            throw new TownyException(Translatable.of("msg_err_dow_owner_no_longer_exists"));
        } else if (!resident.getTownOrNull().equals(townWhoBoughtDOW)) {
            throw new TownyException(Translatable.of("msg_err_dow_owner_not_your_town"));
        }

    }

    private void removeHeldBook(Player player) {
        ItemStack playerHand = player.getInventory().getItemInMainHand();
        if (playerHand.getAmount() == 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            playerHand.setAmount(playerHand.getAmount() - 1);
        }
    }

    @Nullable
    private NamespacedKey getTypeKey() {
        return NamespacedKey.fromString("eventwar.dow.book.type");
    }

    @Nullable
    private static NamespacedKey getTownKey() {
        return NamespacedKey.fromString("eventwar.dow.book.town");
    }

    private boolean townsHaveEnoughOnline(Town targetTown, Town town) {
        return WarUtil.townHasEnoughOnline(targetTown, WarTypeEnum.TOWNWAR.getType()) && WarUtil.townHasEnoughOnline(town, WarTypeEnum.TOWNWAR.getType());
    }

    private Town getDOWPurchaser(Player player) {
        ItemMeta bookMeta = player.getInventory().getItemInMainHand().getItemMeta();
        return UnitedWars.TOWNY_API.getTown(UUID.fromString(bookMeta.getPersistentDataContainer().get(getTownKey(), PersistentDataType.STRING)));
    }
}
