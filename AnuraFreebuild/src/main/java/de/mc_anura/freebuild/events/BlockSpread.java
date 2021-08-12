package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class BlockSpread implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        log(event);
        Location l = event.getBlock().getLocation();
        if (event.getNewState().getType() == Material.GRASS_BLOCK) {
            return;
        }
        if (RegionManager.isFree(l)) {
            ResetManager.addBlock(event.getBlock());
        }
    }
}
