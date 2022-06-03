package org.unitedlands.skills.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;

public class ReloadCommand implements CommandExecutor {

    private final UnitedSkills unitedSkills;

    public ReloadCommand(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("united.skills.admin")) {
            unitedSkills.reloadConfig();
            sender.sendMessage(Component.text("Configuration reloaded!", NamedTextColor.GREEN));
        }
        return false;
    }
}
