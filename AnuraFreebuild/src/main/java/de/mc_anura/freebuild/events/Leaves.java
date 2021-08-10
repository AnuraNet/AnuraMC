package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.Random;
import java.util.UUID;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class Leaves implements Listener {

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        log(event);
        Location l = event.getBlock().getLocation();
        if (!RegionManager.isFree(l)) return;
        ResetManager.addBlock(event.getBlock());
        if (l.clone().subtract(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
            l.getWorld().spawnFallingBlock(l, event.getBlock().getBlockData()).setDropItem(false);
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
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
