package org.unitedlands.skills.jobs;

import com.destroystokyo.paper.ParticleBuilder;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.gamingmesh.jobs.container.blockOwnerShip.BlockOwnerShip;
import com.gamingmesh.jobs.container.blockOwnerShip.BlockTypes;
import dev.lone.itemsadder.api.CustomStack;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.guis.BlendingGui;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.unitedlands.skills.Utils.*;

public class BrewerAbilities implements Listener {
    private final UnitedSkills unitedSkills;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    private Player player;

    public BrewerAbilities(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        player = (Player) event.getEntity().getShooter();
        if (!isBrewer()) {
            return;
        }
        Skill splashBoost = new Skill(player, SkillType.SPLASH_BOOST);
        if (splashBoost.getLevel() == 0) {
            return;
        }
        AreaEffectCloud cloud = event.getAreaEffectCloud();
        cloud.setRadius((float) (cloud.getRadius() * (1 + (splashBoost.getLevel() * 0.1))));
        ActiveSkill fortification = new ActiveSkill(player, SkillType.FORTIFICATION, cooldowns, durations);
        if (fortification.isActive()) {
            PotionData basePotionData = cloud.getBasePotionData();
            PotionMeta potionMeta = event.getEntity().getPotionMeta();
            PotionEffect amplifiedEffect = basePotionData.getType().getEffectType().createEffect(getPotionDuration(basePotionData), getAmplifier(potionMeta));
            cloud.addCustomEffect(amplifiedEffect, true);
        }
    }

