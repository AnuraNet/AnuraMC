package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class BlockFade implements Listener {

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Location l = event.getBlock().getLocation();
        Material mat = event.getBlock().getType();
        if ((mat.equals(Material.SNOW) || mat.equals(Material.ICE)) && RegionManager.isFree(l)) {
            ResetManager.addBlock(event.getBlock());
        }
    }
}
