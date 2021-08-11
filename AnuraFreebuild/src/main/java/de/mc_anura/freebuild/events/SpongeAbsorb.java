package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SpongeAbsorbEvent;

import java.util.Iterator;

public class SpongeAbsorb implements Listener {

    @EventHandler
    public void onSpongeAbsorbProtect(SpongeAbsorbEvent event) {
        Block block = event.getBlock();
        boolean spongeOutside = RegionManager.isFree(block.getLocation());
        Iterator<BlockState> blocks = event.getBlocks().iterator();
        while (blocks.hasNext()) {
            BlockState b = blocks.next();
            boolean outside = RegionManager.isFree(b.getLocation());
            if (!outside && spongeOutside) {
                blocks.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        System.out.println("SpongeAbsorbEvent: " + event.getBlock().getLocation());
        if (RegionManager.isFree(event.getBlock().getLocation())) {
            ResetManager.addBlock(event.getBlock());
        }

        for (BlockState b : event.getBlocks()) {
            if (RegionManager.isFree(b.getLocation())) {
                ResetManager.addBlock(b.getLocation().getBlock());
            }
        }
    }
}
