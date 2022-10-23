package org.unitedlands.war.books.warbooks;

import io.github.townyadvanced.eventwar.objects.WarType;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.war.books.data.Declarer;
import org.unitedlands.war.books.data.WarTarget;

public interface WarBook {
    ItemStack getBook();
    Declarer getDeclarer();
    WarTarget getWarTarget();
    WarType getType();
    String slug();
}
