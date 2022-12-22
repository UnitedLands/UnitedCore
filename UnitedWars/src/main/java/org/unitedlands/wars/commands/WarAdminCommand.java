package org.unitedlands.wars.commands;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarUtil;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;
import org.unitedlands.wars.war.entities.WarringTown;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WarAdminCommand implements TabExecutor {
    private static final List<String> adminTabCompletes = Arrays.asList("end", "purge");
    private static final List<String> optionsTabCompletes = Arrays.asList("town", "nation");
    private CommandSender sender;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("end")) {
            if (args.length == 2) {
                return optionsTabCompletes;
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("town")) {
                    return BaseCommand.getTownyStartingWith(args[2], "t");
                }

                if (args[1].equalsIgnoreCase("nation")) {
                    return BaseCommand.getTownyStartingWith(args[2], "n");
                }
                return Collections.emptyList();
            }
        }
        return args.length == 1 ? adminTabCompletes : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        this.sender = sender;
        if (args[0].equalsIgnoreCase("purge")) {
            parsePurgeCommand();
            return true;
        }
        if (args[0].equalsIgnoreCase("end")) {
            if (args[1].equalsIgnoreCase("town")) {
                Town winner = UnitedWars.TOWNY_API.getTown(args[2]);
                if (winner == null) {
                    sender.sendMessage(Component.text("Invalid Town!").color(NamedTextColor.RED));
                    return true;
                }
                WarringTown winningWarTown = WarDatabase.getWarringTown(winner);
                WarringTown losingWarTown = (WarringTown) winningWarTown.getEnemy();

                // Delegate the rest of the logic to the function.
                parseEndCommand(winningWarTown, losingWarTown);
                return true;
            }
            if (args[1].equalsIgnoreCase("nation")) {
                Nation winner = UnitedWars.TOWNY_API.getNation(args[2]);
                Nation loser = UnitedWars.TOWNY_API.getNation(args[3]);
                if (winner == null || loser == null) {
                    sender.sendMessage(Component.text("One of the towns doesn't exist... Are you sure the names are correct?").color(NamedTextColor.RED));
                    return true;
                }
                WarringNation winningWarNation = WarDatabase.getWarringNation(winner);
                WarringNation losingWarNation = (WarringNation) winningWarNation.getEnemy();

                // Delegate the rest of the logic to the function.
                parseEndCommand(winningWarNation, losingWarNation);
                return true;
            }
        }
        return false;
    }

    private void parsePurgeCommand() {
        TownyUniverse townyUniverse = TownyUniverse.getInstance();
        WarDatabase.cleanUpBossBars();
        for (Town town : townyUniverse.getTowns()) {
            town.setActiveWar(false);
            WarDatabase.removeWarringTown(town);
            WarDataController.removeEndTime(town);
        }

        for (Nation nation : townyUniverse.getNations()) {
            nation.setActiveWar(false);
            WarDatabase.removeWarringNation(nation);
            WarDataController.removeEndTime(nation);
        }

        for (Resident resident : townyUniverse.getResidents()) {
            WarDataController.removeResidentLivesMeta(resident);
        }

        WarDatabase.clearSets();
        WarDatabase.saveWarData();

        sender.sendMessage(Component.text("Purged all saved and ongoing wars!").color(NamedTextColor.GREEN));
    }

    private void parseEndCommand(WarringEntity winner, WarringEntity loser) {
        if (winner == null || loser == null) {
            sender.sendMessage(Component.text("One of the towns doesn't have a war! Are you sure the names are correct?").color(NamedTextColor.RED));
            return;
        }

        War war = winner.getWar();
        war.endWar(winner, loser);
    }
}
