package org.unitedlands.war.books.generators;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.eventwar.objects.WarType;
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
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.Utils;
import org.unitedlands.war.books.data.Declarer;
import org.unitedlands.war.books.warbooks.WarBook;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.*;

public class BookGenerator {
    private final WarBook warBook;
    private static final FileConfiguration CONFIG = Utils.getPlugin().getConfig();
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
        if (warType.isTownWar()) {
            return town.getFormattedName();
        } else if (warType.isNationWar()) {
            return town.getNationOrNull().getFormattedName();
        }
        return town.getMayor().getPlayer().getName();
    }

    private TagResolver.@NotNull Single placeholder(String name, String output) {
        return Placeholder.component(name, text(output));
    }

    private TagResolver.Single placeholderStat(String name) {
        if (warBook.getType().isNationWar()) {
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


        if (warBook.getType().isTownWar()) {
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
            case "residents" -> {
                return String.valueOf(nation.getNumResidents());
            }
            case "balance" -> {
                double totalBalance = nation.getAccount().getHoldingBalance();
                for (Town town: nation.getTowns()) {
                    totalBalance += town.getAccount().getHoldingBalance();
                }
                return String.valueOf(totalBalance);
            }
        }
        return "0";
    }
}
