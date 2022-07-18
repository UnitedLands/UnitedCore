package org.unitedlands.skills.jobs;

import com.destroystokyo.paper.ParticleBuilder;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.events.TreeFallEvent;
import com.songoda.ultimatetimber.manager.SaplingManager;
import com.songoda.ultimatetimber.tree.DetectedTree;
import com.songoda.ultimatetimber.tree.ITreeBlock;
import com.songoda.ultimatetimber.tree.TreeDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

import static org.unitedlands.skills.Utils.*;


public class WoodcutterAbilities implements Listener {

    private final UnitedSkills unitedSkills;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    private Player player;

    public WoodcutterAbilities(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onAxeInteract(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isWoodCutter()) {
            return;
        }
        ActiveSkill treeFeller = new ActiveSkill(player, SkillType.TREE_FELLER, cooldowns, durations);
        if (canActivate(event, "_AXE", treeFeller)) {
            treeFeller.activate();
            event.setCancelled(true);
        }
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
        // int counter = 0;
        // for (ITreeBlock<Block> treeBlock : event.getDetectedTree().getDetectedTreeBlocks().getLogBlocks()) {
        //     if (counter > 10) {
        //         break;
        //     }
        //     Block block = treeBlock.getBlock();
        //     Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), new BlockActionInfo(block, ActionType.BREAK), block);
        //     counter++;
        // }
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
                ParticleBuilder greenParticle = new ParticleBuilder(Particle.VILLAGER_HAPPY);
                greenParticle.count(25)
                        .location(tree.getDetectedTreeBlocks().getInitialLogBlock().getLocation().toCenterLocation())
                        .spawn();
            });
        }
    }

    @EventHandler
    public void onPreciseCut(BlockBreakEvent event) {
        player = event.getPlayer();
        if (!isWoodCutter()) {
            return;
        }
        Skill precisionCutting = new Skill(player, SkillType.PRECISION_CUTTING);
        Material material = event.getBlock().getType();
        if (!material.toString().contains("LOG")) {
            return;
        }
        if (isPlaced(event.getBlock())) {
            return;
        }
        if (precisionCutting.isSuccessful()) {
            multiplyItem(player, new ItemStack(material), 2);
        }
    }

    private UltimateTimber getUltimateTimber() {
        return (UltimateTimber) Bukkit.getPluginManager().getPlugin("UltimateTimber");
    }

    private boolean isWoodCutter() {
        return isInJob(player, "Woodcutter");
    }

}
