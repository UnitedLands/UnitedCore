package org.unitedlands.wars.commands;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;
import org.unitedlands.wars.war.entities.WarringTown;

import java.util.*;

public class WarAdminCommand implements TabExecutor {
    private static final List<String> MAIN_TAB_COMPLETES = Arrays.asList("end", "purge", "tokens", "freeze", "damage", "damagemax", "life");
    private static final List<String> END_TAB_COMPLETES = Arrays.asList("town", "nation");
    private static final List<String> TOKENS_TAB_COMPLETES = List.of("add", "remove", "set");
    private CommandSender sender;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args[0].equalsIgnoreCase("tokens")) {
            if (args.length == 2) {
                return TOKENS_TAB_COMPLETES;
            }
            if (args.length == 3) {
                return BaseCommand.getTownyStartingWith(args[2], "t");
            }
        }
        if (args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("start")) {
            if (args.length == 2) {
                return END_TAB_COMPLETES;
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("town")) {
                    return BaseCommand.getTownyStartingWith(args[2], "t");
                }

                if (args[1].equalsIgnoreCase("nation")) {
                    return BaseCommand.getTownyStartingWith(args[2], "n");
                }
                return Collections.emptyList();
            }
        }
        return args.length == 1 ? MAIN_TAB_COMPLETES : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        this.sender = sender;
        if (args[0].equalsIgnoreCase("purge")) {
            parsePurgeCommand();
            return true;
        }
        if (args[0].equalsIgnoreCase("damage")) {
            Player player = Bukkit.getPlayer(args[1]);
            WarringEntity entity = WarDatabase.getWarringEntity(player);
            int amount = Integer.parseInt(args[2]);
            entity.getWarHealth().decreaseHealth(amount);
        }
        if (args[0].equalsIgnoreCase("damagemax")) {
            Player player = Bukkit.getPlayer(args[1]);
            WarringEntity entity = WarDatabase.getWarringEntity(player);
            int amount = Integer.parseInt(args[2]);
            entity.getWarHealth().decreaseMaxHealth(amount);
        }
        if (args[0].equalsIgnoreCase("life")) {
            Resident player = Utils.getTownyResident(Bukkit.getPlayer(args[1]));
            int amount = Integer.parseInt(args[2]);
            WarDataController.setResidentLives(player, amount);
        }
        if (args[0].equalsIgnoreCase("tokens")) {
            Town town = UnitedWars.TOWNY_API.getTown(args[2]);
            int amount = Integer.parseInt(args[3]);
            if (town == null) {
                sender.sendMessage(Utils.getMessage("invalid-town"));
                return true;
            }
            if (town.isNeutral()) {
                sender.sendMessage("Town is neutral!");
                return true;
            }
            switch (args[1]) {
                case "add" -> parseAddTokensCommand(town, amount);
                case "remove" -> parseRemoveTokensCommand(town, amount);
                case "set" -> parseSetTokensCommand(town, amount);
            }
        }
        if (args[0].equalsIgnoreCase("start")) {
            if (args[1].equalsIgnoreCase("town")) {
                Town first = UnitedWars.TOWNY_API.getTown(args[2]);
                Town second = UnitedWars.TOWNY_API.getTown(args[3]);
                if (first == null || second == null) {
                    sender.sendMessage("Invalid towns.");
                    return true;
                }
                HashSet<Resident> residents = new HashSet<>();
                residents.addAll(first.getResidents());
                residents.addAll(second.getResidents());
                War war = new War(List.of(first, second), null,  residents, WarType.TOWNWAR);
                sender.sendMessage("War between " + first.getFormattedName() + " and " + second.getFormattedName() + " force started!");
                war.endWarTimer();
            }
            if (args[1].equalsIgnoreCase("nation")) {
                Nation first = UnitedWars.TOWNY_API.getNation(args[2]);
                Nation second = UnitedWars.TOWNY_API.getNation(args[3]);
                if (first == null || second == null) {
                    sender.sendMessage("Invalid towns.");
                    return true;
                }
                HashSet<Resident> residents = new HashSet<>();
                residents.addAll(first.getResidents());
                residents.addAll(second.getResidents());
                War war = new War(null, List.of(first, second),  residents, WarType.NATIONWAR);
                sender.sendMessage("War between " + first.getFormattedName() + " and " + second.getFormattedName() + " force started!");
                war.endWarTimer();
            }
        }
        if (args[0].equalsIgnoreCase("freeze")) {
            Nation target = UnitedWars.TOWNY_API.getNation(args[1]);
            if (target == null)
                return true;
            setFrozen(target.getTownBlocks(), Boolean.parseBoolean(args[2]));
        }
        if (args[0].equalsIgnoreCase("end")) {
            if (args[1].equalsIgnoreCase("town")) {
                Town winner = UnitedWars.TOWNY_API.getTown(args[2]);
                if (winner == null) {
                    sender.sendMessage(Component.text("Invalid Town!").color(NamedTextColor.RED));
                    return true;
                }
                WarringTown winningWarTown = WarDatabase.getWarringTown(winner);
                WarringTown losingWarTown = (WarringTown) winningWarTown.getEnemy();

                // Delegate the rest of the logic to the function.
                parseEndCommand(winningWarTown, losingWarTown);
                return true;
            }
            if (args[1].equalsIgnoreCase("nation")) {
                Nation winner = UnitedWars.TOWNY_API.getNation(args[2]);
                Nation loser = UnitedWars.TOWNY_API.getNation(args[3]);
                if (winner == null || loser == null) {
                    sender.sendMessage(Component.text("One of the towns doesn't exist... Are you sure the names are correct?").color(NamedTextColor.RED));
                    return true;
                }
                WarringNation winningWarNation = WarDatabase.getWarringNation(winner);
                WarringNation losingWarNation = (WarringNation) winningWarNation.getEnemy();

                // Delegate the rest of the logic to the function.
                parseEndCommand(winningWarNation, losingWarNation);
                return true;
            }
        }
        return false;
    }

    private void parseSetTokensCommand(Town town, int amount) {
        WarDataController.setTokens(town, amount);
        sender.sendMessage(Component.text("Set " + town.getFormattedName() + " tokens to " + amount));
    }

    private void parseRemoveTokensCommand(Town town, int amount) {
        WarDataController.setTokens(town, Math.max(0, WarDataController.getWarTokens(town) - amount));
        sender.sendMessage(Component.text("Removed " + amount + " tokens from " + town.getFormattedName()));
    }

    private void parseAddTokensCommand(Town town, int amount) {
        WarDataController.setTokens(town, WarDataController.getWarTokens(town) + amount);
        sender.sendMessage(Component.text("Added " + amount + " tokens to " + town.getFormattedName()));
    }

    private void parsePurgeCommand() {
        TownyUniverse townyUniverse = TownyUniverse.getInstance();
        WarDatabase.cleanUpBossBars();
        for (Town town : townyUniverse.getTowns()) {
            town.setActiveWar(false);
            WarDatabase.removeWarringTown(town);
            WarDataController.removeEndTime(town);
        }

        for (Nation nation : townyUniverse.getNations()) {
            nation.setActiveWar(false);
            WarDatabase.removeWarringNation(nation);
            WarDataController.removeEndTime(nation);
        }

        for (Resident resident : townyUniverse.getResidents()) {
            WarDataController.removeResidentLivesMeta(resident);
        }

        WarDatabase.clearSets();
        WarDatabase.saveWarData();

        sender.sendMessage(Component.text("Purged all saved and ongoing wars!").color(NamedTextColor.GREEN));
    }

    private void parseEndCommand(WarringEntity winner, WarringEntity loser) {
        if (winner == null || loser == null) {
            sender.sendMessage(Component.text("One of the towns doesn't have a war! Are you sure the names are correct?").color(NamedTextColor.RED));
            return;
        }

        War war = winner.getWar();
        war.endWar(winner, loser);
    }

    private void setFrozen(Collection<TownBlock> blocks, boolean toggle) {
        for (TownBlock block: blocks) {
            Chunk chunk = block.getWorldCoord().getBukkitWorld().getChunkAt(block.getX(), block.getZ());
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
        System.out.println("Set frozen blocks to: " + toggle);
    }
}
