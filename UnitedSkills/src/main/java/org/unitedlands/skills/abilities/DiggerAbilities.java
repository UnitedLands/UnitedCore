package org.unitedlands.skills.abilities;

import com.destroystokyo.paper.ParticleBuilder;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.skills.LootTable;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.unitedlands.skills.Utils.canActivate;

public class DiggerAbilities implements Listener {
    private static final int[][] MINING_COORD_OFFSETS = new int[][]{{0, 0}, {0, -1}, {-1, 0}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1},};
    private final UnitedSkills unitedSkills;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    private Player player;


    public DiggerAbilities(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMineralFinderBreak(BlockBreakEvent event) {
        player = event.getPlayer();
        if (event.isCancelled()) return;
        if (isDigger()) {
            return;
        }
        Skill mineralFinder = new Skill(player, SkillType.MINERAL_FINDER);
        if (mineralFinder.getLevel() == 0) {
            return;
        }

        LootTable mineralFinderLootTable = new LootTable("mineral-finder-loot", mineralFinder);
        Block block = event.getBlock();
        if (Utils.isPlaced(block)) return;

        ItemStack randomItem = mineralFinderLootTable.getRandomItem(block);
        if (randomItem != null) {
            block.getWorld().dropItem(block.getLocation(), randomItem);
            mineralFinder.notifyActivation();
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onArchaeologistBlockBreak(BlockBreakEvent event) {
        player = event.getPlayer();
        if (event.isCancelled()) return;
        if (isDigger()) {
            return;
        }
        Skill archaeologist = new Skill(player, SkillType.ARCHAEOLOGIST);
        if (archaeologist.getLevel() == 0) {
            return;
        }

        LootTable archaeologistLootTable = new LootTable("archaeologist-loot", archaeologist);
        Block block = event.getBlock();
        if (Utils.isPlaced(block)) return;

        ItemStack randomItem = archaeologistLootTable.getRandomItem(block);
        if (randomItem != null) {
            block.getWorld().dropItem(block.getLocation(), randomItem);
            archaeologist.sendActivationActionBar();
        }
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        player = event.getPlayer();
        if (isDigger()) {
            return;
        }
        Skill excavator = new Skill(player, SkillType.EXCAVATOR);
        if (Utils.isPlaced(event.getBlock())) {
            return;
        }
        if (excavator.isSuccessful()) {
            List<Item> items = event.getItems();
            for (Item item : items) {
                if (Objects.requireNonNull(unitedSkills.getConfig().getList("excavator-items")).contains(item.getItemStack().getType().toString())) {
                    Utils.multiplyItem(player, item.getItemStack(), 1);
                }
            }
        }
    }

    @EventHandler
    public void onTunnellerActivate(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (isDigger()) {
            return;
        }
        ActiveSkill tunneller = new ActiveSkill(player, SkillType.TUNNELLER, cooldowns, durations);
        if (canActivate(event, "SHOVEL", tunneller)) {
            tunneller.activate();
        }
    }

    @EventHandler
    public void onRefinerActivate(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (isDigger()) {
            return;
        }
        ActiveSkill refiner = new ActiveSkill(player, SkillType.REFINER, cooldowns, durations);
        if (canActivate(event, "SHOVEL", refiner)) {
            refiner.activate();
        }
    }

    @EventHandler
    public void onRefineBreak(BlockDropItemEvent event) {
        player = event.getPlayer();
        if (isDigger()) {
            return;
        }
        ActiveSkill refiner = new ActiveSkill(player, SkillType.REFINER, cooldowns, durations);
        if (!refiner.isActive()) {
            return;
        }
        int level = refiner.getLevel();
        Block block = event.getBlockState().getBlock();
        Material blockType = event.getBlockState().getType();
        if (blockType.equals(Material.CLAY) && level == 3) {
            block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.BRICK, 4));
            event.setCancelled(true);
        } else if (blockType.toString().contains("CONCRETE_POWDER") && level >= 2) {
            Material concreteMaterial = Material.getMaterial(blockType.toString().replace("_POWDER", ""));
            assert concreteMaterial != null;
            block.getWorld().dropItem(block.getLocation(), new ItemStack(concreteMaterial));
            event.setCancelled(true);
        } else if (blockType.equals(Material.SAND) && level >= 1) {
            block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.GLASS));
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTunnelBreak(BlockBreakEvent event) {
        player = event.getPlayer();
        if (isDigger()) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        ActiveSkill tunneller = new ActiveSkill(player, SkillType.TUNNELLER, cooldowns, durations);
        if (!tunneller.isActive()) {
            return;
        }
        Block block = event.getBlock();
        createTunnel(block);
    }

    private void createTunnel(Block block) {
        BlockFace face = getFacingBlockFace(player);
        boolean isY = face != null && block.getRelative(face.getOppositeFace()).isEmpty();
        boolean isZ = face == BlockFace.EAST || face == BlockFace.WEST;

        int blocksToBreak = 1;
        ActiveSkill tunneller = new ActiveSkill(player, SkillType.TUNNELLER, cooldowns, durations);
        int level = tunneller.getLevel();
        if (level == 1) blocksToBreak = 2;
        else if (level == 2) blocksToBreak = 5;
        else if (level == 3) blocksToBreak = 9;

        for (int i = 0; i < blocksToBreak; i++) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!item.getType().toString().contains("SHOVEL")) {
                return;
            }
            int xAdd = MINING_COORD_OFFSETS[i][0];
            int zAdd = MINING_COORD_OFFSETS[i][1];
            Block blockAdd;
            if (isY) {
                blockAdd = block.getLocation().clone().add(isZ ? 0 : xAdd, zAdd, isZ ? xAdd : 0).getBlock();
            } else {
                blockAdd = block.getLocation().clone().add(xAdd, 0, zAdd).getBlock();
            }
            // Skip blocks that should not be mined
            if (blockAdd.equals(block)) continue;
            // Skip any stuff that shovels wouldn't drop, and make sure the level is 1 (i.e. can't break stone)
            if (blockAdd.getDrops(item).isEmpty()) continue;
            if (blockAdd.isLiquid()) continue;

            Material addType = blockAdd.getType();

            // Some extra block checks.
            if (addType.isInteractable() && !(addType.equals(Material.REDSTONE_ORE)
                    || addType.equals(Material.DEEPSLATE_REDSTONE_ORE))) continue;
            if (addType == Material.BEDROCK || addType == Material.END_PORTAL || addType == Material.END_PORTAL_FRAME)
                continue;
            if (addType == Material.OBSIDIAN && addType != block.getType()) continue;

            spawnBlockBreakParticles(blockAdd);
            Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), new BlockActionInfo(block, ActionType.BREAK), block);
            block.breakNaturally();
        }
    }

    private void spawnBlockBreakParticles(Block block) {
        ParticleBuilder particle = new ParticleBuilder(Particle.BLOCK_CRACK);
        particle.data(block.getBlockData())
                .location(block.getLocation())
                .count(100)
                .spawn();
    }

    private BlockFace getFacingBlockFace(Entity entity) {
        float yaw = Math.round(entity.getLocation().getYaw() / 90F);

        if ((yaw == -4.0F) || (yaw == 0.0F) || (yaw == 4.0F)) {
            return BlockFace.SOUTH;
        }
        if ((yaw == -1.0F) || (yaw == 3.0F)) {
            return BlockFace.EAST;
        }
        if ((yaw == -2.0F) || (yaw == 2.0F)) {
            return BlockFace.NORTH;
        }
        if ((yaw == -3.0F) || (yaw == 1.0F)) {
            return BlockFace.WEST;
        }
        return null;
    }

    private boolean isDigger() {
        return !Utils.isInJob(player, "Digger");
    }
}
