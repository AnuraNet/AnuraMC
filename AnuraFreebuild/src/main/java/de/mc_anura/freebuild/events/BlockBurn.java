package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class BlockBurn implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        log(event);
        Location l = event.getBlock().getLocation();
        if (RegionManager.isFree(l)) {
            ResetManager.addBlock(event.getBlock());
        }
    }
}
