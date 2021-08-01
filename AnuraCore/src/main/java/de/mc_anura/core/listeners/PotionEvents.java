package de.mc_anura.core.listeners;

import de.mc_anura.core.events.AnuraLeaveEvent;
import de.mc_anura.core.tools.Potions;
import de.mc_anura.core.tools.Villagers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PotionEvents implements Listener {

    @EventHandler
    public void onPotionChange(EntityPotionEffectEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER && event.getAction() == Action.ADDED && event.getCause() != Cause.COMMAND) {
            if (Villagers.is((Villager) event.getEntity())) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getEntityType() != EntityType.PLAYER) return;
        if (event.getCause() == Cause.PLUGIN) return;
        Player P = (Player) event.getEntity();
        switch (event.getAction()) {
            case ADDED, CHANGED -> {
                Potions.addNativePotion(P, event.getNewEffect());
                event.setCancelled(true);
            }
            case REMOVED -> Potions.removeNativePotion(P, event.getOldEffect(), event.getCause() != Cause.EXPIRATION);
            case CLEARED -> Potions.removeNativePotion(P, event.getOldEffect(), true);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Potions.join(event.getPlayer());
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onDisconnect(AnuraLeaveEvent event) {
        Potions.logout(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Potions.refresh(event.getPlayer());
    }
}
