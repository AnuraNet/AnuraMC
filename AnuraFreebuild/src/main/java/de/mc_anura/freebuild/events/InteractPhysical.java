package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractPhysical implements Listener {

    @EventHandler
    public void onFarmland(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType().equals(Material.FARMLAND)) {
            Boolean owner = RegionManager.isOwner(e.getPlayer(), e.getClickedBlock().getLocation());
            if (owner != null) {
                if (!owner) {
                    e.setCancelled(true);
                }
                return;
            }
            Block upb = e.getClickedBlock().getRelative(BlockFace.UP);
            Block clicked = e.getClickedBlock();
            if (upb.getType().equals(Material.WHEAT) || upb.getType().equals(Material.POTATO) || upb.getType().equals(Material.CARROT) ||
                upb.getType().equals(Material.BEETROOTS) || upb.getType().equals(Material.MELON_STEM) || upb.getType().equals(Material.PUMPKIN_STEM)) {
                ResetManager.addBlock(upb);
            }
            ResetManager.addBlock(clicked);
        }
    }
}
