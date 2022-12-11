package org.unitedlands.wars.war;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.books.warbooks.WritableDeclaration;
import org.unitedlands.wars.war.entities.WarringEntity;

import java.util.UUID;

public class WarUtil {


    public static boolean nationHasEnoughOnline(Nation nation) {
        return TownyAPI.getInstance().getOnlinePlayersInNation(nation).size() >= 1;
    }

    public static boolean townHasEnoughOnline(Town town) {
        return TownyAPI.getInstance().getOnlinePlayersInTown(town).size() >= 0;
    }

    public static WritableDeclaration generateWritableDeclaration(BookMeta bookMeta) {
        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();

        UUID declaringUUID = getUUID(pdc, "unitedwars.book.town");
        Declarer declarer = new Declarer(UnitedWars.TOWNY_API.getTown(declaringUUID));


        WarType warType = WarType.valueOf(pdc.get(NamespacedKey.fromString("unitedwars.book.type"), PersistentDataType.STRING).toUpperCase());
        UUID targetUUID = getUUID(pdc, "unitedwars.book.target");
        WarTarget warTarget = getWarTarget(warType, targetUUID);

        return new WritableDeclaration(declarer, warTarget, warType);
    }

    @NotNull
    private static UUID getUUID(PersistentDataContainer pdc, String key) {
        return UUID.fromString(pdc.get(NamespacedKey.fromString(key), PersistentDataType.STRING));
    }


    @NotNull
    private static WarTarget getWarTarget(WarType warType, UUID targetUUID) {
        WarTarget warTarget;
        if (warType == WarType.TOWNWAR)
            warTarget = new WarTarget(UnitedWars.TOWNY_API.getTown(targetUUID));
        else
            warTarget = new WarTarget(UnitedWars.TOWNY_API.getNation(targetUUID));
        return warTarget;
    }

    public static boolean isNotOnCooldownForWar(WarType type, TownyObject obj) {
        return !WarDataController.hasLastWarTime(obj) || !tooSoonForWar(type, obj);
    }

    private static boolean tooSoonForWar(WarType type, TownyObject obj) {
        return WarDataController.getLastWarTime(obj) + type.cooldown() > System.currentTimeMillis();
    }


    public static boolean hasSameWar(Resident first, Resident second) {
        Player player = first.getPlayer();
        Player secondPlayer = second.getPlayer();
        if (!WarDatabase.hasWar(player) || !WarDatabase.hasWar(secondPlayer))
            return false;

        return WarDatabase.getWar(player).equals(WarDatabase.getWar(secondPlayer));
    }

    public static boolean hasSameWar(WarringEntity first, WarringEntity second) {
        return first.getWar().equals(second.getWar());
    }

    public static WarringEntity getOpposingEntity(WarringEntity entity) {
        War war = entity.getWar();
        for (WarringEntity warringEntity : war.getWarringEntities())
            return warringEntity != entity ? warringEntity : null;

        return null;
    }
}
