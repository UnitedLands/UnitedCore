package org.unitedlands.war.books.declaration;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.eventwar.objects.WarType;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
                .author(text(getName("declarer")))
                .title(text("War Declaration Book"))
                .build();
        bookMeta.displayName(getDisplayName());
        bookMeta.setCustomModelData(1);
        attachWarData();
        bookItem.setItemMeta(bookMeta);
        return bookItem;
    }

    @NotNull
    private Component getDisplayName() {
        return parseLine(CONFIG.getString("declaration-book-name"));
    }

    private List<Component> getConfiguredPages() {
        ConfigurationSection bookSection = CONFIG.getConfigurationSection(declarationBook.getType().name().toLowerCase() + "-declaration-book");
        List<Component> deserializedPages = new ArrayList<>(16);

        for (String key : bookSection.getKeys(false)) {
            List<String> pageLines = bookSection.getStringList(key);
            Component deserializedPage = empty();
            for (String line : pageLines) {
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
        String placeholderParsedLine = PlaceholderAPI.setPlaceholders(declarer.getDeclaringPlayer(), line);

        return UnitedWars.MINI_MESSAGE.deserialize(placeholderParsedLine,
                placeholder("declarer-name", getName("declarer")),
                placeholder("target-name", getName("target")),
                placeholder("declaring-mayor", declarer.getDeclaringPlayer().getName()),
                placeholderStat("declarer-balance"),
                placeholderStat("declarer-blocks"),
                placeholderStat("declarer-residents"),
                placeholderStat("target-balance"),
                placeholderStat("target-blocks"),
                placeholderStat("target-residents"));
    }


    private TagResolver.@NotNull Single placeholder(String name, String output) {
        return Placeholder.component(name, text(output));
    }

    private TagResolver.Single placeholderStat(String name) {
        if (declarationBook.getType().isNationWar()) {
            String[] split = name.split("-");
            String type = split[0];
            String stat = split[1];

            Nation nation = null;
            if (type.equals("declarer"))
                nation = declarationBook.getDeclarer().getNation();
            else if (type.equals("target"))
                nation = declarationBook.getWarTarget().getNation();
            return placeholder(name, getNationStat(stat, nation));
        }


        if (declarationBook.getType().isTownWar()) {
            String[] split = name.split("-");
            String type = split[0];
            String stat = split[1];

            Town town = null;
            if (type.equals("declarer"))
                town = declarationBook.getDeclarer().getTown();
            else if (type.equals("target"))
                town = declarationBook.getWarTarget().getTown();
            return placeholder(name, getTownStat(stat, town));
        }
        return placeholder(name, "0");
    }

    private String getTownStat(String statName, Town town) {
        switch (statName) {
            case "blocks" -> {
                return String.valueOf(town.getNumTownBlocks());
            }
            case "residents" -> {
                return String.valueOf(town.getNumResidents());
            }
            case "balance" -> {
                return String.valueOf(town.getAccount().getHoldingBalance());
            }
        }
        return "0";
    }

    private String getNationStat(String statName, Nation nation) {
        switch (statName) {
            case "blocks" -> {
                return String.valueOf(nation.getTownBlocks().size());
            }
            case "residents" -> nation.getNumResidents();
            case "balance" -> nation.getAccount().getCachedBalance();
        }
        return "0";
    }

    private String getName(String type) {
        WarType warType = declarationBook.getType();
        Town town;
        if (type.equals("declarer")) {
            town = declarationBook.getDeclarer().getTown();
        } else {
            town = declarationBook.getWarTarget().getTown();
        }
        if (warType.isTownWar()) {
            return town.getFormattedName();
        } else if (warType.isNationWar()) {
            return town.getNationOrNull().getFormattedName();
        }
        return town.getMayor().getPlayer().getName();
    }

    public void attachWarData() {
        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();
        UUID townUUID = declarationBook.getDeclarer().getTown().getUUID();
        UUID targetUUID = declarationBook.getWarTarget().getUUID();
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.town"), PersistentDataType.STRING, townUUID.toString());
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.type"), PersistentDataType.STRING, declarationBook.getType().name());
        pdc.set(NamespacedKey.fromString("unitedwars.book.target"), PersistentDataType.STRING, targetUUID.toString());
    }


}
