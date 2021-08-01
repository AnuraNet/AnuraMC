package de.mc_anura.core.listeners;

import de.mc_anura.core.tools.Villagers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamage implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntityType() == EntityType.VILLAGER)) return;
        if (Villagers.is((Villager) event.getEntity())) event.setCancelled(true);
    }
}
