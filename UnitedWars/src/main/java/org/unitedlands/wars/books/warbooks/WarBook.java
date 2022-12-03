package org.unitedlands.wars.books.warbooks;

import org.bukkit.inventory.ItemStack;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.war.WarType;

public interface WarBook {
    ItemStack getBook();

    Declarer getDeclarer();

    WarTarget getWarTarget();

    WarType getType();

    String slug();
}
