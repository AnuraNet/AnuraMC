package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeavesDecay implements Listener {

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        Location l = event.getBlock().getLocation();
        if (!RegionManager.isFree(l)) return;
        if (l.clone().subtract(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
            l.getWorld().spawnFallingBlock(l, event.getBlock().getBlockData()).setDropItem(false);
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }
}
