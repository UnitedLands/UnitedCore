package org.unitedlands.wars.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.books.declaration.DeclarationWarBook;
import org.unitedlands.wars.books.declaration.NationDeclarationBook;
import org.unitedlands.wars.books.declaration.TownDeclarationBook;
import org.unitedlands.wars.books.warbooks.WritableDeclaration;
import org.unitedlands.wars.events.WarDeclareEvent;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;

import java.util.*;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.upkeep.util.NationMetaController.isOfficialNation;
import static org.unitedlands.wars.Utils.*;
import static org.unitedlands.wars.war.WarUtil.*;

public class DeclareCommandParser {
    private static final NamespacedKey TYPE_KEY = getKey("book.type");
    private final CommandSender sender;

    public DeclareCommandParser(CommandSender sender) {
        this.sender = sender;
    }

    public void parse() {
        WarType warType = getWarType();
        if (warType == null)
            return;

        switch (warType.name().toLowerCase()) {
            case "townwar" -> parseTownWar();
            case "nationwar" -> parseNationWar();
            default -> getMessageList("surrender-help").forEach(sender::sendMessage);
        }
    }

    private void parseTownWar() {
        Player player = (Player) this.sender;
        WarTarget target = getTargetFromBook();
        if (target == null) {
            player.sendMessage(getMessage("invalid-held-book"));
            return;
        }
        Town targetTown = target.town();
        if (targetTown == null) {
            player.sendMessage(getMessage("invalid-town-name"));
            return;
        }
        Resident resident = UnitedWars.TOWNY_API.getResident(player);
        if (!resident.isMayor()) {
            player.sendMessage(getMessage("must-be-mayor"));
            return;
        }
        Confirmation.runOnAccept(() -> {
            if (invalidBook(WarType.TOWNWAR))
                return;

            if (targetTown.isNeutral()) {
                player.sendMessage(getMessage("must-not-be-neutral-target"));
                return;
            }
            Town town = resident.getTownOrNull();
            if (!townsHaveEnoughOnline(targetTown, town)) {
                player.sendMessage(getMessage("must-have-online-player"));
                return;
            }
            List<Town> towns = new ArrayList<>();
            HashSet<Resident> residents = new HashSet<>();
            towns.add(town);
            towns.add(targetTown);
            residents.addAll(town.getResidents());
            residents.addAll(targetTown.getResidents());
            if (targetTown.hasNation()) {
                residents.addAll(targetTown.getNationOrNull().getResidents());
            }

            new War(towns, null, residents, WarType.TOWNWAR);

            WarDeclareEvent wde = new WarDeclareEvent(getWarHeldBook());
            Bukkit.getServer().getPluginManager().callEvent(wde);

            removeHeldBook(player);
        }).setTitle(getConfirmationTitle(WarType.TOWNWAR, targetTown.getFormattedName())).sendTo(player);
    }

    private void parseNationWar() {
        Player player = (Player) this.sender;
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (!resident.hasNation()) {
            player.sendMessage(getMessage("must-have-nation"));
            return;
        }
        if (!resident.isKing()) {
            player.sendMessage(getMessage("must-be-mayor"));
            return;
        }
        Nation declaringNation = resident.getNationOrNull();
        WarTarget target = getTargetFromBook();
        if (target == null) {
            player.sendMessage(getMessage("invalid-held-book"));
            return;
        }
        Nation targetNation = target.nation();
        if (targetNation == null) {
            player.sendMessage(getMessage("invalid-nation-name"));
            return;
        }

        // Commented out to fix compilation error due to changed method arguments in UnitedUpkeep 2.3.1 

        // if ((isOfficialNation(declaringNation) && !isOfficialNation(targetNation))) {
        //     player.sendMessage(getMessage("cannot-declare-official-nation"));
        //     return;
        // }

        Confirmation.runOnAccept(() -> {
            if (invalidBook(WarType.NATIONWAR))
                return;

            if (targetNation.isNeutral()) {
                player.sendMessage(getMessage("must-not-be-neutral-target"));
                return;
            }
            if (!nationsHaveEnoughOnline(targetNation, declaringNation)) {
                player.sendMessage(getMessage("must-have-online-player"));
                return;
            }
            List<Nation> nations = new ArrayList<>();
            HashSet<Resident> residents = new HashSet<>();
            nations.add(declaringNation);
            nations.add(targetNation);

            residents.addAll(declaringNation.getResidents());
            residents.addAll(targetNation.getResidents());

            new War(null, nations, residents, WarType.NATIONWAR);

            WarDeclareEvent wde = new WarDeclareEvent(getWarHeldBook());
            Bukkit.getServer().getPluginManager().callEvent(wde);

            removeHeldBook(player);
        }).setTitle(getConfirmationTitle(WarType.NATIONWAR, targetNation.getFormattedName())).sendTo(player);

    }

