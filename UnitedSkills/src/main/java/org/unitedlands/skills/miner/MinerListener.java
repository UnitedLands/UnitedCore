package org.unitedlands.skills.miner;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import net.coreprotect.CoreProtectAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.unitedlands.skills.Skill;
import org.unitedlands.skills.SkillType;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class MinerListener implements Listener {
    private Player player;
    private final UnitedSkills unitedSkills;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    private final CoreProtectAPI coreProtect;

    private static final String META_EXPLOSION_SOURCE = "blast_mining_explosion_source";

    public MinerListener(UnitedSkills unitedSkills, CoreProtectAPI coreProtect) {
        this.unitedSkills = unitedSkills;
        this.coreProtect = coreProtect;
    }

    @EventHandler
    public void onInteractWithFlint(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isMiner()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.BLAST_MINING, cooldowns, durations);
        if (skill.getLevel() == 0) {
            return;
        }
        final ItemStack flintAndSteel = player.getInventory().getItemInMainHand();
        if (!flintAndSteel.getType().toString().equals("FLINT_AND_STEEL")) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }

        skill.activate();
    }

    @EventHandler
    public void onInteractWithPickaxe(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isMiner()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.FRENZY, cooldowns, durations);
        if (skill.getLevel() == 0) {
            return;
        }
        final ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (!pickaxe.getType().toString().contains("PICKAXE")) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }

        if (skill.activate()) {
            unboostPickaxeLater(pickaxe, skill);
            boostPickaxe(pickaxe);
        }

    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        player = event.getPlayer();
        if (!isMiner()) {
            return;
        }

        Block block = event.getBlock();
        if (isPlaced(block)) {
            return;
        }

        Skill fortunate = new Skill(player, SkillType.FORTUNATE);
        List<Item> items = event.getItems();
        if (fortunate.isSuccessful()) {
            for (Item item : items) {
                Utils.multiplyItem(player, item.getItemStack(), 2);
            }
        }

        Skill frenzy = new Skill(player, SkillType.FRENZY, cooldowns, durations);
        if (frenzy.isActive()) {
            for (Item item : items) {
                Utils.multiplyItem(player, item.getItemStack(), 3);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        player = event.getPlayer();
        if (!isMiner()) {
            return;
        }
        Block block = event.getBlock();

        PlayerInventory inventory = player.getInventory();
        if (!inventory.getItemInMainHand().toString().contains("PICKAXE")) {
            return;
        }

        Skill skill = new Skill(player, SkillType.BLAST_MINING, cooldowns, durations);
        if (skill.isActive()) {
            if (inventory.contains(Material.TNT)) {
                int power = Math.min(skill.getLevel() * 2, 5);
                block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
                Utils.takeItem(player, Material.TNT);
            } else {
                player.sendActionBar(Component.text("You must have tnt to use Blast Mining!", NamedTextColor.RED));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
            }
        }
    }

    @EventHandler
    public void onBlastExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        event.setCancelled(player.hasMetadata(META_EXPLOSION_SOURCE));
    }

    @EventHandler
    public void onBlastExplosionItemDamage(PlayerItemDamageEvent event) {
        if (!event.getPlayer().hasMetadata(META_EXPLOSION_SOURCE)) {
            return;
        }
        event.setCancelled(true);
    }

    private void boostPickaxe(ItemStack pickaxe) {
        int currentEfficiencyLevel = pickaxe.getEnchantmentLevel(Enchantment.DIG_SPEED);

        ItemMeta itemMeta = pickaxe.getItemMeta();
        itemMeta.addEnchant(Enchantment.DIG_SPEED, currentEfficiencyLevel + 5, true);
        pickaxe.setItemMeta(itemMeta);
    }

    private void unboostPickaxeLater(ItemStack pickaxe, Skill skill) {
        int currentEfficiencyLevel = pickaxe.getEnchantmentLevel(Enchantment.DIG_SPEED);
        ItemMeta itemMeta = pickaxe.getItemMeta();

        unitedSkills.getServer().getScheduler().runTaskLater(unitedSkills, () -> {
            itemMeta.removeEnchant(Enchantment.DIG_SPEED);
            itemMeta.addEnchant(Enchantment.DIG_SPEED, currentEfficiencyLevel, false);
            pickaxe.setItemMeta(itemMeta);
        }, skill.getSecondsLeft() * 20L);
    }

    private boolean isMiner() {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            return job.getJob().getName().equals("Miner");
        }
        return false;
    }

    public boolean isPlaced(Block block) {
        boolean match = false;

        List<String[]> check = coreProtect.blockLookup(block, 0);

        for (String[] value : check) {
            CoreProtectAPI.ParseResult result = coreProtect.parseResult(value);
            if (result.getActionId() == 1) {
                match = true;
                break;
            }
        }
        return match;
    }
}
