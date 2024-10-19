package org.unitedlands.skills.safarinets;

import de.Linus122.SafariNet.API.Listener;
import de.Linus122.SafariNet.API.Status;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.List;

import static org.unitedlands.skills.Utils.isInJob;

public class SafariNetListener implements Listener {
    private final UnitedSkills unitedskills;

    public SafariNetListener(UnitedSkills unitedSkills) {
        this.unitedskills = unitedSkills;
    }

    @Override
    public void playerCatchEntity(Player player, Entity entity, Status status) {
        status.setCancelled(!canUse(player, entity));
    }

    @Override
    public void playerReleaseEntity(Player player, Entity entity, Status status) {

    }

    private boolean canUse(Player player, Entity entity) {
        // We now know we're dealing with a passive mob.
        if (isInList(entity, "wrangler-mobs")) {
            // They aren't a farmer, so return with an error message.
            if (!isInJob(player, "Farmer")) {
                player.sendMessage(Utils.getMessage("must-be-farmer"));
                return false;
            }
            Skill wrangler = new Skill(player, SkillType.WRANGLER);
            // They don't have the skill, so return with an error message
            if (wrangler.getLevel() == 0) {
                player.sendMessage(Utils.getMessage("must-be-farmer"));
                return false;
            }
            // They can use the net if it's a passive entity, they're a farmer, and they have the skill.
            return true;
        }
        // It wasn't a passive mob, so we're double-checking to see if it's a valid hostile mob
        if (isInList(entity, "trafficker-mobs")) {
            // They aren't a hunter, return an error message
            if (!isInJob(player, "Hunter")) {
                player.sendMessage(Utils.getMessage("must-be-hunter"));
                return false;
            }
            Skill trafficker = new Skill(player, SkillType.TRAFFICKER);
            // Trafficker level is 0, error message the player.
            if (trafficker.getLevel() == 0) {
                player.sendMessage(Utils.getMessage("must-be-hunter"));
                return false;
            }
            return true;
        }
        // The mob is neither included in the hostile list nor the passive list, they're not catchable at all.
        return false;
    }

    private boolean isInList(Entity entity, String listName) {
        FileConfiguration config = unitedskills.getConfig();
        List<String> entities = config.getStringList(listName);
        return entities.contains(entity.getType().toString());
    }
}
