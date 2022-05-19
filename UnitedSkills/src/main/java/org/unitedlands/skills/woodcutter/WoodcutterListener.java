package org.unitedlands.skills.woodcutter;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.events.TreeFallEvent;
import com.songoda.ultimatetimber.manager.SaplingManager;
import com.songoda.ultimatetimber.tree.DetectedTree;
import com.songoda.ultimatetimber.tree.ITreeBlock;
import com.songoda.ultimatetimber.tree.TreeDefinition;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;


public class WoodcutterListener implements Listener {

    private final UnitedSkills unitedSkills;
    private final CoreProtectAPI coreProtect;
    private Player player;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    public WoodcutterListener(UnitedSkills unitedSkills, CoreProtectAPI coreProtect) {
        this.unitedSkills = unitedSkills;
        this.coreProtect = coreProtect;
    }

    @EventHandler
    public void onAxeInteract(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isWoodCutter()) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        if (!event.getItem().getType().toString().contains("AXE")) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        if (!event.getAction().isRightClick()) {
            return;
        }
        ActiveSkill skill = new ActiveSkill(player, SkillType.TREE_FELLER, cooldowns, durations);
        skill.activate();
    }

    @EventHandler
    public void onTreeFell(TreeFallEvent event) {
        player = event.getPlayer();
        if (!isWoodCutter()) {
            event.setCancelled(true);
            return;
        }
        ActiveSkill treeFeller = new ActiveSkill(player, SkillType.TREE_FELLER, cooldowns, durations);
        if (!treeFeller.isActive()) {
            event.setCancelled(true);
            return;
        }
        Skill reforestation = new Skill(player, SkillType.REFORESTATION);
        if (reforestation.getLevel() == 0) {
            return;
        }
        if (reforestation.isSuccessful()) {
            SaplingManager saplingManager = getUltimateTimber().getSaplingManager();
            DetectedTree tree = event.getDetectedTree();
            Bukkit.getScheduler().runTask(unitedSkills, () -> {
                try {
                    Method internalReplant = saplingManager.getClass().getDeclaredMethod("internalReplant", TreeDefinition.class, ITreeBlock.class);
                    internalReplant.setAccessible(true);
                    internalReplant.invoke(saplingManager, tree.getTreeDefinition(), tree.getDetectedTreeBlocks().getInitialLogBlock());
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                if (reforestation.getLevel() != 3) {
                    reforestation.notifyActivation();
                }
            });
        }
    }

    @EventHandler
    public void onPreciseCut(BlockBreakEvent event) {
        player = event.getPlayer();
        if (!isWoodCutter()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.PRECISION_CUTTING);
        Material material = event.getBlock().getType();
        if (!material.toString().contains("LOG")) {
            return;
        }
        if (!Utils.isPlaced(coreProtect, event.getBlock())) {
            return;
        }
        if (skill.isSuccessful()) {
            Utils.multiplyItem(player, new ItemStack(material), 1);
        }
    }

    private UltimateTimber getUltimateTimber() {
        return (UltimateTimber) Bukkit.getPluginManager().getPlugin("UltimateTimber");
    }
    private boolean isWoodCutter() {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            return job.getJob().getName().equals("Lumberjack");
        }
        return false;
    }

}
