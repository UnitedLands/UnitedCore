package org.unitedlands.war.books.declaration;

import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.war.books.generators.DeclarationGenerator;
import org.unitedlands.war.books.data.Declarer;
import org.unitedlands.war.books.data.WarTarget;

import java.util.List;

public class NationDeclarationBook implements DeclarationWarBook {

    private final WritableDeclaration writableDeclaration;
    private final Declarer declarer;
    private final WarTarget warTarget;

    public NationDeclarationBook(WritableDeclaration writableDeclaration) {
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
        return WarTypeEnum.NATIONWAR.getType();
    }

    @Override
    public String slug() {
        return "nationwar-declaration";
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
