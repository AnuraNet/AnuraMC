package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.BlockTools;
import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import java.util.Arrays;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

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
        Player P = event.getPlayer();
        Block b = event.getBlock();

        Boolean owner = RegionManager.isOwner(P, b.getLocation());
        if (owner != null) {
            return;
        }

        if (!P.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        ResetManager.addBlock(b);
        BlockState state = b.getRelative(BlockFace.DOWN).getState();
        if (BlockTools.isBreakingBottom(state.getBlockData())) {
            ResetManager.addBlock(state);
        }
        // TODO: Chorus plants, better ore respawning (grouping)
        Block up = b;
        while (true) {
            up = up.getRelative(BlockFace.UP);
            if (BlockTools.isBreakingTop(up.getBlockData())) {
                ResetManager.addBlock(state);
            } else {
                break;
            }
        }
        for (BlockFace face : Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH)) {
            state = b.getRelative(face).getState();
            if (BlockTools.isBreakingSide(state.getBlockData(), face.getOppositeFace())) {
                ResetManager.addBlock(state);
            }
        }
    }
}
