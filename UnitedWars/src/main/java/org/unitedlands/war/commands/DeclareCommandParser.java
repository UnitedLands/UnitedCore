package org.unitedlands.war.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import io.github.townyadvanced.eventwar.events.EventWarDeclarationEvent;
import io.github.townyadvanced.eventwar.instance.War;
import io.github.townyadvanced.eventwar.objects.DeclarationOfWar;
import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import io.github.townyadvanced.eventwar.settings.EventWarSettings;
import io.github.townyadvanced.eventwar.util.WarUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.books.data.WarTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.war.Utils.getMessage;

public class DeclareCommandParser {
    private final CommandSender sender;
    private static final NamespacedKey TYPE_KEY = NamespacedKey.fromString("eventwar.dow.book.type");

    public DeclareCommandParser(CommandSender sender) {
        this.sender = sender;
    }

    public void parseDeclareCommand() {
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
                    parseNationWar();
                } catch (TownyException e) {
                    TownyMessaging.sendErrorMsg(sender, e.getMessage());
                }
            }
        }
    }

    private void parseTownWar() throws TownyException {
        Player player = (Player) this.sender;
        Town targetTown = getTargetFromBook().town();
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
                return;
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

    private void parseNationWar() throws TownyException {
        Player player = (Player) this.sender;
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (!resident.hasNation()) {
            throw new TownyException(Translatable.of("msg_err_dont_belong_nation"));
        }
        Nation declaringNation = resident.getNationOrNull();
        Nation targetNation = getTargetFromBook().nation();
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
            if (!nationsHaveEnoughOnline(targetNation, declaringNation)) {
                TownyMessaging.sendErrorMsg(this.sender, new Translatable[]{Translatable.of("msg_err_not_enough_people_online_for_nationwar", EventWarSettings.nationWarMinOnline())});
                return;
            }
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
        return null;
    }

    private boolean isNationWarBook(PersistentDataContainer pdc) {
        return pdc.get(TYPE_KEY, PersistentDataType.STRING).equalsIgnoreCase("NATIONWAR");
    }

    private boolean isTownWarBook(PersistentDataContainer pdc) {
        return pdc.get(TYPE_KEY, PersistentDataType.STRING).equalsIgnoreCase("TOWNWAR");
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
        if (!pdc.has(TYPE_KEY, PersistentDataType.STRING)) {
            throw new TownyException(Translatable.of("msg_err_you_are_not_holding_dow"));
        }
        String type = pdc.get(TYPE_KEY, PersistentDataType.STRING);
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
        ItemStack bookCopy = createBookCopy(playerHand);
        if (playerHand.getAmount() == 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            playerHand.setAmount(playerHand.getAmount() - 1);
        }
        // Add the copy without any PDC. For History purposes.
        player.getInventory().addItem(bookCopy);
    }

    private ItemStack createBookCopy(ItemStack book) {
        ItemMeta copyMeta = book.getItemMeta();
        Set<NamespacedKey> keys = copyMeta.getPersistentDataContainer().getKeys();
        keys.forEach(key -> copyMeta.getPersistentDataContainer().remove(key));
        copyMeta.displayName(copyMeta.displayName().append(text(" (Artifact)").color(NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
        book.setItemMeta(copyMeta);
        return book;
    }

    private boolean townsHaveEnoughOnline(Town targetTown, Town town) {
        return WarUtil.townHasEnoughOnline(targetTown, WarTypeEnum.TOWNWAR.getType()) && WarUtil.townHasEnoughOnline(town, WarTypeEnum.TOWNWAR.getType());
    }

    private boolean nationsHaveEnoughOnline(Nation targetNation, Nation nation) {
        return WarUtil.nationHasEnoughOnline(targetNation, WarTypeEnum.NATIONWAR.getType()) && WarUtil.nationHasEnoughOnline(nation, WarTypeEnum.NATIONWAR.getType());
    }

    private Town getDOWPurchaser(Player player) {
        ItemMeta bookMeta = player.getInventory().getItemInMainHand().getItemMeta();
        NamespacedKey townKey = NamespacedKey.fromString("eventwar.dow.book.town");
        return UnitedWars.TOWNY_API.getTown(UUID.fromString(bookMeta.getPersistentDataContainer().get(townKey, PersistentDataType.STRING)));
    }
}
