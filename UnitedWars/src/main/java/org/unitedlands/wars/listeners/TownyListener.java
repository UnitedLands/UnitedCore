package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.event.actions.TownySwitchEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TransactionType;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;
import org.unitedlands.wars.war.WarUtil;
import org.unitedlands.wars.war.entities.WarringEntity;

import java.util.ArrayList;
import java.util.List;

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
        if (war.getWarType() == WarType.TOWNWAR) {
            WarringEntity winner = WarUtil.getOpposingEntity(deleted);
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
        WarringEntity winner = WarUtil.getOpposingEntity(deleted);
        war.endWar(deleted, winner);

    }
    @EventHandler
    public void onTownTransaction(TownPreTransactionEvent event) {
        TransactionType type = event.getTransaction().getType();
        if (!event.getTown().hasActiveWar())
            return;

        if (type.equals(TransactionType.WITHDRAW) || type.equals(TransactionType.DEPOSIT)) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }

    }

    @EventHandler
    public void onNationTransaction(NationPreTransactionEvent event) {
        TransactionType type = event.getTransaction().getType();
        if (!event.getNation().hasActiveWar())
            return;

        if (type.equals(TransactionType.WITHDRAW) || type.equals(TransactionType.DEPOSIT)) {
            event.setCancelled(true);
            event.setCancelMessage(Utils.getMessageRaw("cannot-do-in-war"));
        }

    }

    @EventHandler
    public void onBuild(TownyBuildEvent event) {
        if (WarDatabase.getWars().isEmpty())
            return;
        if (event.isInWilderness())
            return;
        Player player = event.getPlayer();

        if (!WarDatabase.hasWar(player))
            return;

        Material mat = event.getMaterial();
        if (mat == Material.TNT || mat == Material.RESPAWN_ANCHOR) {
                event.setCancelled(false);
        }
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

            if (getIgnoredBlocks().contains(block.getType().name()) || getIgnoredBlocks().contains(block.getRelative(BlockFace.UP).getType().name())) {
                alreadyAllowed.remove(block);
                continue;
            }
            ++count;
            toAllow.add(block);
            TownyRegenAPI.beginProtectionRegenTask(block, count, TownyAPI.getInstance().getTownyWorld(block.getLocation().getWorld().getName()), event);
        }
        toAllow.addAll(alreadyAllowed);
        event.setBlockList(toAllow);

    }

    @EventHandler
    public void onSwitchUse(TownySwitchEvent event) {
        Player player = event.getPlayer();
        if (!WarDatabase.hasWar(player))
            return;
        Town town = event.getTownBlock().getTownOrNull();
        if (town == null)
            return;
        War war = WarDatabase.getWar(town);
        if (war == null)
            return;
        WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
        if (!war.equals(warringEntity.getWar()))
            return;
        String material = event.getMaterial().toString();
        if (material.toLowerCase().contains("door") || material.toLowerCase().contains("gate"))
            event.setCancelled(false);
    }

    @EventHandler
    public void onExplosionDamagingEntity(TownyExplosionDamagesEntityEvent event) {
        if (WarDatabase.getWars().isEmpty())
            return;
        if (event.isInWilderness())
            return;

        // Don't damage mobs.
        if (WarDatabase.hasWar(event.getTown()))
            event.setCancelled(true);
    }

    private static List<String> getIgnoredBlocks() {
        return UnitedWars.getInstance().getConfig().getStringList("ignored-explosion-blocks");
    }
}
