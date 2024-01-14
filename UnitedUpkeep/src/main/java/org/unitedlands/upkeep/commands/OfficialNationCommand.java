package org.unitedlands.upkeep.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Nation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.upkeep.util.NationMetaController;

public class OfficialNationCommand implements TabExecutor {
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return args.length == 2 ? BaseCommand.getTownyStartingWith(args[1], "n") : Collections.emptyList();
    }

    public OfficialNationCommand() {
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(CommandType.TOWNYADMIN, "officialnation", this));
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("united.upkeep.admin")) {
            sender.sendMessage("No permission to use this command!");
            return true;
        } else {
            Nation nation = TownyAPI.getInstance().getNation(args[1]);
            if (nation == null) {
                sender.sendMessage("Invalid nation name!");
            } else {
                NationMetaController.setOfficialNation(nation, args[0].equalsIgnoreCase("true"));
                sender.sendMessage("Successfully set " + args[1] + " as an official nation!");
            }
            return true;
        }
    }
}
