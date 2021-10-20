package me.obito.chromium;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.setJoinMessage(ChatColor.of("#63F528") + "a");
    }

}
