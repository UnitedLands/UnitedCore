package org.unitedlands.unitedchat.player;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
    private final UnitedChat uc;
    private final Formatter formatter;

    public PlayerListener(UnitedChat uc, Formatter formatter) {
        this.uc = uc;
        this.formatter = formatter;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        FileConfiguration config = uc.getConfig();
        List<String> firstMotd = config.getStringList("messages.FirstJoinMotd");
        List<String> motd = config.getStringList("messages.Motd");
        Player p = event.getPlayer();

        if (p.hasPermission("united.chat.gradient")) {
            uc.createPlayerFile(p);
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

        String message = event.getMessage();
        String finalizedMessage = formatter.finalizeMessage(player, message);

        if (uc.getPlayerConfig(player) == null) {
            uc.createPlayerFile(player);
        }

        boolean gradientEnabled = uc.getPlayerConfig(player).getBoolean("GradientEnabled");
        String gradient = uc.getPlayerConfig(player).getString("Gradient");

        if (gradientEnabled && gradient != null) {
            event.setMessage(formatter.gradientMessage(finalizedMessage, gradient));
            return;
        }
        event.setMessage(formatter.gradientMessage(finalizedMessage, "#FFFFFF:#FFFFFF"));
    }

}
