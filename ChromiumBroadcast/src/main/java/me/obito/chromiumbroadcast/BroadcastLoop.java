/*package me.obito.chromiumbroadcast;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class BroadcastLoop extends BukkitRunnable {

    int index = -1;
    @Override
    public void run() {
        index++;
        if(ChromiumBroadcast.getMaxMessages() == index) {
            index = 0;
        }
        Bukkit.getServer().broadcastMessage(ChromiumBroadcast.getMessage(index));
    }

}*/
