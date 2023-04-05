package org.unitedlands.unitedchat.player;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.unitedlands.unitedchat.Formatter;
import org.unitedlands.unitedchat.UnitedChat;

import java.util.List;


public class PlayerListener implements Listener {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Formatter formatter = new Formatter();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        FileConfiguration config = UnitedChat.getPlugin().getConfig();
        List<String> firstMotd = config.getStringList("messages.first-join-motd");
        List<String> motd = config.getStringList("messages.motd");
        Player p = event.getPlayer();

        if (p.hasPermission("united.chat.gradient")) {
            ChatPlayer chatPlayer = new ChatPlayer(p.getUniqueId());
            if (chatPlayer.getPlayerConfig() == null)
                chatPlayer.createPlayerFile();
        }

        if (p.hasPlayedBefore()) {
            for (String s : motd) {
                p.sendMessage(miniMessage.deserialize(PlaceholderAPI.setPlaceholders(p, s)));
            }
        } else {
            for (String s : firstMotd) {
                p.sendMessage(miniMessage.deserialize(PlaceholderAPI.setPlaceholders(p, s)));
            }
        }

    }

    @EventHandler
    public void onChat(AsyncChatHookEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("united.chat.gradient")) {
            return;
        }

        if (!event.getChannel().getName().equals("general")) {
            return;
        }

        ChatPlayer chatPlayer = new ChatPlayer(player.getUniqueId());
        String message = event.getMessage();
        String finalizedMessage = formatter.finalizeMessage(player, message);

        if (chatPlayer.getPlayerConfig() == null) {
            chatPlayer.createPlayerFile();
        }

        if (chatPlayer.isGradientEnabled()) {
            event.setMessage(formatter.gradientMessage(finalizedMessage, chatPlayer.getGradient()));
            return;
        }
        event.setMessage(formatter.gradientMessage(finalizedMessage, "#FFFFFF:#FFFFFF"));
    }

}
