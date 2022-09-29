package org.unitedlands.war.listeners;

import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.books.Declarer;
import org.unitedlands.war.books.WarTarget;
import org.unitedlands.war.books.WritableDeclaration;
import org.unitedlands.war.books.declaration.DeclarationBook;
import org.unitedlands.war.books.declaration.NationDeclarationBook;
import org.unitedlands.war.books.declaration.TownDeclarationBook;

import java.util.List;
import java.util.UUID;

public class BookListener implements Listener {
    private final UnitedWars unitedWars;

    public BookListener(UnitedWars unitedWars) {
        this.unitedWars = unitedWars;
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        boolean isWritableDeclaration = WritableDeclaration.isWritableDeclaration(event.getPreviousBookMeta().getPersistentDataContainer());
        if (!isWritableDeclaration) return;
        BookMeta bookMeta = event.getNewBookMeta();
        WritableDeclaration writableDeclaration = generateFromMeta(bookMeta);
        // Extract the pages from the new book the player edited
        List<Component> extractedReason = bookMeta.pages();
        // Set the reason of the declaration to the new pages.
        writableDeclaration.setReason(extractedReason);

        // Generate the sealed book.
        WarType type = writableDeclaration.getWarType();
        DeclarationBook declarationBook = null;
        if (type.isTownWar()) {
            declarationBook = new TownDeclarationBook(writableDeclaration);
        } else if (type.isNationWar()) {
            declarationBook = new NationDeclarationBook(writableDeclaration);
        }


        Player player = event.getPlayer();
        event.setCancelled(true);
        // Replace the held item, the old book, with the new sealed book to be used for declaration.
        player.getInventory().setItemInMainHand(declarationBook.getBook());
    }

    private WritableDeclaration generateFromMeta(BookMeta bookMeta) {
        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();

        UUID declaringUUID = getUUID(pdc, "eventwar.dow.book.town");
        Declarer declarer = new Declarer(UnitedWars.TOWNY_API.getTown(declaringUUID));


        WarType warType = WarTypeEnum.valueOf(pdc.get(NamespacedKey.fromString("eventwar.dow.book.type"), PersistentDataType.STRING).toUpperCase()).getType();
        UUID targetUUID = getUUID(pdc, "unitedwars.book.target");
        WarTarget warTarget = getWarTarget(warType, targetUUID);

        return new WritableDeclaration(declarer, warTarget, warType);
    }

    @NotNull
    private UUID getUUID(PersistentDataContainer pdc, String key) {
        return UUID.fromString(pdc.get(NamespacedKey.fromString(key), PersistentDataType.STRING));
    }


    @NotNull
    private WarTarget getWarTarget(WarType warType, UUID targetUUID) {
        WarTarget warTarget;
        if (warType.isTownWar())
            warTarget = new WarTarget(UnitedWars.TOWNY_API.getTown(targetUUID));
        else
            warTarget = new WarTarget(UnitedWars.TOWNY_API.getNation(targetUUID));
        return warTarget;
    }
}
