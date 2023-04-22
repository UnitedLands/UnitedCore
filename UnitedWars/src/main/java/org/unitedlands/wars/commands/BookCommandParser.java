package org.unitedlands.wars.commands;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.*;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.TokenCostCalculator;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.books.warbooks.WritableDeclaration;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarType;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.*;
import static org.unitedlands.wars.war.WarDatabase.hasWar;

public class BookCommandParser {
    private final CommandSender sender;
    private final WarType type;
    private final String target;
    public BookCommandParser(CommandSender sender, WarType type, String target) {
        this.sender = sender;
        this.type = type;
        this.target = target;
    }

    public void parse() {
        Player player = (Player) sender;
        Resident resident = getTownyResident(player);

        if (!resident.hasTown()) {
            player.sendMessage(getMessage("must-have-town"));
            return;
        }
        if (!resident.isMayor()) {
            player.sendMessage(getMessage("must-be-mayor"));
            return;
        }
        if (isNeutral(resident)) {
            player.sendMessage(getMessage("must-not-be-neutral"));
            return;
        }
        if (hasWar(player)) {
            player.sendMessage(getMessage("ongoing-war"));
        }

        switch (type.name().toLowerCase()) {
            case "townwar" -> parseTownBookCreationCommand();
            case "nationwar" -> {
                if (resident.hasNation()) {
                    parseNationBookCreationCommand();
                } else {
                    player.sendMessage(getMessage("must-have-nation"));
                }
            }
        }
    }


    private void parseTownBookCreationCommand() {
        Player player = (Player) sender;
        Town targetTown = UnitedWars.TOWNY_API.getTown(target);

        if (targetTown == null) {
            player.sendMessage(getMessage("invalid-town-name"));
            return;
        }

        Town declaringTown = getPlayerTown(player);
        if (declaringTown.equals(targetTown)) {
            player.sendMessage(getMessage("cannot-declare-on-self"));
            return;
        }

        if (targetTown.isNeutral()) {
            player.sendMessage(getMessage("must-not-be-neutral-target"));
            return;
        }

        if (hasWar(targetTown)) {
            player.sendMessage(getMessage("ongoing-war-target"));
            return;
        }
        // Both have a nation, force a nation war.
        if (targetTown.hasNation() && declaringTown.hasNation()) {
            player.sendMessage(getMessage("must-declare-nationwar"));
            return;
        }

        WarType type = WarType.TOWNWAR;
        TokenCostCalculator costCalculator = new TokenCostCalculator(targetTown);
        int cost = costCalculator.calculateWarCost();

        Confirmation.runOnAccept(() -> {
            if (takeTokens(declaringTown, cost)) {
                WritableDeclaration writableDeclaration = new WritableDeclaration(new Declarer(player), new WarTarget(targetTown), type);
                player.getInventory().addItem(writableDeclaration.getBook());

                TownyMessaging.sendPrefixedTownMessage(declaringTown, getMessageRaw("town-purchased-declaration"));
            }
        }).setTitle(getConfirmationTitle(cost)).sendTo(player);
    }

    private void parseNationBookCreationCommand() {
        Player player = (Player) sender;

        Nation targetNation = UnitedWars.TOWNY_API.getNation(target);
        if (targetNation == null) {
            player.sendMessage(getMessage("invalid-nation-name"));
            return;
        }
        Nation declaringNation = getPlayerTown(player).getNationOrNull();
        if (targetNation.equals(declaringNation)) {
            player.sendMessage(getMessage("cannot-declare-on-self"));
            return;
        }
        if (!declaringNation.getKing().getPlayer().equals(player)) {
            player.sendMessage(getMessage("must-be-mayor"));
            return;
        }

        if (declaringNation.getAllies().contains(targetNation)) {
            player.sendMessage(getMessage("cannot-declare-on-allies"));
            return;
        }

        if (targetNation.isNeutral()) {
            player.sendMessage(getMessage("must-not-be-neutral-target"));
            return;
        }

        if (hasWar(targetNation.getCapital())) {
            player.sendMessage(getMessage("ongoing-war-target"));
            return;
        }

        WarType type = WarType.NATIONWAR;
        WritableDeclaration writableDeclaration = new WritableDeclaration(new Declarer(player), new WarTarget(targetNation), type);
        TokenCostCalculator costCalculator = new TokenCostCalculator(targetNation);
        int cost = costCalculator.calculateWarCost();

        Confirmation.runOnAccept(() -> {
            if (takeTokens(declaringNation.getCapital(), cost)) {
                player.getInventory().addItem(writableDeclaration.getBook());
                TownyMessaging.sendPrefixedNationMessage(declaringNation, getMessageRaw("nation-purchased-declaration"));
            }
        }).setTitle(getConfirmationTitle(cost)).sendTo(player);
    }

    private boolean takeTokens(TownyObject declarer, int cost) {
        if (WarDataController.getWarTokens(declarer) < cost) {
            sender.sendMessage(getMessage("not-enough-tokens", Placeholder.component("cost", text(cost))));
            return false;
        }
        int remainder = WarDataController.getWarTokens(declarer) - cost;
        WarDataController.setTokens(declarer, remainder);
        return true;
    }

    private boolean isNeutral(Resident resident) {
        if (resident.getTownOrNull().hasNation()) {
            return resident.getNationOrNull().isNeutral();
        }
        return resident.getTownOrNull().isNeutral();
    }

    private Translatable getConfirmationTitle(int cost) {
        String message = UnitedWars.getInstance().getConfig().getString("messages.war-confirmation")
                .replace("<cost>", String.valueOf(cost))
                .replace("<type>", type.getFormattedName());
        return Translatable.of(message);
    }

}
