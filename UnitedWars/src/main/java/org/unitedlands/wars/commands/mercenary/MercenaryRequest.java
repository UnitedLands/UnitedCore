package org.unitedlands.wars.commands.mercenary;

import com.palmergames.bukkit.towny.object.Resident;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringEntity;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.getMessage;

public class MercenaryRequest {
    private final UUID requester;
    private final UUID target;
    private final int moneyAmount;

    public MercenaryRequest(UUID requester, UUID target, int moneyAmount) {
        this.requester = requester;
        this.target = target;
        this.moneyAmount = moneyAmount;
        notifyTarget();
    }

    public Player getRequester() {
        return Bukkit.getPlayer(requester);
    }

    public Player getTarget() {
        return Bukkit.getPlayer(target);
    }

    public int getMoneyAmount() {
        return moneyAmount;
    }

    public void accept() {
        Resident requestingResident = Utils.getTownyResident(requester);
        Resident mercenary = Utils.getTownyResident(target);
        requestingResident.getAccount().withdraw(moneyAmount, "hired mercenary");
        mercenary.getAccount().deposit(moneyAmount, "hired as mercenary");

        WarringEntity entity = WarDatabase.getWarringEntity(requester);
        War war = entity.getWar();

        entity.addMercenary(mercenary);
        war.addResident(mercenary, entity.getLeader().getTownOrNull());

        Component message = getMessage("mercenary-hired",
                Placeholder.component("mercenary", text(mercenary.getFormattedName())),
                Placeholder.component("entity", text(entity.name())));
        war.broadcast(message);
    }

    private void notifyTarget() {
        getTarget().sendMessage(getMessage("mercenary-received",
                Placeholder.component("amount", text(moneyAmount)),
                Placeholder.component("requester", text(WarDatabase.getWarringEntity(getRequester()).name()))));
    }
}
