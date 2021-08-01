package de.mc_anura.core.listeners;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.tools.Villagers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

public class EntityFish implements Listener {

    @EventHandler
    public void onPlayerFish(PlayerFishEvent e) {
        Entity entity = e.getCaught();
        if (!e.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY) || entity == null) return;
        if (entity.getType() == EntityType.PLAYER) {
            e.setCancelled(true);
            e.getHook().remove();
            return;
        }
        if (entity.getType() == EntityType.VILLAGER) {
            Villager v = (Villager) entity;
            if (Villagers.is(v)) {
                Location loc = v.getLocation();
                AnuraThread.add(Bukkit.getScheduler().runTaskLater(AnuraCore.getInstance(), () -> {
                    entity.setVelocity(new Vector(0, 0, 0));
                    entity.teleport(loc);
                }, 1));
            }
        }
    }
}
