package org.unitedlands.wars.books.generators;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.warbooks.WarBook;
import org.unitedlands.wars.war.WarType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.*;

public class BookGenerator {
    private static final FileConfiguration CONFIG = UnitedWars.getInstance().getConfig();
    private final WarBook warBook;
    private final ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
    private BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

    public BookGenerator(WarBook warBook) {
        this.warBook = warBook;
    }


    public ItemStack generateBook() {
        List<Component> pages = getConfiguredPages();
        BookMeta.BookMetaBuilder builder = bookMeta.toBuilder();
        pages.forEach(builder::addPage);
        bookMeta = builder
                .author(text("WarUpdates"))
                .title(text("War Book"))
                .build();
        bookMeta.displayName(getDisplayName());
        bookItem.setItemMeta(bookMeta);
        return bookItem;
    }


    protected List<Component> getConfiguredPages() {
        ConfigurationSection bookSection = getBookSection();
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
        return deserializedPages;
    }

    @Nullable
    private ConfigurationSection getBookSection() {
        return CONFIG.getConfigurationSection(warBook.slug() + "-book");
    }

    @NotNull
    protected Component getDisplayName() {
        return parseLine(CONFIG.getString(warBook.slug() + "-name"));
    }

    @NotNull
    protected Component parseLine(String line) {
        Declarer declarer = warBook.getDeclarer();
        String placeholderParsedLine = PlaceholderAPI.setPlaceholders(declarer.player(), line);

        return UnitedWars.MINI_MESSAGE.deserialize(placeholderParsedLine,
                placeholder("declarer-name", getName("declarer")),
                placeholder("target-name", getName("target")),
                placeholder("declaring-mayor", declarer.player().getName()),
                placeholderStat("declarer-balance"),
                placeholderStat("declarer-blocks"),
                placeholderStat("declarer-residents"),
                placeholderStat("target-balance"),
                placeholderStat("target-blocks"),
                placeholderStat("target-residents"));
    }

    protected String getName(String type) {
        WarType warType = warBook.getType();
        Town town;
        if (type.equals("declarer")) {
            town = warBook.getDeclarer().town();
        } else {
            town = warBook.getWarTarget().town();
        }
        if (warType == WarType.TOWNWAR) {
            return town.getFormattedName();
        } else if (warType == WarType.NATIONWAR) {
            return town.getNationOrNull().getFormattedName();
        }
        return town.getMayor().getPlayer().getName();
    }

    private TagResolver.@NotNull Single placeholder(String name, String output) {
        return Placeholder.component(name, text(output));
    }

    private TagResolver.Single placeholderStat(String name) {
        if (warBook.getType() == WarType.NATIONWAR) {
            String[] split = name.split("-");
            String type = split[0];
            String stat = split[1];

            Nation nation = null;
            if (type.equals("declarer"))
                nation = warBook.getDeclarer().nation();
            else if (type.equals("target"))
                nation = warBook.getWarTarget().nation();
            return placeholder(name, getNationStat(stat, nation));
        }


        if (warBook.getType() == WarType.TOWNWAR) {
            String[] split = name.split("-");
            String type = split[0];
            String stat = split[1];

            Town town = null;
            if (type.equals("declarer"))
                town = warBook.getDeclarer().town();
            else if (type.equals("target"))
                town = warBook.getWarTarget().town();
            return placeholder(name, getTownStat(stat, town));
        }
        return placeholder(name, "0");
    }

    private String getTownStat(String statName, Town town) {
        return switch (statName) {
            case "blocks" -> String.valueOf(town.getNumTownBlocks());
            case "residents" -> String.valueOf(town.getNumResidents());
            case "balance" -> NumberFormat.getInstance().format(town.getAccount().getHoldingBalance());
            default -> "0";
        };
    }

    private String getNationStat(String statName, Nation nation) {
        return switch (statName) {
            case "blocks" -> String.valueOf(nation.getTownBlocks().size());
            case "residents" -> String.valueOf(nation.getNumResidents());
            case "balance" -> {
                double totalBalance = nation.getAccount().getHoldingBalance();
                for (Town town : nation.getTowns()) {
                    totalBalance += town.getAccount().getHoldingBalance();
                }
                yield NumberFormat.getInstance().format(totalBalance);
            }
            default -> "0";
        };
    }
}
