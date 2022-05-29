package org.unitedlands.skills.safarinets;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import de.Linus122.SafariNet.API.Listener;
import de.Linus122.SafariNet.API.Status;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.List;

public class TraffickerListener implements Listener {
    @Override
    public void playerCatchEntity(Player player, Entity entity, Status status) {
        status.setCancelled(!canUse(player, entity));
    }

    @Override
    public void playerReleaseEntity(Player player, Entity entity, Status status) {
        status.setCancelled(!canUse(player, entity));
    }

    private boolean canUse(Player player, Entity entity) {
        if (!isHunter(player)) {
            player.sendMessage(Utils.getMessage("must-be-hunter"));
            return false;
        }
        Skill trafficker = new Skill(player, SkillType.TRAFFICKER);
        return trafficker.getLevel() != 0 && isHostile(entity);
    }

    private boolean isHostile(Entity entity) {
        FileConfiguration config = Utils.getUnitedSkills().getConfig();
        List<String> hostileEntities = config.getStringList("trafficker-mobs");
        for (String entityType : hostileEntities) {
            if (entity.getType().toString().equals(entityType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHunter(Player player) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            return job.getJob().getName().equals("Hunter");
        }
        return false;
    }
}
