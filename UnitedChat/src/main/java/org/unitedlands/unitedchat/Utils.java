package org.unitedlands.unitedchat;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.unitedlands.unitedchat.player.ChatFeature;

public class Utils {
    public static boolean isChatFeatureEnabled(ChatFeature feature, Player player) {
        if (player == null)
            return false;
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(UnitedChat.getPlugin(), feature.toString());
        // features are on by default, which means it wasn't ever toggled before if there is no key.
        if (!pdc.has(key))
            return true;
        return pdc.get(key, PersistentDataType.INTEGER) == 1;
    }

    public static String canSee(Player player, String string, ChatFeature feature) {
        if (isChatFeatureEnabled(feature, player)) {
            if (feature == ChatFeature.PREFIXES)
                return string; // prefixes are built in with a space
            return string + " "; // other stuff isn't
        }
        else
            return ""; // disabled, return empty.
    }
}
