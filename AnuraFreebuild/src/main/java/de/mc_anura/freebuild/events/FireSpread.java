package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class FireSpread implements Listener {

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        Location l = event.getBlock().getLocation();
        if (event.getNewState().getType().equals(Material.FIRE) && RegionManager.isFree(l)) {
            ResetManager.addBlock(event.getBlock());
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Location l = event.getBlock().getLocation();
        if (RegionManager.isFree(l)) {
            ResetManager.addBlock(event.getBlock());
        }
    }
}
