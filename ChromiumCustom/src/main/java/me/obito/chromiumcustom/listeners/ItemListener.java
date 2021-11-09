package me.obito.chromiumcustom.listeners;

import de.tr7zw.nbtapi.NBTItem;
import me.obito.chromiumcustom.util.CustomItem;
import me.obito.chromiumcustom.util.ToolCustomItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ItemListener implements Listener {

    private Consumer<PlayerInteractEvent> onRightClickConsumer;
    private Consumer<EntityShootBowEvent> onBowFireConsumer;
    private Consumer<ProjectileLaunchEvent> onThrowConsumer;

    public void setOnRightClickConsumer(Consumer<PlayerInteractEvent> e) {
        this.onRightClickConsumer = e;
    }

    public Consumer<PlayerInteractEvent> getOnRightClickConsumer() {
        return onRightClickConsumer;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if(p.getEquipment().getChestplate() != null) {
                CustomItem custom = CustomItem.getCustomItemByName(p.getEquipment().getChestplate().getItemMeta().getDisplayName());
                if(custom != null) {
                    double health = p.getHealth() - e.getFinalDamage();
                    p.setHealth(health < 0 ? 0 : health);
                    p.setNoDamageTicks(0);
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 5, 1);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        NBTItem item = new NBTItem(e.getItem().getItemStack());
        if(item.hasKey("SOURCE")) {
            if(item.getString("SOURCE").equals(e.getPlayer().getUniqueId().toString())) {
                e.getItem().setPickupDelay(20);
                e.setCancelled(true);
            } else {
                e.getItem().remove();
                e.getPlayer().setNoDamageTicks(0);
                e.getPlayer().damage(2);
                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 5, 1);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if(e.getItem() == null)
            return;
        if(e.getItem().getType().equals(Material.AIR))
            return;
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            CustomItem custom = verifyCustomItem(e.getItem());
            ToolCustomItem customTool = ToolCustomItem.getItem(e.getItem());
            if(custom != null) {
                if(custom.getOnRightClickConsumer() != null) {
                    custom.getOnRightClickConsumer().accept(e);
                    return;
                }
            }
            if(customTool != null) {
                if(customTool.getOnRightClickConsumer() != null) {
                    customTool.getOnRightClickConsumer().accept(e);
                    return;
                }
            }
        }
    }

    public void setOnBowFireConsumer(Consumer<EntityShootBowEvent> e) {
        this.onBowFireConsumer = e;
    }

    public Consumer<EntityShootBowEvent> getOnBowFireConsumer() {
        return onBowFireConsumer;
    }

    @EventHandler
    public void onBowFire(EntityShootBowEvent e) {
        ToolCustomItem custom = ToolCustomItem.getItem(e.getBow());
        if(custom != null && custom.getOnBowFireConsumer() != null) {
            custom.getOnBowFireConsumer().accept(e);
        }
    }

    public void setOnThrowConsumer(Consumer<ProjectileLaunchEvent> e) {
        this.onThrowConsumer = e;
    }

    public Consumer<ProjectileLaunchEvent> getOnThrowConsumer() {
        return onThrowConsumer;
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();
            ItemStack stack = player.getItemInHand();
            CustomItem custom = verifyCustomItem(stack);
            if(custom != null && custom.getOnThrowConsumer() != null) {
                custom.getOnThrowConsumer().accept(e);
            }
        }

    }

    private CustomItem verifyCustomItem(ItemStack item) {
        return CustomItem.getCustomItem(item);
    }
}
