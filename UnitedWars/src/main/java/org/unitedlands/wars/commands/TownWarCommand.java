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
import static org.unitedlands.wars.Utils.getMessageList;

public class TownWarCommand implements TabExecutor {
    private static final List<String> warTabCompletes = Arrays.asList("declare", "scroll");
    public TownWarCommand() {
        TownyCommandAddonAPI.CommandType commandType = TownyCommandAddonAPI.CommandType.TOWN;
        AddonCommand warCommand = new AddonCommand(commandType, "war", this);
        TownyCommandAddonAPI.addSubCommand(warCommand);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("scroll")) {
            if (args.length == 2) {
                return BaseCommand.getTownyStartingWith(args[1], "t");
            }
        }
        return args.length == 1 ? warTabCompletes : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            if (args.length == 0) {
                getMessageList("townwar-help").forEach(commandSender::sendMessage);
                return true;
            }
            if (args[0].equalsIgnoreCase("declare")) {
                DeclareCommandParser declareCommand = new DeclareCommandParser(commandSender);
                declareCommand.parse();
            }

            if (args[0].equalsIgnoreCase("scroll")) {
                if (args.length < 2) {
                    commandSender.sendMessage(getMessage("must-specify-target"));
                    return true;
                }
                BookCommandParser bookCommand = new BookCommandParser(commandSender, WarType.TOWNWAR, args[1]);
                bookCommand.parse();
            }
        }
        return true;
    }
}
