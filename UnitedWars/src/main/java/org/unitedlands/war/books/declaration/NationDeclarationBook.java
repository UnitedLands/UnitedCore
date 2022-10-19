package org.unitedlands.war.books.declaration;

import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.war.books.Declarer;
import org.unitedlands.war.books.WarTarget;
import org.unitedlands.war.books.WritableDeclaration;

import java.util.List;

public class NationDeclarationBook implements DeclarationBook {

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
        DeclarationBookGenerator bookGenerator = new DeclarationBookGenerator(this);
        return bookGenerator.generateBook();
    }

    @Override
    public WarType getType() {
        return WarTypeEnum.NATIONWAR.getType();
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
