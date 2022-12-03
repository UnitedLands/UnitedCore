package org.unitedlands.wars.commands;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.AddonCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.wars.war.WarType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.unitedlands.wars.Utils.getMessage;

public class WarCommand implements TabExecutor {
    private static final List<String> warTabCompletes = Arrays.asList("declare", "book");
    private static final List<String> optionsTabCompletes = Arrays.asList("town", "nation");

    public WarCommand() {
        AddonCommand warCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN, "war", this);
        TownyCommandAddonAPI.addSubCommand(warCommand);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args[0].equalsIgnoreCase("book")) {
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
        return args.length == 1 ? warTabCompletes : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {

            if (args[0].equalsIgnoreCase("declare")) {
                DeclareCommandParser parser = new DeclareCommandParser(commandSender);
                parser.parseDeclareCommand();
            }

            if (args[0].equalsIgnoreCase("book")) {
                if (args.length < 3) {
                    commandSender.sendMessage(getMessage("must-specify-target"));
                    return true;
                }
                WarType type = WarType.valueOf((args[1] + "war").toUpperCase());
                BookCommandParser parser = new BookCommandParser(commandSender);
                parser.parseBookCreation(type, args[2]);
            }
        }
        return true;
    }

}
