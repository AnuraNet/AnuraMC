package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class FallingBlock implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFallingBlock(EntityChangeBlockEvent e) {
        System.out.println("EntityChangeBlockEvent: " + e.getBlock().getLocation());
        if (e.getEntityType().equals(EntityType.FALLING_BLOCK)) {
            if (!RegionManager.isFree(e.getBlock().getLocation())) return;
            // If (e.g sand) block fell, check for adjacent faces (other blocks may drop too)
            if (!e.getTo().isSolid()) {
                ResetManager.blockDestroyed(e.getBlock());
            } else {
                ResetManager.addBlock(e.getBlock());
            }
        }
    }
}
