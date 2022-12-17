package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.event.actions.TownySwitchEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.events.WarDeclareEvent;
import org.unitedlands.wars.events.WarEndEvent;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;
import org.unitedlands.wars.war.entities.WarringTown;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.unitedlands.wars.Utils.getTownyResident;

public class TownyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoinTown(TownPreAddResidentEvent event) {
        if (event.getTown().hasActiveWar()) {
            War war = WarDatabase.getWar(event.getTown());
            if (war == null) {
                return;
            }
            war.addResident(event.getResident(), event.getTown());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoinNation(NationPreAddTownEvent event) {
        if (event.getNation().hasActiveWar()) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }
    }

    @EventHandler
    public void onTownLeave(TownLeaveEvent event) {
        if (event.getTown().hasActiveWar()) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }
    }

    @EventHandler
    public void onTownClaim(TownPreClaimEvent event) {
        if (event.getTown().hasActiveWar()) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }

    }

    @EventHandler
    public void onTownUnclaim(TownPreUnclaimCmdEvent event) {
        if (event.getTown().hasActiveWar()) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }
    }

    @EventHandler
    public void onTownToggleNeutral(TownToggleNeutralEvent event) {
        if (event.getTown().hasActiveWar() && event.getFutureState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNationToggleNeutral(NationToggleNeutralEvent event) {
        if (event.getNation().hasActiveWar() && event.getFutureState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownDeleted(PreDeleteTownEvent event) {
        if (!event.getTown().hasActiveWar())
            return;
        Town town = event.getTown();
        War war = WarDatabase.getWar(town);
        if (war == null)
            return;

        WarringEntity deleted = WarDatabase.getWarringEntity(town.getMayor());
        if (deleted == null)
            return;
        if (war.getWarType() == WarType.TOWNWAR || town.isCapital()) {
            WarringEntity winner = deleted.getEnemy();
            war.endWar(deleted, winner);
        } else {
            war.removeTown(town);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationDelete(PreDeleteNationEvent event) {
        if (!event.getNation().hasActiveWar())
            return;

        Nation nation = event.getNation();
        War war = WarDatabase.getWar(nation.getCapital());
        if (war == null)
            return;

        WarringEntity deleted = WarDatabase.getWarringEntity(nation.getKing());
        if (deleted == null)
            return;
        WarringEntity winner = deleted.getEnemy();
        war.endWar(deleted, winner);

    }
    @EventHandler
    public void onTownTransaction(TownPreTransactionEvent event) {
        TransactionType type = event.getTransaction().getType();
        if (!event.getTown().hasActiveWar())
            return;

        if (type.equals(TransactionType.WITHDRAW)) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }

    }

    @EventHandler
    public void onNationTransaction(NationPreTransactionEvent event) {
        TransactionType type = event.getTransaction().getType();
        if (!event.getNation().hasActiveWar())
            return;

        if (type.equals(TransactionType.WITHDRAW)) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }

    }

    @EventHandler
    public void onBuild(TownyBuildEvent event) {
        Player player = event.getPlayer();
        if (event.isInWilderness())
            return;
        if (!WarDatabase.hasWar(player))
            return;
        if (isInvalidLocation(event.getTownBlock(), player))
            return;

        Material mat = event.getMaterial();
        if (isModifiableMaterial(mat)) {
            event.setCancelled(false);
        }
        if (mat == Material.OBSIDIAN)
            removeBlockLater(event.getBlock()); // 30 seconds * 20 ticks.
    }

    @EventHandler
    public void onBreak(TownyDestroyEvent event) {
        Player player = event.getPlayer();
        if (event.isInWilderness())
            return;
        if (!WarDatabase.hasWar(player))
            return;
        if (isInvalidLocation(event.getTownBlock(), player))
            return;

        event.setCancelled(false);
        Block block = event.getBlock();
        Material mat = block.getType();
        if (isModifiableMaterial(mat))
            return;

        TownyRegenAPI.beginProtectionRegenTask(block, 60, TownyAPI.getInstance().getTownyWorld(block.getLocation().getWorld().getName()), event);
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (UnitedWars.TOWNY_API.isWilderness(event.getBlock()))
            return;
        if (!WarDatabase.hasWar(player))
            return;
        if (isInvalidLocation(UnitedWars.TOWNY_API.getTownBlock(event.getBlock().getLocation()), player))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onExplosionDamagingBlocks(TownyExplodingBlocksEvent event) {
        if (WarDatabase.getWars().isEmpty())
            return;

        List<Block> alreadyAllowed = event.getTownyFilteredBlockList();
        List<Block> toAllow = new ArrayList<>();

        if (alreadyAllowed == null)
            return;

        int count = 0;

        for (Block block : event.getVanillaBlockList()) {
            Town town = UnitedWars.TOWNY_API.getTown(block.getLocation());
            if (town == null)
                continue;
            if (!WarDatabase.hasWar(town))
                continue;

            alreadyAllowed.remove(block);

            toAllow.add(block);
            if (block.getType() == Material.TNT)
                continue;
            ++count;

            TownyRegenAPI.beginProtectionRegenTask(block, count, TownyAPI.getInstance().getTownyWorld(block.getLocation().getWorld().getName()), event);
        }
        toAllow.addAll(alreadyAllowed);
        event.setBlockList(toAllow);

    }
    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.WOLF || entity.getType() == EntityType.ENDER_CRYSTAL)
            return;
        TownBlock townBlock = UnitedWars.TOWNY_API.getTownBlock(entity.getLocation());
        if (townBlock == null)
            return;
        Town town = townBlock.getTownOrNull();
        if (town == null)
            return;

        if (!WarDatabase.hasWar(town))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onSwitchUse(TownySwitchEvent event) {
        Player player = event.getPlayer();
        if (event.isInWilderness())
            return;
        if (!WarDatabase.hasWar(player))
            return;
        if (isInvalidLocation(event.getTownBlock(), player))
            return;
        String material = event.getMaterial().toString();
        if (material.contains("DOOR") || material.contains("GATE") || material.contains("CRYSTAL"))
            event.setCancelled(false);
    }

    @EventHandler
    public void onExplosionDamagingEntity(TownyExplosionDamagesEntityEvent event) {
        if (WarDatabase.getWars().isEmpty())
            return;
        if (event.isInWilderness())
            return;
        if (event.getEntity().getType().equals(EntityType.PLAYER))
            return;
        // Don't damage mobs.
        if (WarDatabase.hasWar(event.getTown()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onWarStart(WarDeclareEvent event) {
        WarType type = event.getDeclarationWarBook().getType();
        if (type == WarType.TOWNWAR) {
            toggleFreeze(event.getTarget().town().getTownBlocks(), false);
            toggleFreeze(event.getDeclarer().town().getTownBlocks(), false);
        }
        if (type == WarType.NATIONWAR) {
            toggleFreeze(event.getTarget().nation().getTownBlocks(), false);
            toggleFreeze(event.getDeclarer().nation().getTownBlocks(), false);
        }
    }

    @EventHandler
    public void onWarEnd(WarEndEvent event) {
        if (event.getWinner() instanceof WarringTown town) {
            toggleFreeze(town.getTown().getTownBlocks(), true);
            WarringTown otherTown = (WarringTown) event.getLoser();
            toggleFreeze(otherTown.getTown().getTownBlocks(), true);
        } else {
            WarringNation nation = (WarringNation) event.getWinner();
            WarringNation otherNation = (WarringNation) event.getLoser();
            toggleFreeze(nation.getNation().getTownBlocks(), true);
            toggleFreeze(otherNation.getNation().getTownBlocks(), true);
        }
    }

    private static boolean isModifiableMaterial(Material mat) {
        return mat == Material.TNT || mat == Material.COBWEB || mat == Material.LADDER || mat == Material.SCAFFOLDING || mat == Material.OBSIDIAN || mat == Material.END_CRYSTAL;
    }
    private boolean isInvalidLocation(@Nullable TownBlock townBlock, Player player) {
        if (townBlock == null)
            return true;
        Town town = townBlock.getTownOrNull();
        if (town == null)
            return true;
        Resident resident = getTownyResident(player);
        if (resident.getTownOrNull().equals(town))
            return true;
        War war = WarDatabase.getWar(town);
        if (war == null)
            return true;
        WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
        return !war.equals(warringEntity.getWar());
    }

    private void removeBlockLater(Block block) {
        Bukkit.getServer().getScheduler().runTaskLater(UnitedWars.getInstance(), () -> {
            block.setType(Material.AIR);
        }, 600);
    }

    private void toggleFreeze(Collection<TownBlock> blocks, boolean toggle) {
        for (TownBlock block: blocks) {
            Chunk chunk = block.getWorldCoord().getBukkitWorld().getChunkAt(block.getX(), block.getZ());
            for (Entity entity: chunk.getEntities()) {
                if (entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.WOLF)
                    continue;
                if (entity instanceof LivingEntity living) {
                    living.setAI(toggle);
                    living.setGravity(toggle);
                    living.setCollidable(toggle);
                    living.setInvulnerable(toggle);
                }
            }
        }
    }
}
