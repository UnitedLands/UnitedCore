package org.unitedlands.skills.points;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.gamingmesh.jobs.api.JobsPrePaymentEvent;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
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
        String name = event.getJob().getName();
        if (name.equals("Hunter") && getJobsLevel(name) % 5 == 0) {
            playerConfiguration.increaseJobPoints(name, 2);
            return;
        }
        playerConfiguration.increaseJobPoints(name, 1);
    }

    @EventHandler
    public void onJobPayment(JobsPrePaymentEvent event) {
        String jobName = event.getJob().getName();
        if (!jobName.equals("Hunter")) {
            return;
        }
        if (event.getEntity() == null) {
            return;
        }
        if (event.getEntity().hasMetadata("spawner-mob")) {
            event.setAmount(event.getAmount() * 0.25);
        } else {
            event.setAmount(event.getAmount());
        }
    }

    private int getJobsLevel(String jobName) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player.getPlayer());
        if (jobsPlayer == null) {
            return 0;
        }
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            if (job.getJob().getName().equals(jobName)) {
                return job.getLevel();
            }
        }
        return 0;
    }
}
