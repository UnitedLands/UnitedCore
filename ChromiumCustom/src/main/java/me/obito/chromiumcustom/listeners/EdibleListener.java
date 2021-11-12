package me.obito.chromiumcustom.listeners;

import me.obito.chromiumcustom.util.EdibleCustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class EdibleListener implements Listener {

    @SuppressWarnings("unchecked")



    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();

        EdibleCustomItem custom = null;
        boolean isMainHand = false;

        if(e.getItem().equals(p.getEquipment().getItemInOffHand())) {
            custom = EdibleCustomItem.getEdibleCustomItem(p.getEquipment().getItemInOffHand());
        } else if (e.getItem().equals(p.getEquipment().getItemInMainHand())){
            isMainHand = true;
            custom = EdibleCustomItem.getEdibleCustomItem(p.getEquipment().getItemInMainHand());
        }

        if(custom == null) {
            return;
        } else {
            if(isMainHand) {
                p.getEquipment().getItemInMainHand().setAmount(p.getEquipment().getItemInMainHand().getAmount()-1);
            } else {
                p.getEquipment().getItemInOffHand().setAmount(p.getEquipment().getItemInOffHand().getAmount()-1);
            }
            int newHunger = p.getFoodLevel()+custom.getHunger();
            p.setFoodLevel(newHunger > 20 ? 20 : newHunger);
            custom.getOnEatApply().forEach( (effect) -> {
                p.addPotionEffect(effect);
            });
            e.setCancelled(true);
        }
    }

}
