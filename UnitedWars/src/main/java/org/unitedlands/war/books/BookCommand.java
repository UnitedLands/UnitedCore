package org.unitedlands.war.books;

import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.books.declaration.TownDeclarationBook;

public class BookCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            @NotNull Town targetTown = UnitedWars.TOWNY_API.getTown(args[0]);


            WarTarget warTarget = new WarTarget(Bukkit.getOfflinePlayer(targetTown.getMayor().getUUID()));
            TownDeclarationBook townDeclarationBook =  new TownDeclarationBook(new Declarer(player), warTarget);
            ItemStack book = townDeclarationBook.getBook();
            player.getInventory().setItemInMainHand(book);
        }
        return false;
    }
}