    @EventHandler
    public void onExpandedPotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        player = (Player) event.getEntity().getShooter();
        if (!isBrewer()) {
            return;
        }
        Skill splashBoost = new Skill(player, SkillType.SPLASH_BOOST);
        if (splashBoost.getLevel() == 0) {
            return;
        }
        ParticleBuilder particle = new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE);
        particle.count(splashBoost.getLevel() * 2);
        particle.force();
        particle.location(event.getEntity().getLocation());

        float radius = (float) (4.25 * (1 + (splashBoost.getLevel() * 0.1)));
        PotionMeta potionMeta = event.getPotion().getPotionMeta();
        PotionData potionData = potionMeta.getBasePotionData();

        int baseDuration = getPotionDuration(potionData);
        int baseAmplifier = getAmplifier(potionMeta);

        Location potionLocation = event.getPotion().getLocation();
        potionLocation.getNearbyLivingEntities(radius, radius, radius).forEach(entity -> {
            double distance = getDistance(potionLocation, entity.getLocation());
            int duration = (int) (baseDuration - (baseDuration * (distance * 0.1)));
            int amplifier = (int) Math.round((baseAmplifier - (baseAmplifier * (distance * 0.1))));
            if (potionData.getType().getEffectType() == null) {
                if (potionMeta.hasCustomEffects()) {
                    potionMeta.getCustomEffects().forEach(effect -> entity.addPotionEffect(new PotionEffect(effect.getType(), duration, baseAmplifier)));
                    particle.spawn();
                    return;
                }
                return;
            }
            PotionEffect potionEffect = new PotionEffect(potionData.getType().getEffectType(), duration, amplifier);
            entity.addPotionEffect(potionEffect);
        });
        particle.spawn();
    }

    @EventHandler
    public void onHarmfulPotion(EntityPotionEffectEvent event) {
        PotionEffect effect = event.getNewEffect();
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        player = (Player) entity;
        if (!isBrewer()) {
            return;
        }
        if (effect == null) {
            return;
        }
        if (!isHarmfulEffect(effect)) {
            return;
        }
        Skill exposureTherapy = new Skill(player, SkillType.EXPOSURE_THERAPY);
        if (exposureTherapy.isSuccessful()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onFortificationActivate(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isBrewer()) {
            return;
        }
        ActiveSkill fortification = new ActiveSkill(player, SkillType.FORTIFICATION, cooldowns, durations);
        if (canActivate(event, "POTION", fortification)) {
            fortification.activate();
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        player = event.getPlayer();
        if (!isBrewer()) {
            return;
        }

        ItemStack item = event.getItem();
        if (!item.getType().equals(Material.POTION)) {
            return;
        }
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        PotionData potionData = potionMeta.getBasePotionData();
        final PotionType potionType = potionData.getType();
        Skill assistedHealing = new Skill(player, SkillType.ASSISTED_HEALING);
        if (potionType.equals(PotionType.INSTANT_HEAL) && assistedHealing.getLevel() > 0) {
            increaseHealingEffect(potionMeta);
        }

        Skill exposureTherapy = new Skill(player, SkillType.EXPOSURE_THERAPY);
        if (potionType.equals(PotionType.INSTANT_DAMAGE)) {
            if (exposureTherapy.isSuccessful()) {
                player.getInventory().remove(item);
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
                event.setCancelled(true);
            }
        }

        ActiveSkill fortification = new ActiveSkill(player, SkillType.FORTIFICATION, cooldowns, durations);
        if (fortification.isActive()) {
            if (potionMeta.hasCustomEffects()) {
                potionMeta.getCustomEffects().forEach(effect -> {
                    if (!canFortify(potionMeta)) {
                        return;
                    }
                    PotionEffect potionEffect = new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier() + 1);
                    player.addPotionEffect(potionEffect);
                });
            } else {
                if (potionType.getEffectType() == null) {
                    return;
                }
                if (!canFortify(potionMeta)) {
                    return;
                }
                PotionEffect potionEffect = new PotionEffect(potionType.getEffectType(), getPotionDuration(potionData), getAmplifier(potionMeta));
                player.addPotionEffect(potionEffect);
            }

            event.setCancelled(true);
            player.getInventory().remove(item);
            player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
        }
    }

    @EventHandler
    public void onPotionBrew(BrewEvent event) {
        Block block = event.getBlock();
        BrewingStand brewingStand = (BrewingStand) block.getState();
        player = getBrewingStandOwner(block);
        if (player == null) {
            return;
        }
        Skill qualityIngredients = new Skill(player, SkillType.QUALITY_INGREDIENTS);
        if (qualityIngredients.getLevel() > 0) {
            return;
        }
        for (ItemStack item : event.getContents()) {
            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
            PotionData basePotionData = potionMeta.getBasePotionData();

            if (basePotionData.isUpgraded()) {
                return;
            }
            PotionType potionType = basePotionData.getType();
            PotionData potionData = new PotionData(potionType, basePotionData.isExtended(), true);
            potionMeta.setBasePotionData(potionData);
            item.setItemMeta(potionMeta);
            brewingStand.update();
        }
    }

    @EventHandler
    public void onBrewingStart(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!inventory.getType().equals(InventoryType.BREWING)) {
            return;
        }
        player = (Player) event.getWhoClicked();
        if (!isBrewer()) {
            return;
        }
        Skill modifiedHardware = new Skill(player, SkillType.MODIFIED_HARDWARE);
        if (modifiedHardware.getLevel() == 0) {
            return;
        }
        Block brewingStandBlock = inventory.getLocation().getBlock();
        BrewingStand brewingStand = (BrewingStand) brewingStandBlock.getState(false);
        if (!ownsBrewingStand(brewingStandBlock)) {
            return;
        }
        if (!canStartBrewing(brewingStand)) {
            return;
        }

        unitedSkills.getServer().getScheduler().runTaskLater(unitedSkills, task -> {
            if (!(brewingStand instanceof BrewingStand)) {
                return;
            }
            if (!canStartBrewing(brewingStand)) {
                return;
            }
            if (brewingStand.getBrewingTime() == 0) {
                return;
            }
            int defaultTime = 20;
            int brewingTime = (int) (defaultTime * (1 - (modifiedHardware.getLevel() * 0.10)));
            brewingStand.setBrewingTime(brewingTime * 20);
            brewingStand.update();
        }, 2);
    }

    @EventHandler
    public void onMilkPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Bukkit.broadcastMessage("1");
        if (isMilkPotion(item, "milk_potion")) {
            Bukkit.broadcastMessage("2");
            Player player = event.getPlayer();
            removeAllEffects(player);
        }
    }
    @EventHandler
    public void onMilkPotionSplash(PotionSplashEvent event) {
        ItemStack item = event.getPotion().getItem();
        if (isMilkPotion(item, "splash_milk_potion")) {
            for (Entity entity : event.getPotion().getNearbyEntities(2, 2, 2)) {
                if (entity instanceof LivingEntity livingEntity) {
                    removeAllEffects(livingEntity);
                }
            }
        }
    }

    @EventHandler
    public void onLingeringMilkPotionSplash(LingeringPotionSplashEvent event) {
        ItemStack item = event.getEntity().getItem();
        if (isMilkPotion(item, "lingering_milk_potion")) {
            AreaEffectCloud cloud = event.getAreaEffectCloud();
            cloud.setDuration(200);
            Location location = event.getHitBlock().getLocation();
            if (location == null) {
                location = event.getHitEntity().getLocation();
            }
            for (Entity entity : location.getNearbyLivingEntities(2, 2, 2)) {
                Bukkit.broadcastMessage(event.getEntity().getNearbyEntities(2, 2, 2).toString());
                if (entity instanceof LivingEntity livingEntity) {
                    removeAllEffects(livingEntity);
                }
            }
        }
    }
    @EventHandler
    public void onMilkArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow) {
            if (isMilkPotion(arrow.getItemStack(), "milk_arrow")) {
                Entity entity = event.getHitEntity();
                if (entity != null) {
                    if (entity instanceof LivingEntity livingEntity) {
                        removeAllEffects(livingEntity);
                    }
                }
            }
        }
    }

    private boolean isMilkPotion(ItemStack item, String milkPotionName) {
        return CustomStack.getInstance("unitedlands:" +milkPotionName).getItemStack().equals(item);
    }
    private void removeAllEffects(LivingEntity entity) {
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            entity.removePotionEffect(effect.getType());
        }
    }

    private boolean canStartBrewing(BrewingStand brewingStand) {
        if (hasBottleOrPotion(brewingStand.getInventory())) return true;
        if (hasBrewingItem(brewingStand.getInventory())) return true;
        if (hasBlazePowder(brewingStand)) return true;
        return false;
    }

    private boolean hasBottleOrPotion(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }
            if (item.getType().equals(Material.POTION) || item.getType().equals(Material.SPLASH_POTION)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBrewingItem(Inventory inventory) {
        FileConfiguration config = unitedSkills.getConfig();
        @NotNull List<String> brewingItems = config.getStringList("brewing-items");
        ItemStack item = inventory.getItem(3);
        if (item == null) {
            return false;
        }
        return brewingItems.contains(item.getType().toString());
    }

    private boolean hasBlazePowder(BrewingStand brewingStand) {
        ItemStack blazePowderSlot = brewingStand.getInventory().getItem(3);
        if (brewingStand.getFuelLevel() > 0) {
            return true;
        }
        if (blazePowderSlot == null) {
            return false;
        }
        return blazePowderSlot.getType().equals(Material.BLAZE_POWDER);
    }

    private Player getBrewingStandOwner(Block block) {
        if (block.hasMetadata("jobsBrewingOwner")) {
            BlockOwnerShip blockOwner = getJobs().getBlockOwnerShip(BlockTypes.BREWING_STAND).orElse(null);
            List<MetadataValue> data = blockOwner.getBlockMetadatas(block);
            if (data.isEmpty()) {
                return null;
            }
            MetadataValue value = data.get(0);
            String uuid = value.asString();
            Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        }
        return null;
    }

    private boolean ownsBrewingStand(Block block) {
        if (block.hasMetadata("jobsBrewingOwner")) {
            BlockOwnerShip blockOwner = getJobs().getBlockOwnerShip(BlockTypes.BREWING_STAND).orElse(null);
            List<MetadataValue> data = blockOwner.getBlockMetadatas(block);
            if (data.isEmpty()) {
                return false;
            }
            MetadataValue value = data.get(0);
            String uuid = value.asString();
            return uuid.equals(player.getUniqueId().toString());
        }
        return false;
    }

    @EventHandler
    public void onBrewingStandOpen(InventoryOpenEvent event) {
        player = (Player) event.getPlayer();
        if (!event.getInventory().getType().equals(InventoryType.BREWING)) {
            return;
        }
        if (!isBrewer()) {
            return;
        }
        if (player.isSneaking()) {
            event.setCancelled(true);
            if (!player.hasPermission("united.skills.blend")) {
                player.sendMessage(getMessage("no-permission"));
                return;
            }
            BlendingGui blendingGui = new BlendingGui(unitedSkills);
            Gui gui = blendingGui.createGui(player);
            gui.open(player);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        PotionMeta potionMeta = event.getPotion().getPotionMeta();
        PotionEffectType potionType = potionMeta.getBasePotionData().getType().getEffectType();
        for (Entity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player)) {
                return;
            }
            player = (Player) entity;
            if (!isBrewer()) {
                return;
            }

            Skill exposureTherapy = new Skill(player, SkillType.EXPOSURE_THERAPY);
            Skill assistedHealing = new Skill(player, SkillType.ASSISTED_HEALING);

            if (potionType == null) {
                if (potionMeta.hasCustomEffects()) {
                    potionMeta.getCustomEffects().forEach(effect -> {
                        if (effect.getType().equals(PotionEffectType.HARM)) {
                            if (exposureTherapy.isSuccessful()) {
                                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
                                event.setIntensity(player, 0);
                            }
                            return;
                        }
                        if (effect.getType().equals(PotionEffectType.HEAL)) {
                            if (assistedHealing.getLevel() > 0) {
                                event.setIntensity(player, 0);
                                increaseHealingEffect(potionMeta);
                            }
                        }
                    });
                }
                return;
            }
            if (potionType.equals(PotionEffectType.HARM)) {
                if (exposureTherapy.isSuccessful()) {
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
                    event.setIntensity(player, 0);
                }
            }

            if (assistedHealing.getLevel() > 0) {
                if (potionType.equals(PotionEffectType.HEAL)) {
                    event.setIntensity(player, 0);
                    increaseHealingEffect(potionMeta);
                    return;
                }
            }
        }
    }

    private void increaseHealingEffect(PotionMeta potionMeta) {
        Skill skill = new Skill(player, SkillType.ASSISTED_HEALING);
        int skillLevel = skill.getLevel();
        double health = player.getHealth();
        int healthModifier = skillLevel + 1;
        if (potionMeta.getBasePotionData().isUpgraded()) {
            player.setHealth(Math.min(20, health + ((healthModifier + 2) * skillLevel * 2)));
            return;
        }
        player.setHealth(Math.min(20, health + healthModifier * skillLevel * 2));
    }

    private boolean canFortify(PotionMeta potionMeta) {
        ActiveSkill fortification = new ActiveSkill(player, SkillType.FORTIFICATION, cooldowns, durations);
        if (fortification.getLevel() == 0) {
            return false;
        }
        if (!fortification.isActive()) {
            return false;
        }
        FileConfiguration config = unitedSkills.getConfig();
        List<String> fortifyList = config.getStringList("fortification-effects.1");
        if (fortification.getLevel() == 3) {
            fortifyList.addAll(config.getStringList("fortification-effects.3"));
            fortifyList.addAll(config.getStringList("fortification-effects.2"));
        }
        if (fortification.getLevel() == 2) {
            fortifyList.addAll(config.getStringList("fortification-effects.2"));
        }
        if (potionMeta.hasCustomEffects()) {
            for (PotionEffect effect : potionMeta.getCustomEffects()) {
                if (fortifyList.contains(effect.getType().getName())) {
                    return true;
                }
            }
        }
        @Nullable PotionEffectType effect = potionMeta.getBasePotionData().getType().getEffectType();
        if (effect == null) {
            return false;
        }
        return fortifyList.contains(effect.getName());
    }

    private boolean isHarmfulEffect(PotionEffect effect) {
        @NotNull List<String> harmfulPotions = unitedSkills.getConfig().getStringList("harmful-potions");
        return harmfulPotions.contains(effect.getType().getName());
    }

    private JobsPlayer getJobsPlayer() {
        return Jobs.getPlayerManager().getJobsPlayer(player);
    }

    private int getAmplifier(PotionMeta potionMeta) {
        PotionData potionData = potionMeta.getBasePotionData();
        int amplifier = 0;
        if (potionData.isUpgraded()) {
            String typeName = potionData.getType().toString();
            FileConfiguration config = unitedSkills.getConfig();
            amplifier = config.getInt("potions." + typeName + ".max_amplifier");
        }
        Skill fortification = new Skill(player, SkillType.FORTIFICATION);
        if (fortification.getLevel() > 0) {
            if (canFortify(potionMeta)) {
                amplifier += 1;
            }
        }
        return amplifier;
    }

    private double getDistance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2));
    }

    private int getPotionDuration(PotionData potionData) {
        String typeName = potionData.getType().toString();
        FileConfiguration config = unitedSkills.getConfig();
        int duration = config.getInt("potions." + typeName + ".default");

        if (potionData.isUpgraded()) {
            duration = config.getInt("potions." + typeName + ".upgraded");
        } else if (potionData.isExtended()) {
            duration = config.getInt("potions." + typeName + ".extended");
        }
        return duration * 20;
    }

    private boolean isBrewer() {
        return Utils.isInJob(player, "Brewer");
    }
}
