package org.unitedlands.skills.commands;

import dev.triumphteam.gui.guis.Gui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.brewer.BlendingGui;

import static org.unitedlands.skills.Utils.getMessage;

public class BlendCommand implements CommandExecutor {
    private final BlendingGui blendingGui;

    public BlendCommand(BlendingGui blendingGui) {
        this.blendingGui = blendingGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("united.skills.blend")) {
            player.sendMessage(getMessage("no-permission"));
            return true;
        }
        Gui gui = blendingGui.createGui(player);
        gui.open(player);
        return true;
    }
}
