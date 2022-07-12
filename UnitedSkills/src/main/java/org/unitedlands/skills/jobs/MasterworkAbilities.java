package org.unitedlands.skills.jobs;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.unitedlands.skills.UnitedSkills;

import java.util.Arrays;

public class MasterworkAbilities implements Listener {
    private final UnitedSkills unitedSkills;

    public MasterworkAbilities(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("boosted-masterwork-health")) {
            player.removeMetadata("boosted-masterwork-health", unitedSkills);
        }
    }

    public void runHealthIncrease() {
        unitedSkills.getServer().getScheduler().runTaskTimer(unitedSkills, task -> {
            String metadataKey = "boosted-masterwork-health";
            for (Player player : unitedSkills.getServer().getOnlinePlayers()) {
                if (hasFullMasterworkArmor(player) && !player.hasMetadata(metadataKey)) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(24D);
                    player.setMetadata(metadataKey, new FixedMetadataValue(unitedSkills, true));
                } else if (player.hasMetadata(metadataKey) && !hasFullMasterworkArmor(player)) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20D);
                    player.removeMetadata(metadataKey, unitedSkills);
                }
            }
        }, 0L, 20L);
    }

    private boolean hasFullMasterworkArmor(Player player) {
        EntityEquipment equipment = player.getEquipment();
        ItemStack[] armorPieces = getArmorPieces();
        return Arrays.deepEquals(armorPieces, equipment.getArmorContents());
    }

    private ItemStack[] getArmorPieces() {
        return new ItemStack[]{
                CustomStack.getInstance("unitedlands:masterwork_boots").getItemStack(),
                CustomStack.getInstance("unitedlands:masterwork_leggings").getItemStack(),
                CustomStack.getInstance("unitedlands:masterwork_chestplate").getItemStack(),
                CustomStack.getInstance("unitedlands:masterwork_helmet").getItemStack()
        };
    }

}
