package me.obito.chromiumchat;

import me.obito.chromiumchat.commands.GradientCmd;
import me.obito.chromiumchat.player.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ChromiumChat extends JavaPlugin {

    @Override
    public void onEnable(){
        Bukkit.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getCommand("gradient").setExecutor(new GradientCmd());
    }

    @Override
    public void onDisable(){

    }
}
