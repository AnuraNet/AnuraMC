package de.mc_anura.core.listeners;

import de.mc_anura.core.tools.AnuraInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryEvent implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        Inventory i = event.getView().getTopInventory();
        Player P = (Player) event.getWhoClicked();
        AnuraInventory inv = AnuraInventory.getOpenInv(P);
        if (inv == null) return;
        event.setCancelled(true);
        if (i == event.getClickedInventory()) {
            inv.checkInvClick(event.getSlot(), P, event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY);
        } else {
            inv.checkPlayerInvClick(P, event);
        }
    }

    @EventHandler
    public void onInvDrag(InventoryDragEvent event) {
        Player P = (Player) event.getWhoClicked();
        AnuraInventory inv = AnuraInventory.getOpenInv(P);
        if (inv == null) return;
        event.setCancelled(true);
        inv.checkInvClick(event.getInventorySlots().iterator().next(), P, false);
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        Inventory i = event.getInventory();
        Player P = (Player) event.getPlayer();
        AnuraInventory inv = AnuraInventory.getOpenInv(P);
        if (inv == null) return;
        inv.close(P);
    }
}
