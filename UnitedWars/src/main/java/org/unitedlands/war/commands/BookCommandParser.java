package org.unitedlands.war.commands;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.*;
import io.github.townyadvanced.eventwar.db.WarMetaDataController;
import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.books.TokenCostCalculator;
import org.unitedlands.war.books.data.Declarer;
import org.unitedlands.war.books.data.WarTarget;
import org.unitedlands.war.books.warbooks.WritableDeclaration;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.war.Utils.*;
import static org.unitedlands.war.Utils.getMessage;

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

        WarType type = WarTypeEnum.TOWNWAR.getType();
        int cost = type.tokenCost();

        Confirmation.runOnAccept(() -> {
            Town declaringTown = getPlayerTown(player);
            takeTokens(declaringTown, cost);

            WritableDeclaration writableDeclaration = new WritableDeclaration(new Declarer(player), new WarTarget(targetTown), type);
            player.getInventory().addItem(writableDeclaration.getBook());

            TownyMessaging.sendPrefixedTownMessage(declaringTown, Translatable.of("msg_town_purchased_declaration_of_type", declaringTown, type.name()));

        }).setTitle(Translatable.of("msg_you_are_about_to_purchase_a_declaration_of_war_of_type_for_x_tokens", type.name(), cost)).sendTo(player);
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

        WarType type = WarTypeEnum.NATIONWAR.getType();
        WritableDeclaration writableDeclaration = new WritableDeclaration(new Declarer(player), new WarTarget(targetNation), type);
        TokenCostCalculator costCalculator = new TokenCostCalculator(writableDeclaration);
        int cost = costCalculator.calculateWarCost();

        Confirmation.runOnAccept(() -> {
            Nation declaringNation = getPlayerTown(player).getNationOrNull();
            takeTokens(declaringNation.getCapital(), cost);

            player.getInventory().addItem(writableDeclaration.getBook());

            TownyMessaging.sendPrefixedNationMessage(declaringNation, Translatable.of("msg_town_purchased_declaration_of_type", declaringNation, type.name()));

        }).setTitle(Translatable.of("msg_you_are_about_to_purchase_a_declaration_of_war_of_type_for_x_tokens", type.name(), cost)).sendTo(player);
    }

    private void takeTokens(Town declaringTown, int cost) {
        if (WarMetaDataController.getWarTokens(declaringTown) < cost) {
            sender.sendMessage(getMessage("not-enough-tokens", Placeholder.component("cost", text(cost))));
            return;
        }
        int remainder = WarMetaDataController.getWarTokens(declaringTown) - cost;
        WarMetaDataController.setTokens(declaringTown, remainder);
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
}
