package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlace implements Listener {

    private boolean checkAllowed(BlockPlaceEvent event) {
        Player P = event.getPlayer();
        Boolean owner = RegionManager.isOwner(P, event.getBlock().getLocation());
        if (owner != null) {
            if (!owner) {
                event.setCancelled(true);
            }
            return true;
        }
        return !P.getGameMode().equals(GameMode.SURVIVAL);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (checkAllowed(event)) return;
        ResetManager.addBlock(event.getBlockReplacedState());
    }

    @EventHandler
    public void onMultiPlace(BlockMultiPlaceEvent event) {
        if (checkAllowed(event)) return;
        for (BlockState state : event.getReplacedBlockStates()) {
            if (state.getBlock().equals(event.getBlock())) continue;
            ResetManager.addBlock(state);
        }
    }
}
