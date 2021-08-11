package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EnDeathEvent implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        System.out.println("EntityDeathEvent: " + e.getEntityType());
        UUID owner = RegionManager.getOwner(e.getEntity().getLocation());
        if (owner != null) {
            return;
        }
        ResetManager.entityDied(e.getEntity());
    }
}
