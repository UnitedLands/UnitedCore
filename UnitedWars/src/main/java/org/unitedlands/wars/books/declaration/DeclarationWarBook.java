package org.unitedlands.wars.books.declaration;

import net.kyori.adventure.text.Component;
import org.unitedlands.wars.books.warbooks.WarBook;

import java.util.List;

public interface DeclarationWarBook extends WarBook {
    List<Component> getReason();
}
