package org.unitedlands.wars.books.declaration;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.books.generators.DeclarationGenerator;
import org.unitedlands.wars.books.warbooks.WritableDeclaration;
import org.unitedlands.wars.war.WarType;

import java.util.List;

public class TownDeclarationBook implements DeclarationWarBook {
    private final WritableDeclaration writableDeclaration;
    private final Declarer declarer;
    private final WarTarget warTarget;

    public TownDeclarationBook(WritableDeclaration writableDeclaration) {
        this.writableDeclaration = writableDeclaration;
        declarer = writableDeclaration.getDeclarer();
        warTarget = writableDeclaration.getWarTarget();
    }

    @Override
    public ItemStack getBook() {
        DeclarationGenerator declarationGenerator = new DeclarationGenerator(this);
        return declarationGenerator.generateBook();
    }


    @Override
    public WarType getType() {
        return WarType.TOWNWAR;
    }

    @Override
    public String slug() {
        return "townwar-declaration";
    }

    @Override
    public Declarer getDeclarer() {
        return declarer;
    }

    @Override
    public WarTarget getWarTarget() {
        return warTarget;
    }

    @Override
    public List<Component> getReason() {
        return writableDeclaration.getReason();
    }

}
