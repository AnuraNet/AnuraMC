package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

public class EntityExplode implements Listener {

    @EventHandler
    public void onExplodeEntity(EntityExplodeEvent event) {
        Location center = event.getLocation();
        UUID owner = RegionManager.getOwner(center);
        boolean area = owner != null;
        for (Block b : event.blockList()) {
            if (b.getType() == Material.FIRE) continue;
            if (!area) ResetManager.addBlock(b);
            if (b.getType() == Material.TNT) continue;
            FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation(), b.getBlockData());
            Location fl = fb.getLocation();
            Location rel = new Location(fb.getWorld(), fl.getX() - center.getX(), fl.getY() - center.getY(), fl.getZ() - center.getZ());
            Vector v = new Vector(3f / rel.getX(), 3f / rel.getY(), 3f / rel.getZ());
            v.normalize();
            v.multiply(fl.distance(center) / 2.5f);
            fb.setVelocity(v);
        }
        event.setYield(0);
    }
}
