package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.event.actions.*;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.events.WarDeclareEvent;
import org.unitedlands.wars.events.WarEndEvent;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;
import org.unitedlands.wars.war.entities.WarringEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.unitedlands.wars.UnitedWars.TOWNY_API;
import static org.unitedlands.wars.Utils.getTownyResident;
import static org.unitedlands.wars.war.WarDatabase.hasWar;

public class TownyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTownInvite(TownInvitePlayerEvent event) {
        var invite = event.getInvite();
        if (!hasWar(invite.getSender()))
            return;
        invite.getDirectSender().sendMessage(Utils.getMessageRaw("cannot-do-in-war"));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoinTown(TownPreAddResidentEvent event) {
        if (hasWar(event.getTown())) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoinNation(NationPreAddTownEvent event) {
        if (hasWar(event.getNation().getCapital())) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }
    }

    @EventHandler
    public void onTownLeave(TownLeaveEvent event) {
        if (hasWar(event.getTown())) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }
    }

    @EventHandler
    public void onTownClaim(TownPreClaimEvent event) {
        if (hasWar(event.getTown())) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }

    }

    @EventHandler
    public void onTownUnclaim(TownPreUnclaimCmdEvent event) {
        if (hasWar(event.getTown())) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }
    }

    @EventHandler
    public void onTownToggleNeutral(TownToggleNeutralEvent event) {
        if (hasWar(event.getTown()) && event.getFutureState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNationToggleNeutral(NationToggleNeutralEvent event) {
        if (hasWar(event.getNation().getCapital()) && event.getFutureState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownDeleted(PreDeleteTownEvent event) {
        if (!hasWar(event.getTown()))
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
        if (!hasWar(event.getNation().getCapital()))
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
        if (!hasWar(event.getTown()))
            return;

        if (type.equals(TransactionType.WITHDRAW)) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }

    }

    @EventHandler
    public void onNationTransaction(NationPreTransactionEvent event) {
        TransactionType type = event.getTransaction().getType();
        if (!hasWar(event.getNation().getCapital()))
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
        if (!hasWar(player))
            return;
        if (isInvalidLocation(event.getTownBlock(), player))
            return;

        Material mat = event.getMaterial();
        if (isModifiableMaterial(mat))
            event.setCancelled(false);
        if (mat == Material.OBSIDIAN)
            removeBlockLater(event.getBlock(), 30); // 30 secs
        else
            removeBlockLater(event.getBlock(), 60 * 5); // 5 mins
    }

    @EventHandler
    public void onBreak(TownyDestroyEvent event) {
        Player player = event.getPlayer();
        if (event.isInWilderness())
            return;
        if (!hasWar(player))
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
        if (TOWNY_API.isWilderness(event.getBlock()))
            return;
        if (!hasWar(player))
            return;
        if (isInvalidLocation(TOWNY_API.getTownBlock(event.getBlock().getLocation()), player))
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
            Town town = TOWNY_API.getTown(block.getLocation());
            if (town == null)
                continue;
            if (!hasWar(town))
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

    @EventHandler
    public void onItemUse(TownyItemuseEvent event) {
        Player player = event.getPlayer();
        if (event.isInWilderness())
            return;
        if (!hasWar(player))
            return;
        Material material = event.getMaterial();
        if (material == Material.ENDER_PEARL || material == Material.CHORUS_FRUIT)
            event.setCancelled(false);
    }
    @EventHandler
    public void onSwitchUse(TownySwitchEvent event) {
        Player player = event.getPlayer();
        if (event.isInWilderness())
            return;
        if (!hasWar(player))
            return;
        if (isInvalidLocation(event.getTownBlock(), player))
            return;
        String material = event.getMaterial().toString();
        if (material.contains("DOOR") || material.contains("GATE") || material.contains("CRYSTAL"))
            event.setCancelled(false);
    }

    @EventHandler
    public void onLecternTake(PlayerTakeLecternBookEvent event) {
        Location location = event.getLectern().getLocation();
        if (TOWNY_API.isWilderness(location))
            return;
        TownBlock townBlock = TOWNY_API.getTownBlock(location);
        if (townBlock == null)
            return;
        if (!isInvalidLocation(townBlock, event.getPlayer()))
            event.setCancelled(true);
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
        if (hasWar(event.getTown()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onLiquidFlow(FluidLevelChangeEvent event) {
        Location location = event.getBlock().getLocation();
        if (TOWNY_API.isWilderness(location))
            return;
        TownBlock townBlock = TOWNY_API.getTownBlock(location);
        if (townBlock == null)
            return;
        Town town = townBlock.getTownOrNull();
        if (town == null)
            return;
        if (hasWar(town))
            event.setCancelled(true);
    }

    @EventHandler
    public void onWarStart(WarDeclareEvent event) {
        setFrozen(event.getTargetEntity().getGovernment().getTownBlocks(), true);
        setFrozen(event.getDeclaringEntity().getGovernment().getTownBlocks(), true);
    }

    @EventHandler
    public void onWarEnd(WarEndEvent event) {
        setFrozen(event.getWinner().getGovernment().getTownBlocks(), false);
        setFrozen(event.getLoser().getGovernment().getTownBlocks(), false);
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
        if (town.getTrustedResidents().contains(resident))
            return true;
        War war = WarDatabase.getWar(town);
        if (war == null)
            return true;
        WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
        return !war.equals(warringEntity.getWar());
    }

    private void removeBlockLater(Block block, int time) {
        Bukkit.getServer().getScheduler().runTaskLater(UnitedWars.getInstance(), () -> {
            block.setType(Material.AIR);
        }, time * 20);
    }

    private void setFrozen(Collection<TownBlock> blocks, boolean toggle) {
        for (TownBlock block: blocks) {
            @NotNull CompletableFuture<Chunk> future = block.getWorldCoord().getBukkitWorld().getChunkAtAsync(block.getX(), block.getZ());
            future.thenAcceptAsync(chunk -> freezeChunkEntities(toggle, chunk));
        }
    }

    private void freezeChunkEntities(boolean toggle, Chunk chunk) {
        for (Entity entity: chunk.getEntities()) {
            if (entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.WOLF)
                continue;
            entity.setInvulnerable(toggle);
            entity.setGravity(!toggle);
            if (entity instanceof LivingEntity living) {
                living.setAI(!toggle);
                living.setCollidable(!toggle);
            }
        }
    }
}
