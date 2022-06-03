package org.unitedlands.skills.jobs;

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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.unitedlands.skills.Utils.canActivate;


public class MinerAbilities implements Listener {
    private Player player;
    private final UnitedSkills unitedSkills;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();
    private boolean frenzyIsActive;
    private final CoreProtectAPI coreProtect;

    public MinerAbilities(UnitedSkills unitedSkills, CoreProtectAPI coreProtect) {
        this.unitedSkills = unitedSkills;
        this.coreProtect = coreProtect;
    }

    @EventHandler
    public void onBlastMiningActivate(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isMiner()) {
            return;
        }
        ActiveSkill blastMining = new ActiveSkill(player, SkillType.BLAST_MINING, cooldowns, durations);
        if (canActivate(event, "FLINT_AND_STEEL", blastMining)) {
            blastMining.activate();
        }
    }

    @EventHandler
    public void onFrenzyActivate(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isMiner()) {
            return;
        }
        ActiveSkill frenzy = new ActiveSkill(player, SkillType.FRENZY, cooldowns, durations);
        if (canActivate(event, "PICKAXE", frenzy)) {
            frenzy.activate();
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, frenzy.getDuration() * 20, 3));
            frenzyIsActive = true;
            unitedSkills.getServer().getScheduler().runTaskLater(unitedSkills, () -> frenzyIsActive = false, frenzy.getDuration() * 20L);
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
        if (Utils.isPlaced(coreProtect, block)) {
            return;
        }

        Skill fortunate = new Skill(player, SkillType.FORTUNATE);
        List<Item> items = event.getItems();
        if (fortunate.isSuccessful()) {
            for (Item item : items) {
                Utils.multiplyItem(player, item.getItemStack(), 1);
            }
        }

        ActiveSkill frenzy = new ActiveSkill(player, SkillType.FRENZY, cooldowns, durations);
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
        ItemStack mainHand = inventory.getItemInMainHand();
        if (!mainHand.toString().contains("PICKAXE")) {
            return;
        }

        if (frenzyIsActive) {
            ActiveSkill frenzy = new ActiveSkill(player, SkillType.FRENZY, cooldowns, durations);
            event.setExpToDrop(event.getExpToDrop() * (frenzy.getLevel() + 2));
            return;
        }

        ActiveSkill blastMining = new ActiveSkill(player, SkillType.BLAST_MINING, cooldowns, durations);
        if (blastMining.isActive()) {
            if (Utils.takeItemFromMaterial(player, Material.TNT)) {
                int power = Math.min(blastMining.getLevel() * 2, 5);
                block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
                damagePickaxe(mainHand, 10 + (blastMining.getLevel()) * 3);
            } else {
                player.sendActionBar(Component.text("You must have tnt to use Blast Mining!", NamedTextColor.RED));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
            }
        }
    }

    private void damagePickaxe(ItemStack pickaxe, int damage) {
        Damageable meta = (Damageable) pickaxe.getItemMeta();
        if (pickaxe.containsEnchantment(Enchantment.DURABILITY)) {
            int level = pickaxe.getEnchantmentLevel(Enchantment.DURABILITY);
            damage = (int) (damage * (1 - (0.2 * level)));
        }
        meta.setDamage(meta.getDamage() + damage);
        pickaxe.setItemMeta(meta);
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                || event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION))) return;
        player = (Player) event.getEntity();
        if (!isMiner()) {
            return;
        }
        Skill shellShocked = new Skill(player, SkillType.SHELL_SHOCKED);
        int level = shellShocked.getLevel();
        if (level == 0) {
            return;
        }
        double damage = event.getDamage() - (event.getDamage() * (level * 0.1));
        event.setDamage(damage);
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed tnt)) {
            return;
        }
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
        return Utils.isInJob(player, "Miner");
    }

}
