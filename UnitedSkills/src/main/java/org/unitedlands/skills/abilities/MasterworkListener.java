package org.unitedlands.skills.abilities;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.unitedlands.skills.UnitedSkills;

public class MasterworkListener implements Listener {

    private final UnitedSkills unitedSkills;

    public MasterworkListener(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onMasterworkAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager))
            return;
        if (!(event.getEntity() instanceof Player victim))
            return;
        double damageModifier = getMasterworkDamageModifier(victim);
        if (damageModifier == 0.0)
            return;
        if (!isHoldingMasterworkWeapon(damager))
            return;
        event.setDamage(event.getDamage() + (event.getDamage() * damageModifier));
    }

    private boolean isHoldingMasterworkWeapon(Player damager) {
        CustomStack customStack = CustomStack.byItemStack(damager.getInventory().getItemInMainHand());
        if (customStack == null)
            return false;
        return customStack.getId().equals("oiled_masterwork_sword") || customStack.getId().equals("oiled_masterwork_axe");
    }

    private double getMasterworkDamageModifier(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        double modifier = 0.0;
        for (ItemStack piece: armor) {
            CustomStack customStack = CustomStack.byItemStack(piece);
            if (customStack == null)
                continue;
            if (customStack.getId().contains("masterwork"))
                continue;
            switch (customStack.getItemStack().getType()) {
                case LEATHER_HELMET, LEATHER_BOOTS -> modifier += getModifier("tips");
                case LEATHER_CHESTPLATE, LEATHER_LEGGINGS -> modifier += getModifier("middle");
            }
        }
        return modifier;
    }

    private double getModifier(String path) {
        return unitedSkills.getConfig().getDouble("masterwork-modifier." + path);
    }
}
