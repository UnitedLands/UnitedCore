package org.unitedlands.war.books.generators;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.unitedlands.war.books.declaration.DeclarationWarBook;

import java.util.List;
import java.util.UUID;

public class DeclarationGenerator extends BookGenerator {
    private final DeclarationWarBook declarationBook;
    private ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
    private final BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

    public DeclarationGenerator(DeclarationWarBook declarationBook) {
        super(declarationBook);
        this.declarationBook = declarationBook;
    }

    @Override
    public ItemStack generateBook() {
        bookItem = super.generateBook();
        // Add custom model data
        bookMeta.setCustomModelData(2);
        // Change author and title
        bookMeta.setAuthor(getName("declarer"));
        bookMeta.setTitle("War Declaration Book");
        // Attach extra war data.
        attachWarData();
        bookItem.setItemMeta(bookMeta);
        return bookItem;
    }

    @Override
    protected List<Component> getConfiguredPages() {
        List<Component> pages = super.getConfiguredPages();
        pages.addAll(declarationBook.getReason());
        return pages;
    }

    public void attachWarData() {
        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();
        UUID townUUID = declarationBook.getDeclarer().town().getUUID();
        UUID targetUUID = declarationBook.getWarTarget().uuid();
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.town"), PersistentDataType.STRING, townUUID.toString());
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.type"), PersistentDataType.STRING, declarationBook.getType().name());
        pdc.set(NamespacedKey.fromString("unitedwars.book.target"), PersistentDataType.STRING, targetUUID.toString());
    }


}
