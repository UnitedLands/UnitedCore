package org.unitedlands.skills.points;

import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.unitedlands.skills.UnitedSkills;

public class JobsListener implements Listener {

    private final UnitedSkills unitedSkills;
    private Player player;
    public JobsListener(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        player = event.getPlayer();
        PlayerConfiguration playerConfiguration = new PlayerConfiguration(unitedSkills, player);

        if (!player.hasPlayedBefore()) {
            playerConfiguration.createFile();
        } else if (!playerConfiguration.getFile().exists()) {
            playerConfiguration.createFile();
        }
    }

    @EventHandler
    public void onJobLevelUp(JobsLevelUpEvent event) {
        player = event.getPlayer().getPlayer();
        PlayerConfiguration playerConfiguration = new PlayerConfiguration(unitedSkills, player);
        playerConfiguration.increaseJobPoints(event.getJob().getName(), 1);
    }
}
