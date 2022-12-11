package org.unitedlands.wars.commands.surrender;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;

import java.util.Objects;
import java.util.UUID;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.getMessage;

class SurrenderRequest {
    private final UUID requester;
    private final UUID target;
    private final SurrenderType type;
    private final int moneyAmount;
    private final UUID town;

    SurrenderRequest(UUID requester, UUID target, SurrenderType type) {
        this.requester = requester;
        this.target = target;
        this.type = type;
        moneyAmount = 0;
        town = null;
        notifyTarget();
    }

    SurrenderRequest(UUID requester, UUID target, SurrenderType type, int moneyAmount) {
        this.requester = requester;
        this.target = target;
        this.type = type;
        this.moneyAmount = moneyAmount;
        town = null;
        notifyTarget();
    }

    SurrenderRequest(UUID requester, UUID target, SurrenderType type, Town town) {
        this.requester = requester;
        this.target = target;
        this.type = type;
        this.moneyAmount = 0;
        this.town = town.getUUID();
        notifyTarget();
    }

    public Player getRequester() {
        return Bukkit.getPlayer(requester);
    }

    public Player getTarget() {
        return Bukkit.getPlayer(target);
    }

    public SurrenderType getType() {
        return type;
    }

    public int getMoneyAmount() {
        return moneyAmount;
    }

    public Town getOfferedTown() {
        return UnitedWars.TOWNY_API.getTown(town);
    }

    public String getTitle() {
        String title =  Utils.getMessageRaw("surrender-accept-" + type.name().toLowerCase())
                .replace("<surrender-name>", WarDatabase.getWarringEntity(Bukkit.getPlayer(requester)).name())
                .replace("<money-amount>", String.valueOf(moneyAmount));
        if (type == SurrenderType.TOWN) {
            title = title.replace("<offered-town>", UnitedWars.TOWNY_API.getTown(town).getFormattedName());
        }
        return title;
    }

    public void accept() {
        switch (type) {
            case MONEY -> acceptMoney();
            case TOWN -> acceptTown();
            case WHITEPEACE -> acceptPeace();
        }
    }

    private void acceptPeace() {
        WarringEntity winner = WarDatabase.getWarringEntity(getTarget());
        WarringEntity loser = WarDatabase.getWarringEntity(getRequester());

        broadcastMessage(winner, getMessage("war-end-peace"));
        broadcastMessage(loser, getMessage("war-end-peace"));
    }

    private void acceptMoney() {
        WarringEntity winner = WarDatabase.getWarringEntity(getTarget());
        WarringEntity loser = WarDatabase.getWarringEntity(getRequester());

        loser.getGovernment().getAccount().withdraw(moneyAmount, "Surrendered");
        winner.getGovernment().getAccount().deposit(moneyAmount, loser.name() + " surrendered!");

        TagResolver.Single moneyComponent = Placeholder.component("money", text(moneyAmount));
        broadcastMessage(winner, getMessage("won-surrender-money", moneyComponent));
        broadcastMessage(loser, getMessage("lost-surrender-money", moneyComponent));
    }
    private void acceptTown() {
        WarringNation winner = (WarringNation) WarDatabase.getWarringEntity(getTarget());
        WarringNation loser = (WarringNation) WarDatabase.getWarringEntity(getRequester());
        Town offeredTown = getOfferedTown();
        offeredTown.removeNation();
        try {
            offeredTown.setNation(winner.getNation(), true);
        } catch (AlreadyRegisteredException e) {
            e.printStackTrace();
        }
        TagResolver.Single townName = Placeholder.component("town", text(offeredTown.getFormattedName()));
        broadcastMessage(winner, getMessage("won-surrender-town", townName));
        broadcastMessage(loser, getMessage("lost-surrender-town", townName));
    }

    private void broadcastMessage(WarringEntity warringEntity, Component component) {
        warringEntity.getOnlinePlayers().forEach(player -> {
            player.sendMessage(component);
        });
    }

    private void notifyTarget() {
        Component offer;
        switch (type) {
            case MONEY -> offer = text(moneyAmount + " Gold");
            case TOWN -> offer = text("the town of " + getOfferedTown().getFormattedName());
            case WHITEPEACE -> offer = text("to have a White Peace");
            default -> offer = empty();
        }
        getTarget().sendMessage(getMessage("surrender-received",
                Placeholder.component("offer", offer),
                Placeholder.component("requester", text(WarDatabase.getWarringEntity(getRequester()).name()))));
    }

    enum SurrenderType {
        TOWN,
        MONEY,
        WHITEPEACE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurrenderRequest that = (SurrenderRequest) o;

        if (!Objects.equals(requester, that.requester)) return false;
        if (!Objects.equals(target, that.target)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = requester != null ? requester.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
