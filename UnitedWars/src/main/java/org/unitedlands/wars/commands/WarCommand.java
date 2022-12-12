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
    private static final List<String> warTabCompletes = Arrays.asList("declare", "scroll");
    private final String type;
    public WarCommand(String type) {
        this.type = type;
        TownyCommandAddonAPI.CommandType commandType = type.equals("n") ? TownyCommandAddonAPI.CommandType.NATION : TownyCommandAddonAPI.CommandType.TOWN;
        AddonCommand warCommand = new AddonCommand(commandType, "war", this);
        TownyCommandAddonAPI.addSubCommand(warCommand);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("scroll")) {
            if (args.length == 2) {
                return BaseCommand.getTownyStartingWith(args[1], type);
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

            if (args[0].equalsIgnoreCase("scroll")) {
                if (args.length < 2) {
                    commandSender.sendMessage(getMessage("must-specify-target"));
                    return true;
                }
                BookCommandParser parser = new BookCommandParser(commandSender);
                WarType warType = type.equals("n") ? WarType.NATIONWAR : WarType.TOWNWAR;
                parser.parseBookCreation(warType, args[1]);
            }
        }
        return true;
    }
}
