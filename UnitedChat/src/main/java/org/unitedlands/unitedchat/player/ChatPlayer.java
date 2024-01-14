package org.unitedlands.unitedchat.player;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.unitedchat.UnitedChat;

import java.io.File;
import java.util.UUID;

public class ChatPlayer {
    private final UUID uuid;

    public ChatPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public String getGradient() {
        Player player = getPlayer();
        PersistentDataContainer pdc = getPDC(player);
        if (!pdc.has(getKey("gradient"))) {
            return null;
        }
        return pdc.get(getKey("gradient"), PersistentDataType.STRING);
    }

    public void setGradient(String gradient) {
        Player player = getPlayer();
        player.getPersistentDataContainer().set(getKey("gradient"), PersistentDataType.STRING, gradient);
    }

    public boolean isGradientEnabled() {
        Player player = getPlayer();
        PersistentDataContainer pdc = getPDC(player);
        if (getGradient() == null) {
            return false;
        }
        if (!pdc.has(getKey("gradient-enabled"))) {
            return false;
        }
        return Boolean.parseBoolean(pdc.get(getKey("gradient-enabled"), PersistentDataType.STRING));
    }

    public void toggleChatFeature(ChatFeature feature, boolean toggle) {
        Player player = getPlayer();
        if (player == null)
            return;
        PersistentDataContainer pdc = getPDC(player);
        NamespacedKey key = new NamespacedKey(UnitedChat.getPlugin(), feature.toString());
        if (toggle) {
            pdc.set(key, PersistentDataType.INTEGER, 1); // 1 == true, 0 == false
        } else {
            pdc.set(key, PersistentDataType.INTEGER, 0);
        }
    }

    @NotNull
    private static PersistentDataContainer getPDC(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc;
    }

    @Nullable
    private Player getPlayer() {
        Player player = Bukkit.getPlayer(uuid);
        return player;
    }

    public void setGradientEnabled(boolean toggle) {
        Player player = getPlayer();
        player.getPersistentDataContainer().set(getKey("gradient-enabled"), PersistentDataType.STRING, toggle + "");
    }


    private NamespacedKey getKey(String name) {
        return new NamespacedKey(UnitedChat.getPlugin(), name);
    }

}
