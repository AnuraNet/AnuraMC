package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class SandEvent implements Listener {

    @EventHandler
    public void onFallingBlock(EntityChangeBlockEvent e) {
        if (e.getEntityType().equals(EntityType.FALLING_BLOCK)) {
            UUID owner = RegionManager.getOwner(e.getBlock().getLocation());
            if (owner != null) {
                return;
            }
            if (e.getBlock().getType().equals(Material.AIR) && e.getTo().toString().contains("_LEAVES")) {
                Random r = new Random();
                Material down = e.getBlock().getRelative(BlockFace.DOWN).getType();
                if (r.nextInt(25) == 0 && (down.equals(Material.GRASS) || down.equals(Material.DIRT))) {
                    Material sapling = Material.getMaterial(e.getTo().toString().split("_")[0] + "_SAPLING");
                    BlockState b = e.getBlock().getState();
                    if (sapling != null) {
                        b.setType(sapling);
                        b.update(false, false);
                        e.setCancelled(true);
                        e.getEntity().remove();
                    }
                    return;
                }
            }
            ResetManager.addBlock(e.getBlock());
        }
    }
}
