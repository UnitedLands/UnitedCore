package org.unitedlands.war.books.declaration;

import com.palmergames.bukkit.towny.object.Town;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.Utils;
import org.unitedlands.war.books.Declarer;
import org.unitedlands.war.books.WarTarget;
import org.unitedlands.war.books.declaration.DeclarationBook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.*;

public class BookGenerator {
    private static final FileConfiguration CONFIG = Utils.getPlugin().getConfig();
    private final DeclarationBook declarationBook;
    private final ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
    private BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

    public BookGenerator(DeclarationBook declarationBook) {
        this.declarationBook = declarationBook;
    }

    public ItemStack generateBook() {
        List<Component> pages = getConfiguredPages();

        BookMeta.BookMetaBuilder builder = bookMeta.toBuilder();
        for (Component page: pages) {
            builder.addPage(page);
        }
        bookMeta = builder
                .author(text(getAuthorName()))
                .title(text("War Declaration Book"))
                .build();

        attachWarData();

        bookItem.setItemMeta(bookMeta);
        return bookItem;
    }

    private List<Component> getConfiguredPages() {
        ConfigurationSection bookSection = CONFIG.getConfigurationSection(declarationBook.getType().name().toLowerCase() + "-declaration-book");
        List<Component> deserializedPages = new ArrayList<>(16);

        for (String key: bookSection.getKeys(false)) {
            List<String> pageLines = bookSection.getStringList(key);
            Component deserializedPage = empty();
            for (String line: pageLines) {
                deserializedPage = deserializedPage
                            .append(parseLine(line))
                            .append(newline());
            }
            deserializedPages.add(deserializedPage);
        }
        // Append the reason at the end.
        deserializedPages.addAll(declarationBook.getReason());
        return deserializedPages;
    }

    @NotNull
    private Component parseLine(String line) {
        Declarer declarer = declarationBook.getDeclarer();
        WarTarget warTarget = declarationBook.getWarTarget();

        String placeholderParsedLine = PlaceholderAPI.setPlaceholders(declarer.getDeclaringPlayer(), line);
        Town town = declarer.getTown();
        Town targetTown = warTarget.getTown();
        return UnitedWars.MINI_MESSAGE.deserialize(placeholderParsedLine,
                placeholder("town-name", town.getName()),
                placeholder("target-name", targetTown.getName()),
                placeholderStat("town-balance"),
                placeholderStat("town-blocks"),
                placeholderStat("town-residents"),
                placeholderStat("target-balance"),
                placeholderStat("target-blocks"),
                placeholderStat("target-residents"));
    }


    private TagResolver.@NotNull Single placeholder(String name, String output) {
        return Placeholder.component(name, text(output));
    }

    private TagResolver.Single placeholderStat(String name) {
        if (declarationBook.getType().isTownWar()) {
            String[] split = name.split("-");
            String type = split[0];
            String stat = split[1];

            Town town = null;
            if (type.equals("town"))
                town = declarationBook.getDeclarer().getTown();
            else if (type.equals("target"))
                town = declarationBook.getWarTarget().getTown();
            return placeholder(name, getStat(stat, town));
        }
        return placeholder(name, "0");
    }

    private String getStat(String statName, Town town) {
        switch (statName) {
            case "blocks" -> town.getNumTownBlocks();
            case "residents" -> town.getNumResidents();
            case "balance" -> town.getAccount().getCachedBalance();
        }
        return "0";
    }


    public void attachWarData() {
        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();
        UUID townUUID = declarationBook.getDeclarer().getTown().getUUID();
        UUID targetUUID = declarationBook.getWarTarget().getTown().getUUID();
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.town"), PersistentDataType.STRING, townUUID.toString());
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.type"), PersistentDataType.STRING, declarationBook.getType().name());
        pdc.set(NamespacedKey.fromString("unitedwars.book.target"), PersistentDataType.STRING, targetUUID.toString());
    }

    private String getAuthorName() {
        switch (declarationBook.getType().name()) {
            case "TOWNWAR" -> declarationBook.getDeclarer().getTown().getFormattedName();
            case "NATIONWAR" -> declarationBook.getDeclarer().getNation().getFormattedName();
            default -> declarationBook.getDeclarer().getDeclaringPlayer().name();
        }
        return declarationBook.getDeclarer().getDeclaringPlayer().getName();
    }

}
