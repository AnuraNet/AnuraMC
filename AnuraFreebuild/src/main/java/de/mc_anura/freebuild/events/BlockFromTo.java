package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Objects;

public class BlockFromTo implements Listener {

    @EventHandler
    public void onBlockFronTo(BlockFromToEvent event) {
        Block from = event.getBlock();
        Block to = event.getToBlock();
        if (RegionManager.isFree(from.getLocation())) {
            if (RegionManager.isFree(to.getLocation())) {
                ResetManager.addBlock(from);
                ResetManager.addBlock(to);
            } else {
                event.setCancelled(true);
            }
        } else {
            if (RegionManager.isFree(to.getLocation())) {
                event.setCancelled(true);
            } else if (!Objects.equals(RegionManager.getOwner(from.getLocation()), RegionManager.getOwner(to.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }
}
