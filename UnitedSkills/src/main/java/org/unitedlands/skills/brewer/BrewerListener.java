package org.unitedlands.skills.brewer;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.gamingmesh.jobs.container.blockOwnerShip.BlockOwnerShip;
import com.gamingmesh.jobs.container.blockOwnerShip.BlockTypes;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.unitedlands.skills.Utils.*;

public class BrewerListener implements Listener {
    private final UnitedSkills unitedSkills;
    private final BlendingGui blendingGui;
    private final List<BrewingStand> standsToBeSpedUp = new ArrayList<>();
    private final List<BrewingStand> spedUpStands = new ArrayList<>();
    private Player player;

    public BrewerListener(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
        blendingGui = new BlendingGui(this.unitedSkills);
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
        if (isSuccessful(player, "exposure-therapy")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        player = event.getPlayer();
        if (!isBrewer()) {
            return;
        }
        if (getSkillLevel(player, "assisted-healing") == 0 || getSkillLevel(player, "exposure-therapy") == 0) {
            return;
        }
        ItemStack item = event.getItem();
        if (!item.getType().equals(Material.POTION)) {
            return;
        }
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        final PotionType potionType = potionMeta.getBasePotionData().getType();
        if (potionType.equals(PotionType.INSTANT_HEAL)) {
            increaseHealingEffect(potionMeta);
            return;
        }
        if (potionType.equals(PotionType.INSTANT_DAMAGE)) {
            if (isSuccessful(player, "exposure-therapy")) {
                player.getInventory().remove(item);
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPotionBrew(BrewEvent event) {
        Block block = event.getBlock();
        BrewingStand brewingStand = (BrewingStand) block.getState();
        spedUpStands.remove(brewingStand);
        player = getBrewingStandOwner(block);
        if (player == null) {
            return;
        }
        if (!(getSkillLevel(player, "quality-ingredients") == 1)) {
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
    // TODO- Fix this ability
    public void onBrewingStandInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (!block.getType().equals(Material.BREWING_STAND)) {
            return;
        }
        player = event.getPlayer();
        // They'd be opening the blending GUI if that's the case
        if (player.isSneaking() && getSkillLevel(player, "blend") > 0) {
            return;
        }
        if (!isBrewer()) {
            return;
        }

        BrewingStand brewingStand = (BrewingStand) block.getState();
        if (!ownsBrewingStand(block)) {
            return;
        }

        int skillLevel = getSkillLevel(player, "upgraded-hardware");

        if (skillLevel == 0) {
            return;
        }

        if (standsToBeSpedUp.contains(brewingStand)) {
            return;
        }
        standsToBeSpedUp.add(brewingStand);
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
            if (getSkillLevel(player, "exposure-therapy") != 0) {
                if (potionType.equals(PotionEffectType.HARM)) {
                    if (isSuccessful(player, "exposure-therapy")) {
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
                        event.setIntensity(player, 0);
                    }
                }
            }

            int skillLevel = getSkillLevel(player, "assisted-healing") - 1;

            if (skillLevel > 0) {
                if (potionType.equals(PotionEffectType.HEAL)) {
                    if (isSuccessful(player, "assisted-healing")) {
                        event.setIntensity(player, 0);
                        increaseHealingEffect(potionMeta);
                        return;
                    }
                }
            }
        }
    }

    private void increaseHealingEffect(PotionMeta potionMeta) {
        int skillLevel = getSkillLevel(player, "assisted-healing");
        double health = player.getHealth();
        int healthModifier = skillLevel + 1;
        if (potionMeta.getBasePotionData().isUpgraded()) {
            player.setHealth(health + ((healthModifier + 2) * skillLevel * 2));
            return;
        }
        player.setHealth(health + healthModifier * skillLevel * 2);
    }

    private boolean isHarmfulEffect(PotionEffect effect) {
        @NotNull List<String> harmfulPotions = unitedSkills.getConfig().getStringList("harmful-potions");
        return harmfulPotions.contains(effect.getType().getName());
    }

    private JobsPlayer getJobsPlayer() {
        return Jobs.getPlayerManager().getJobsPlayer(player);
    }

    private boolean isBrewer() {
        JobsPlayer jobsPlayer = getJobsPlayer();
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            return job.getJob().getName().equals("Brewer");
        }
        return false;
    }
}
