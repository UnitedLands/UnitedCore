package org.unitedlands.skills.abilities;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.ItemActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.LootTable;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.*;

import static org.unitedlands.skills.Utils.canActivate;

public class FishermanAbilities implements Listener {
    private final UnitedSkills unitedSkills;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    private Player player;

    public FishermanAbilities(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onRareCatchFish(PlayerFishEvent event) {
        player = event.getPlayer();
        if (isFisherman()) {
            return;
        }
        Skill rareCatch = new Skill(player, SkillType.RARE_CATCH);
        if (rareCatch.getLevel() == 0) {
            return;
        }
        if (event.getCaught() != null) {
            LootTable rareCatchLoot = new LootTable("rare-catch-loot", rareCatch);
            Biome biome = event.getHook().getLocation().getBlock().getBiome();
            replaceCaughtFish(event.getCaught(), rareCatchLoot.getRandomItem(biome));
        }
    }

    @EventHandler
    public void onTreasureHunterFish(PlayerFishEvent event) {
        player = event.getPlayer();
        if (isFisherman()) {
            return;
        }
        Skill treasureHunter = new Skill(player, SkillType.TREASURE_HUNTER);
        if (treasureHunter.getLevel() == 0) {
            return;
        }
        if (event.getCaught() != null) {
            LootTable treasureHunterTable = new LootTable("treasure-hunter-loot", treasureHunter);
            if (event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
                replaceCaughtFish(event.getCaught(), treasureHunterTable.getRandomItem());
            }
        }
    }

    private void replaceCaughtFish(Entity caught, ItemStack item) {
        if (caught instanceof Item caughtFish) {
            if (item == null) {
                return;
            }

            Entity newItem = caughtFish.getWorld().dropItem(caughtFish.getLocation(), item);
            Bukkit.getScheduler().runTaskLater(unitedSkills, () -> {
                newItem.setVelocity(caughtFish.getVelocity());
                caughtFish.remove();
            }, 2);
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
        if (isFisherman()) {
            return;
        }
        Skill swiftSwimmer = new Skill(player, SkillType.SWIFT_SWIMMER);
        int level = swiftSwimmer.getLevel();
        if (level == 0) {
            return;
        }
        player.setVelocity(player.getLocation().getDirection().multiply(0.2 * level));
    }

    @EventHandler
    public void onAnglerFish(PlayerFishEvent event) {
        player = event.getPlayer();
        if (isFisherman()) {
            return;
        }
        Skill angler = new Skill(player, SkillType.ANGLER);
        if (angler.getLevel() == 0) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(unitedSkills, () -> {
            FishHook hook = event.getHook();
            // decrease wait time by 30% for every level in angler
            int waitTime = (int) (hook.getWaitTime() * (1 - (angler.getLevel() * 0.3)));
            if (angler.getLevel() == 3) {
                // Decrease it by 80% for level 3.
                waitTime = (int) (hook.getWaitTime() * 0.2);
            }

            event.getHook().setWaitTime(waitTime - 20);
        }, 20);
    }

    @EventHandler
    public void onGrappleActivate(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (isFisherman()) {
            return;
        }
        ActiveSkill grapple = new ActiveSkill(player, SkillType.GRAPPLE, cooldowns, durations);
        if (canActivate(event, "FISHING_ROD", grapple)) {
            event.setCancelled(true);
            grapple.activate();
        }
    }
    @EventHandler
    public void onGrapple(PlayerFishEvent event) {
        player = event.getPlayer();
        if (isFisherman()) {
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
        FishHook hook = event.getHook();
        if (hook.hasMetadata("stuckBlock")) {
            hook.removeMetadata("stuckBlock", unitedSkills);
            Objects.requireNonNull(hook.getVehicle()).remove();
            @NotNull Vector direction = hook.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            player.setVelocity(direction.multiply(grapple.getLevel() * 0.8));
            return;
        }
        Location hookLocation = hook.getLocation();
        Block bottomBlock = hookLocation.getBlock().getRelative(0, -1, 0);
        if (!bottomBlock.getType().isSolid() || player.getLocation().getBlock().equals(bottomBlock)) {
            return;
        }

        @NotNull Vector direction = hookLocation.toVector().subtract(player.getLocation().toVector()).normalize();
        player.setVelocity(direction.multiply(grapple.getLevel() * 0.8));
    }

    @EventHandler
    public void onHookStick(ProjectileHitEvent event) {
        if ((event.getEntity() instanceof FishHook fishHook) && (event.getEntity().getShooter() instanceof Player player)) {
            this.player = player;
            ActiveSkill grapple = new ActiveSkill(player, SkillType.GRAPPLE, cooldowns, durations);
            if (!grapple.isActive()) {
                return;
            }
            if (event.getHitBlock() != null) {
                Location hitblock = event.getHitBlock().getLocation().add(0.5, 0, 0.5);
                ArmorStand armorStand = player.getWorld().spawn(hitblock, ArmorStand.class);
                armorStand.addPassenger(fishHook);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStand.setSmall(true);
                armorStand.setArms(false);
                armorStand.setMarker(true);
                armorStand.setBasePlate(false);
                fishHook.setGravity(false);
                fishHook.setMetadata("stuckBlock", new FixedMetadataValue(unitedSkills, ""));
            }
        }

    }

    @EventHandler
    public void onHookedUse(PlayerFishEvent event) {
        player = event.getPlayer();
        if (isFisherman()) {
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
        if (entity instanceof Item || entity instanceof Player ||  entity instanceof ArmorStand) {
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
        BukkitAPIHelper mythicMobsHelper = new BukkitAPIHelper();
        if (mythicMobsHelper.isMythicMob(livingEntity)) {
            return;
        }
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
        if (isFisherman()) {
            return;
        }

        Skill luckyCatch = new Skill(player, SkillType.LUCKY_CATCH);
        if (!luckyCatch.isSuccessful()) {
            return;
        }

        if (!(event.getCaught() instanceof Item item)) {
            return;
        }
        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
            return;
        }

        ItemStack drop = item.getItemStack();
        Entity extraItem = event.getPlayer().getWorld().dropItem(item.getLocation(), drop);

        Bukkit.getScheduler().runTask(unitedSkills, () -> extraItem.setVelocity(item.getVelocity()));

        Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), new ItemActionInfo(drop, ActionType.FISH));
        luckyCatch.notifyActivation();
    }

    @EventHandler
    public void onFishFoodEat(PlayerItemConsumeEvent event) {
        player = event.getPlayer();
        if (isFisherman()) {
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
                || type.equals(Material.COD)
                // Bread is the base item for the sushi custom item.
                || type.equals(Material.BREAD);
    }

    private boolean isFisherman() {
        return !Utils.isInJob(player, "Fisherman");
    }
}
