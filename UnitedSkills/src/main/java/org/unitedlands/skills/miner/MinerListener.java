package org.unitedlands.skills.miner;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import net.coreprotect.CoreProtectAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
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
    private boolean frenzyIsActive;
    private final CoreProtectAPI coreProtect;

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

        if (!isRightClick(event.getAction())) {
            return;
        }

        if (skill.isActive()) {
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
        if (!isRightClick(event.getAction())) {
            return;
        }

        if (skill.isActive()) {
            return;
        }

        if (skill.activate()) {
            unboostPickaxeLater(pickaxe, skill);
            boostPickaxe(pickaxe);
            frenzyIsActive = true;
            unitedSkills.getServer().getScheduler().runTaskLater(unitedSkills, () -> frenzyIsActive = false, skill.getDuration() * 20L);
        }
    }

    private boolean isRightClick(Action action) {
        return action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK);
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        player = event.getPlayer();
        if (!isMiner()) {
            return;
        }
        Block block = event.getBlock();

        PlayerInventory inventory = player.getInventory();
        ItemStack mainHand = inventory.getItemInMainHand();
        if (!mainHand.toString().contains("PICKAXE")) {
            return;
        }

        Skill skill = new Skill(player, SkillType.BLAST_MINING, cooldowns, durations);
        if (skill.isActive() && !frenzyIsActive) {
            if (Utils.takeItemFromMaterial(player, Material.TNT)) {
                int power = Math.min(skill.getLevel() * 2, 5);
                block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
                damagePickaxe(mainHand, 15 + (skill.getLevel()) * 3);
            } else {
                player.sendActionBar(Component.text("You must have tnt to use Blast Mining!", NamedTextColor.RED));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
            }
        }
    }

    private void damagePickaxe(ItemStack pickaxe, int damage) {
        Damageable meta = (Damageable) pickaxe.getItemMeta();
        meta.setDamage(meta.getDamage() + damage);
        pickaxe.setItemMeta(meta);
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof TNTPrimed)) return;
        if (!(event.getEntity() instanceof  Player)) return;
        player = (Player) event.getEntity();
        if (!isMiner()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.SHELL_SHOCKED);
        int level = skill.getLevel();
        if (level == 0) {
            return;
        }
        double damage = event.getDamage() - (event.getDamage() * (level * 0.1));
        event.setDamage(damage);
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) {
            return;
        }
        TNTPrimed tnt = (TNTPrimed) event.getEntity();
        if (tnt.getSource() == null) {
            return;
        }
        if (!tnt.getSource().equals(player)) {
            return;
        }
        player = (Player) tnt.getSource();
        if (!isMiner()) {
            return;
        }
        Skill pyrotechnics = new Skill(player, SkillType.PYROTECHNICS);
        int level = pyrotechnics.getLevel();
        if (level == 0) {
            return;
        }
        float power = (float) (4 + (4 * (level * 0.2)));
        event.getLocation().createExplosion(player, power, false, true);
        tnt.remove();
        event.setCancelled(true);
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
