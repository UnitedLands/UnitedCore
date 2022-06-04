package org.unitedlands.skills.commands;

import dev.triumphteam.gui.guis.Gui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.guis.BlendingGui;

import static org.unitedlands.skills.Utils.getMessage;

public class BlendCommand implements CommandExecutor {
    private final UnitedSkills unitedSkills;

    public BlendCommand(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        if (!player.hasPermission("united.skills.blend")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }
        BlendingGui blendingGui = new BlendingGui(unitedSkills);
        Gui gui = blendingGui.createGui(player);
        gui.open(player);
        return true;
    }
}
