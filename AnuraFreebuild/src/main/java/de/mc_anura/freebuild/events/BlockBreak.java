package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.BlockTools;
import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import java.util.Arrays;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockBreak implements Listener {

    public static void log(BlockEvent ev) {
        System.out.println(ev.getEventName() + ": " + ev.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player P = event.getPlayer();
        Block b = event.getBlock();
        
        Boolean owner = RegionManager.isOwner(P, b.getLocation());
        if (owner != null) {
            if (!owner) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onBlockBreakMonitor(BlockBreakEvent event) {
        log(event);
        Player P = event.getPlayer();
        Block b = event.getBlock();

        if (!RegionManager.isFree(b.getLocation())) {
            return;
        }

        if (!P.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        ResetManager.blockDestroyed(b);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        log(event);
    }
}
