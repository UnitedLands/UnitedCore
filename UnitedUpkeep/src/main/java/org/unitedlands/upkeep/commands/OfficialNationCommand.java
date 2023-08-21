package org.unitedlands.upkeep.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.upkeep.UnitedUpkeep;

import java.util.Collections;
import java.util.List;

import static org.unitedlands.upkeep.util.NationMetaController.setOfficialNation;

public class OfficialNationCommand implements TabExecutor {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 2) {
            return BaseCommand.getTownyStartingWith(args[1], "n");
        }
        return Collections.emptyList();
    }

    public OfficialNationCommand() {
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(TownyCommandAddonAPI.CommandType.TOWNYADMIN, "officialnation", this));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("united.upkeep.admin")) {
            sender.sendMessage("No permission to use this command!");
            return true;
        }
        Nation nation = TownyAPI.getInstance().getNation(args[0]);
        if (nation == null) {
            sender.sendMessage("Invalid nation name!");
            return true;
        }
        setOfficialNation(nation, args[1].equalsIgnoreCase("true"));
        sender.sendMessage("Successfully set " + args[1] + " as an official nation!");
        return true;
    }

}
