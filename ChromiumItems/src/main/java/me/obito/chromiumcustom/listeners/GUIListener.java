package me.obito.chromiumcustom.listeners;

import me.obito.chromiumcustom.util.GUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class GUIListener implements Listener {

	private Consumer<InventoryClickEvent> onClickConsumer;
	
	public void setOnClickConsumer(Consumer<InventoryClickEvent> e) {
		this.onClickConsumer = e;
	}
	
	public Consumer<InventoryClickEvent> getOnClickConsumer() {
		return onClickConsumer;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		GUI gui = GUI.getByInventory(e.getView().getTopInventory());
		
		if(gui != null) {
			if(e.getAction().equals(InventoryAction.PICKUP_ALL)) {
				gui.getOnClickConsumer().accept(e);
			} else {
				e.setCancelled(true);
			}
		}
	}
	
}
