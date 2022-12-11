package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.object.Resident;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.declaration.DeclarationWarBook;
import org.unitedlands.wars.books.declaration.NationDeclarationBook;
import org.unitedlands.wars.books.declaration.TownDeclarationBook;
import org.unitedlands.wars.books.warbooks.WritableDeclaration;
import org.unitedlands.wars.war.WarType;

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.getMessage;
import static org.unitedlands.wars.Utils.getTownyResident;
import static org.unitedlands.wars.war.WarUtil.generateWritableDeclaration;

public class BookListener implements Listener {
    private final UnitedWars unitedWars;

    public BookListener(UnitedWars unitedWars) {
        this.unitedWars = unitedWars;
    }

    private static boolean isWritableDeclaration(ItemMeta meta) {
        return WritableDeclaration.isWritableDeclaration(meta.getPersistentDataContainer());
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        if (!isWritableDeclaration(event.getPreviousBookMeta()))
            return;
        if (!event.isSigning())
            return;

        BookMeta bookMeta = event.getNewBookMeta();
        WritableDeclaration writableDeclaration = generateWritableDeclaration(bookMeta);

        // Extract the pages from the new book the player edited
        List<Component> extractedReason = bookMeta.pages();
        // Set the reason of the declaration to the new pages.
        writableDeclaration.setReason(extractedReason);

        // Generate the sealed book.
        WarType type = writableDeclaration.getWarType();
        DeclarationWarBook declarationBook;
        if (type == WarType.TOWNWAR)
            declarationBook = new TownDeclarationBook(writableDeclaration);
        else
            declarationBook = new NationDeclarationBook(writableDeclaration);

        // Replace the held item, the old book, with the new sealed book to be used for declaration.
        DeclarationWarBook finalDeclarationBook = declarationBook;
        unitedWars.getServer().getScheduler().runTask(unitedWars, () -> event.getPlayer().getInventory().setItemInMainHand(finalDeclarationBook.getBook()));
    }

    private boolean canWriteBook(Resident resident, WritableDeclaration writableDeclaration) {
        return resident.getTownOrNull().equals(writableDeclaration.getDeclarer().town()) && resident.isMayor();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        if (!isWritableDeclaration(item.getItemMeta())) return;

        Resident resident = getTownyResident(event.getPlayer());
        WritableDeclaration writableDeclaration = generateWritableDeclaration((BookMeta) item.getItemMeta());

        if (!canWriteBook(resident, writableDeclaration)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(getMessage("only-mayors-can-sign", Placeholder.component("declarer-name", text(writableDeclaration.getDeclarerName()))));
            event.getPlayer().closeInventory();
        }
    }
}
