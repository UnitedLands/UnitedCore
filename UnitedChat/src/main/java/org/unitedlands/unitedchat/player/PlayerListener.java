package org.unitedlands.unitedchat.player;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.unitedlands.unitedchat.UnitedChat;

import java.net.URL;
import java.util.List;


public class PlayerListener implements Listener {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer sectionRGB = LegacyComponentSerializer.builder().character('§').hexCharacter('#').hexColors().build();
    private final UnitedChat uc;

    public PlayerListener(UnitedChat uc) {
        this.uc = uc;
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

        String message = finalizeGradientMessage(event.getMessage());
        String serializedMessage;

        boolean gradientEnabled = uc.getPlayerConfig(player).getBoolean("GradientEnabled");
        if (gradientEnabled) {
            String gradient = uc.getPlayerConfig(player).getString("Gradient");
            if (gradient == null) {
                return;
            }
            Component gradientedComponent = miniMessage.deserialize("<gradient:" + gradient + ">" + message + "</gradient>");
            serializedMessage = sectionRGB.serialize(gradientedComponent);
        } else {
            Component messageWithPings = miniMessage.deserialize(message);
            serializedMessage = LegacyComponentSerializer.legacyAmpersand().serialize(messageWithPings);
        }
        event.setMessage(color(serializedMessage));
    }

    private String finalizeGradientMessage(String message) {
        String[] parts = message.split(" ");
        String excludedPart;
        String pingedPlayer;
        for (String part : parts) {
            if (part.startsWith(":") && part.endsWith(":")) {
                excludedPart = "<white>" + part + "</white>";
                message = message.replace(part, excludedPart);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (part.equals(player.getName())) {
                    pingedPlayer = "<yellow><i>" + player.getName() + "</i></yellow>";
                    message = message.replace(part, pingedPlayer);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
            }
            if (isURL(part)) {
                String highlightedURL = "<click:open_url:'" + part + "'><u><color:#10afee>" +
                        part + "</color:#10afee></u></click>";
                message = message.replace(part, highlightedURL);
            }
        }
        return message;
    }

    private String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
