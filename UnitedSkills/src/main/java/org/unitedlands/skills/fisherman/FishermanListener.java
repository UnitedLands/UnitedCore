package org.unitedlands.skills.fisherman;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.LootTable;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FishermanListener implements Listener {
    private final UnitedSkills unitedSkills;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    private Player player;

    public FishermanListener(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onTreasureHunterFish(PlayerFishEvent event) {
        player = event.getPlayer();
        if (!isFisherman()) {
            return;
        }
        Skill treasureHunter = new Skill(player, SkillType.TREASURE_HUNTER);
        if (treasureHunter.getLevel() == 0) {
            return;
        }
        event.getHook().setWaitTime(20);
        if (event.getCaught() != null) {
            if (event.getCaught() instanceof Item caughtFish) {
                LootTable treasureHunterLootTable = new LootTable("treasure-hunter-loot", treasureHunter);
                ItemStack item = treasureHunterLootTable.getRandomItem();
                if (item == null) {
                    return;
                }
                caughtFish.getWorld().dropItem(caughtFish.getLocation(), item);
                List<Entity> entities = caughtFish.getNearbyEntities(1, 1, 1);
                for (Entity entity : entities) {
                    if (entity.equals(caughtFish)) {
                        continue;
                    }
                    Bukkit.getScheduler().runTaskLater(unitedSkills, () -> {
                        entity.setVelocity(caughtFish.getVelocity());
                    }, 2);
                }
                caughtFish.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }
        player = event.getPlayer();
        if (!player.isSwimming()) {
            return;
        }
        if (!isFisherman()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.SWIFT_SWIMMER);
        int level = skill.getLevel();
        if (level == 0) {
            return;
        }
        player.setVelocity(player.getLocation().getDirection().multiply(0.2 * level));
    }

    @EventHandler
    public void onAnglerFish(PlayerFishEvent event) {
        player = event.getPlayer();
        if (!isFisherman()) {
            return;
        }
        Skill angler = new Skill(player, SkillType.ANGLER);
        if (angler.getLevel() == 0) {
            return;
        }

        FishHook hook = event.getHook();
        // decrease wait time by 20% for every level in angler
        int waitTime = (int) (hook.getWaitTime() * (1 - (angler.getLevel() * 0.2)));
        hook.setWaitTime(waitTime);
    }

    @EventHandler
    public void onGrappleActivate(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isFisherman()) {
            return;
        }
        ActiveSkill grapple = new ActiveSkill(player, SkillType.GRAPPLE, cooldowns, durations);
        if (grapple.getLevel() == 0) {
            return;
        }
        if (!event.getAction().isRightClick()) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        if (event.getItem().getType() != Material.FISHING_ROD) {
            return;
        }
        grapple.activate();
    }

    @EventHandler
    public void onGrapple(PlayerFishEvent event) {
        player = event.getPlayer();
        if (!isFisherman()) {
            return;
        }
        ActiveSkill grapple = new ActiveSkill(player, SkillType.GRAPPLE, cooldowns, durations);
        if (!grapple.isActive()) {
            return;
        }
        if (event.getCaught() != null) {
            if (event.getCaught() instanceof LivingEntity caught) {
                Location playerLoc = player.getLocation();
                Location caughtLoc = caught.getLocation();
                Vector direction = caughtLoc.toVector().subtract(playerLoc.toVector());
                player.setVelocity(direction.normalize().multiply(1.2));
            }
            return;
        }
        if (grapple.getLevel() == 1) {
            return;
        }
        Entity hook = event.getHook();
        Location hookLocation = hook.getLocation();
        Block bottomBlock = hookLocation.getBlock().getRelative(0, -1, 0);
        if (!bottomBlock.getType().isSolid() || player.getLocation().getBlock().equals(bottomBlock)) {
            return;
        }

        @NotNull Vector direction = hookLocation.toVector().subtract(player.getLocation().toVector()).normalize();
        player.setVelocity(direction.multiply(grapple.getLevel() * 0.8));
    }


    @EventHandler
    public void onHookedUse(PlayerFishEvent event) {
        player = event.getPlayer();
        if (!isFisherman()) {
            return;
        }
        Skill hooked = new Skill(player, SkillType.HOOKED);
        if (hooked.getLevel() == 0) {
            return;
        }
        Entity entity = event.getCaught();
        if (entity == null) {
            return;
        }
        if (entity instanceof Item || entity instanceof Player) {
            return;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        if (!hooked.isSuccessful()) {
            return;
        }
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) {
            return;
        }

        FishHook hook = event.getHook();
        ItemStack item = equipment.getItemInMainHand();

        if (item.getType() != Material.AIR) {
            equipment.setItemInMainHand(new ItemStack(Material.AIR));
            livingEntity.getWorld().dropItem(hook.getLocation(), item);
            hooked.notifyActivation();
        } else if (hasArmor(equipment)) {
            removeRandomArmor(equipment, hook);
            hooked.notifyActivation();
        }
    }

    private boolean hasArmor(EntityEquipment equipment) {
        for (ItemStack armor : equipment.getArmorContents()) {
            if (armor.getType() != Material.AIR) {
                return true;
            }
        }
        return false;
    }

    private void removeRandomArmor(EntityEquipment equipment, FishHook hook) {
        List<ItemStack> armor = Arrays.asList(equipment.getArmorContents());
        for (ItemStack armorPiece : armor) {
            if (armorPiece == null || armorPiece.getType() == Material.AIR) {
                continue;
            }
            armor.set(armor.indexOf(armorPiece), null);
            hook.getWorld().dropItem(hook.getLocation(), armorPiece);
            break;
        }
        equipment.setArmorContents(armor.toArray(new ItemStack[4]));
    }

    @EventHandler
    public void onLuckyCatch(PlayerFishEvent event) {
        player = event.getPlayer();
        if (!isFisherman()) {
            return;
        }

        Skill luckyCatch = new Skill(player, SkillType.LUCKY_CATCH);
        if (!luckyCatch.isSuccessful()) {
            return;
        }

        if (!(event.getCaught() instanceof Item item)) {
            return;
        }

        ItemStack drop = item.getItemStack();

        if (drop.getMaxStackSize() > 0) {
            drop.setAmount(drop.getAmount() * 2);
            luckyCatch.notifyActivation();
        }

    }

    @EventHandler
    public void onFishFoodEat(PlayerItemConsumeEvent event) {
        player = event.getPlayer();
        if (!isFisherman()) {
            return;
        }
        Skill pescatarian = new Skill(player, SkillType.PESCATARIAN);
        if (pescatarian.isSuccessful()) {
            if (isFishFood(event.getItem())) {
                int level = pescatarian.getLevel();
                float saturation = player.getSaturation();
                int foodLevel = player.getFoodLevel();
                player.setSaturation(saturation + level);
                player.setFoodLevel(foodLevel + level);
            }
        }
    }

    private boolean isFishFood(ItemStack item) {
        Material type = item.getType();
        return type.equals(Material.COOKED_COD)
                || type.equals(Material.COOKED_SALMON)
                || type.equals(Material.SALMON)
                || type.equals(Material.COD);
    }

    private boolean isFisherman() {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            return job.getJob().getName().equals("Fisherman");
        }
        return false;
    }
}
