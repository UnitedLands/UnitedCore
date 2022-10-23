package org.unitedlands.war.books.declaration;

import net.kyori.adventure.text.Component;
import org.unitedlands.war.books.warbooks.WarBook;

import java.util.List;

public interface DeclarationWarBook extends WarBook {
    List<Component> getReason();
}
