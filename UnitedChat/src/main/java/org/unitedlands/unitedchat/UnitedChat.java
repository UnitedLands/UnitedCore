package org.unitedlands.unitedchat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.unitedchat.commands.ClearChatCommand;
import org.unitedlands.unitedchat.commands.GradientCommand;
import org.unitedlands.unitedchat.player.PlayerListener;

import java.io.File;

public class UnitedChat extends JavaPlugin {

    private static UnitedChat PLUGIN;

    public UnitedChat() {
        PLUGIN = this;
    }

    public static UnitedChat getPlugin() {
        return PLUGIN;
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("[Exception] PlaceholderAPI is required!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getCommand("gradient").setExecutor(new GradientCommand());
        this.getCommand("cc").setExecutor(new ClearChatCommand());
        saveDefaultConfig();

    }

    public static Component getMessage(String name) {
        String message = PLUGIN.getConfig().getString("messages." + name);
        return MiniMessage.miniMessage().deserialize(message);
    }
}