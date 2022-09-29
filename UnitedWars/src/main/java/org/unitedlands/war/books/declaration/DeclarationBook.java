package org.unitedlands.war.books.declaration;

import io.github.townyadvanced.eventwar.objects.WarType;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.war.books.Declarer;
import org.unitedlands.war.books.WarTarget;

import java.util.List;

public interface DeclarationBook {
    ItemStack getBook();
    WarType getType();
    Declarer getDeclarer();
    WarTarget getWarTarget();
    List<Component> getReason();
}