    private WarTarget getTargetFromBook() {
        PersistentDataContainer pdc = getHeldBookData();
        if (pdc == null)
            return null;
        NamespacedKey targetKey = getKey("book.target");

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

    private DeclarationWarBook getWarHeldBook() {
        Player player = (Player) sender;
        ItemStack book = player.getInventory().getItemInMainHand();

        if (book.getType() != Material.WRITTEN_BOOK) return null;
        BookMeta meta = (BookMeta) book.getItemMeta();
        WritableDeclaration writableDeclaration = generateWritableDeclaration(meta);
        if (writableDeclaration.getWarType() == WarType.TOWNWAR) {
            return new TownDeclarationBook(writableDeclaration);
        } else {
            return new NationDeclarationBook(writableDeclaration);
        }
    }

    private PersistentDataContainer getHeldBookData() {
        Player player = (Player) this.sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.WRITTEN_BOOK) {
            player.sendMessage(getMessage("invalid-held-book"));
            return null;
        }
        if (item.getItemMeta() == null) {
            player.sendMessage(getMessage("invalid-held-book"));
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer();
    }

    private WarType getWarType() {
        PersistentDataContainer pdc = getHeldBookData();
        if (pdc == null) {
            sender.sendMessage(getMessage("invalid-held-book"));
            return null;
        }

        String storedTypeName = pdc.get(TYPE_KEY, PersistentDataType.STRING);
        try {
            return WarType.valueOf(storedTypeName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sender.sendMessage(getMessage("invalid-declaration-book"));
        return null;
    }

    private boolean isNationWarBook(PersistentDataContainer pdc) {
        return pdc.get(TYPE_KEY, PersistentDataType.STRING).equalsIgnoreCase("NATIONWAR");
    }

    private boolean isTownWarBook(PersistentDataContainer pdc) {
        return pdc.get(TYPE_KEY, PersistentDataType.STRING).equalsIgnoreCase("TOWNWAR");
    }

    private boolean invalidBook(WarType wartype) {
        Player player = (Player) this.sender;
        Resident resident = UnitedWars.TOWNY_API.getResident(player);
        if (resident == null)
            return true;
        if (!resident.hasTown()) {
            player.sendMessage(getMessage("must-have-town"));
            return true;
        }
        Town town = resident.getTownOrNull();
        if (town == null)
            return true;
        if (WarDatabase.hasWar(town)) {
            player.sendMessage(getMessage("ongoing-war"));
            return true;
        } else if (!isNotOnCooldownForWar(WarType.TOWNWAR, town)) {
            player.sendMessage(getMessage("on-cooldown"));
            return true;
        }
        ItemStack book = player.getInventory().getItemInMainHand();
        if (!book.getType().equals(Material.WRITTEN_BOOK)) {
            player.sendMessage(getMessage("invalid-held-book"));
            return true;
        }
        PersistentDataContainer pdc = book.getItemMeta().getPersistentDataContainer();
        if (pdc.isEmpty()) {
            player.sendMessage(getMessage("invalid-held-book"));
            return true;
        }
        if (!pdc.has(TYPE_KEY, PersistentDataType.STRING)) {
            player.sendMessage(getMessage("invalid-held-book"));
            return true;
        }
        String type = pdc.get(TYPE_KEY, PersistentDataType.STRING);
        if (type == null)
            return true;
        if (!type.equalsIgnoreCase(wartype.name())) {
            player.sendMessage(getMessage("invalid-held-book"));
            return true;
        }
        Town townWhoBoughtDOW = getDeclarationBuyer(player);
        if (townWhoBoughtDOW == null) {
            player.sendMessage(getMessage("invalid-held-book"));
            return true;
        } else if (!resident.getTownOrNull().equals(townWhoBoughtDOW)) {
            player.sendMessage(getMessage("must-be-book-owner"));
            return true;
        }
        return false; // all checks passed.
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
        return townHasEnoughOnline(targetTown) && townHasEnoughOnline(town);
    }

    private boolean nationsHaveEnoughOnline(Nation targetNation, Nation nation) {
        return nationHasEnoughOnline(targetNation) && nationHasEnoughOnline(nation);
    }

    private Town getDeclarationBuyer(Player player) {
        ItemMeta bookMeta = player.getInventory().getItemInMainHand().getItemMeta();
        NamespacedKey townKey = getKey("book.town");
        return UnitedWars.TOWNY_API.getTown(UUID.fromString(bookMeta.getPersistentDataContainer().get(townKey, PersistentDataType.STRING)));
    }

    private Translatable getConfirmationTitle(WarType warType, String name) {
        String message = getMessageRaw("war-declare-confirmation")
                .replace("<target>", String.valueOf(name))
                .replace("<type>", warType.getFormattedName());
        return Translatable.of(message);
    }
}
