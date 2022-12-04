package org.unitedlands.wars.commands;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.*;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.TokenCostCalculator;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.books.warbooks.WritableDeclaration;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarType;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.*;

public class BookCommandParser {
    private final CommandSender sender;

    public BookCommandParser(CommandSender sender) {
        this.sender = sender;
    }

    public void parseBookCreation(WarType type, String target) {
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
        if (hasActiveWar(resident)) {
            player.sendMessage(getMessage("ongoing-war"));
        }

        switch (type.name().toLowerCase()) {
            case "townwar" -> parseTownBookCreationCommand(target);
            case "nationwar" -> {
                if (resident.hasNation()) {
                    parseNationBookCreationCommand(target);
                } else {
                    player.sendMessage(getMessage("must-have-nation"));
                }
            }
        }
    }


    private void parseTownBookCreationCommand(@NotNull String target) {
        Player player = (Player) sender;
        Town targetTown = UnitedWars.TOWNY_API.getTown(target);

        if (targetTown == null) {
            player.sendMessage(getMessage("invalid-town-name"));
            return;
        }

        if (targetTown.isNeutral()) {
            player.sendMessage(getMessage("must-not-be-neutral-target"));
            return;
        }

        if (targetTown.hasActiveWar()) {
            player.sendMessage(getMessage("ongoing-war-target"));
            return;
        }
        Town declaringTown = getPlayerTown(player);
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

                TownyMessaging.sendPrefixedTownMessage(declaringTown, Translatable.of("msg_town_purchased_declaration_of_type", declaringTown, type.name()));
            }
        }).setTitle(getConfirmationTitle(type, cost)).sendTo(player);
    }

    private void parseNationBookCreationCommand(@NotNull String target) {
        Player player = (Player) sender;

        Nation targetNation = UnitedWars.TOWNY_API.getNation(target);
        if (targetNation == null) {
            player.sendMessage(getMessage("invalid-nation-name"));
            return;
        }

        if (targetNation.isNeutral()) {
            player.sendMessage(getMessage("must-not-be-neutral-target"));
            return;
        }

        if (targetNation.hasActiveWar()) {
            player.sendMessage(getMessage("ongoing-war-target"));
            return;
        }

        WarType type = WarType.NATIONWAR;
        WritableDeclaration writableDeclaration = new WritableDeclaration(new Declarer(player), new WarTarget(targetNation), type);
        TokenCostCalculator costCalculator = new TokenCostCalculator(targetNation);
        int cost = costCalculator.calculateWarCost();

        Confirmation.runOnAccept(() -> {
            Nation declaringNation = getPlayerTown(player).getNationOrNull();
            if (takeTokens(declaringNation, cost)) {
                player.getInventory().addItem(writableDeclaration.getBook());
                TownyMessaging.sendPrefixedNationMessage(declaringNation, Translatable.of("msg_town_purchased_declaration_of_type", declaringNation, type.name()));
            }
        }).setTitle(getConfirmationTitle(type, cost)).sendTo(player);
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

    private boolean hasActiveWar(Resident resident) {
        if (resident.getTownOrNull().hasNation()) {
            return resident.getNationOrNull().hasActiveWar();
        }
        return resident.getTownOrNull().hasActiveWar();
    }

    private Translatable getConfirmationTitle(WarType warType, int cost) {
        String message = UnitedWars.getInstance().getConfig().getString("messages.war-confirmation")
                .replace("<cost>", String.valueOf(cost))
                .replace("<type>", getFormattedTypeName(warType));
        return Translatable.of(message);
    }

    private String getFormattedTypeName(WarType warType) {
        String name = warType.name()
                .substring(0,1).toUpperCase() + warType.name().substring(1).toLowerCase(); // Capitalize the first letter
        return name + " War";
    }
}
