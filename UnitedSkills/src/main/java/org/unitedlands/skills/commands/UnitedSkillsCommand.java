package org.unitedlands.skills.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.skill.SkillFile;

public class UnitedSkillsCommand implements CommandExecutor {

    private final UnitedSkills unitedSkills;

    public UnitedSkillsCommand(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("united.skills.admin")) {
            if (args[0].equals("reload")) {
                unitedSkills.reloadConfig();
                SkillFile skillFile = new SkillFile(unitedSkills);
                skillFile.reloadConfig();
                sender.sendMessage(Component.text("Configuration reloaded!", NamedTextColor.GREEN));
            }
        }
        return false;
    }
}
