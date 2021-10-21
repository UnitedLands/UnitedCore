package me.obito.chromium;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GradientListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){

        String mes = e.getMessage();
        e.setMessage(ChatColor.of("#123456") + mes);

    }

}
