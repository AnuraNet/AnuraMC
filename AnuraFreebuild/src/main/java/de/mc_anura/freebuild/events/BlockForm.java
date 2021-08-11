package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class BlockForm implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onBlockForm(BlockFormEvent event) {
        log(event);

        Block b = event.getBlock();

        Material mat = event.getNewState().getType();
        if (mat == Material.ICE || mat == Material.SNOW) {
            return;
        }

        if (!RegionManager.isFree(b.getLocation())) {
            return;
        }

        ResetManager.addBlock(b);
    }
}
