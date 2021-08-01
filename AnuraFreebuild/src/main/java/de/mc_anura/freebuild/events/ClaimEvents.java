package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ClaimManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class ClaimEvents implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(inClaim(event.getPlayer()));
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        event.setCancelled(inClaim(event.getPlayer()));
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        event.setCancelled(inClaim(event.getPlayer()));
    }

    @EventHandler
    public void onInvInteract(InventoryClickEvent event) {
        event.setCancelled(inClaim((Player) event.getWhoClicked()));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        event.setCancelled(inClaim((Player) event.getEntity()));
    }

    private boolean inClaim(Player P) {
        return ClaimManager.isClaiming(P);
    }
}
