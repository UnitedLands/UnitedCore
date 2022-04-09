package org.unitedlands.skills;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.skills.brewer.BlendingGui;
import org.unitedlands.skills.commands.BlendCommand;

public final class UnitedSkills extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("blend").setExecutor(new BlendCommand(new BlendingGui(this)));
        saveDefaultConfig();
    }
}
