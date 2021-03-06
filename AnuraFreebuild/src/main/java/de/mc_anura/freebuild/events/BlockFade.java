package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class BlockFade implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        log(event);
        Location l = event.getBlock().getLocation();
        Material mat = event.getBlock().getType();
        if ((mat.equals(Material.SNOW) || mat.equals(Material.ICE) || Tag.CORAL_BLOCKS.isTagged(mat)) && RegionManager.isFree(l)) {
            ResetManager.addBlock(event.getBlock());
        }
    }
}
