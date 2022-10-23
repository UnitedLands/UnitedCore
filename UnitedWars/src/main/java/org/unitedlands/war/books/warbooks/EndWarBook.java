package org.unitedlands.war.books.warbooks;

import io.github.townyadvanced.eventwar.instance.War;
import io.github.townyadvanced.eventwar.objects.WarType;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.war.books.data.Declarer;
import org.unitedlands.war.books.data.WarTarget;
import org.unitedlands.war.books.generators.BookGenerator;

public class EndWarBook implements WarBook {
    private final Declarer declarer;
    private final WarTarget warTarget;
    private final WarType warType;

    public EndWarBook(War war) {
        this.declarer = new Declarer(war.getDeclarationOfWar().getTown());
        this.warTarget = new WarTarget(war.getWarParticipants().getTowns().get(1));
        this.warType = war.getWarType();
    }

    @Override
    public ItemStack getBook() {
        BookGenerator generator = new BookGenerator(this);
        return generator.generateBook();
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
    public WarType getType() {
        return warType;
    }

    @Override
    public String slug() {
        return "end-war";
    }
}
