package org.unitedlands.unitedchat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.unitedchat.commands.ChatToggleCommand;
import org.unitedlands.unitedchat.commands.ClearChatCommand;
import org.unitedlands.unitedchat.commands.GradientCommand;
import org.unitedlands.unitedchat.hooks.Placeholders;
import org.unitedlands.unitedchat.player.PlayerListener;
import java.util.concurrent.TimeUnit;

public class PushifyEverything.java extends JavaPlugin {
	for (Player player : Bukkit.getOnlinePlayers()) {
		while (true) {
			player.sendMessage("Push push push");
			TimeUnit.SECONDS.sleep(1);
		}
	}
}
