package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class BlockPlace implements Listener {

    private void checkAllowed(BlockPlaceEvent event) {
        Player P = event.getPlayer();
        Boolean owner = RegionManager.isOwner(P, event.getBlock().getLocation());
        if (owner != null) {
            if (!owner) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler
    public void onBlockPlaceProtect(BlockPlaceEvent event) {
        checkAllowed(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        log(event);
        if (!RegionManager.isFree(event.getBlock().getLocation())) {
            return;
        }

        if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        ResetManager.addBlock(event.getBlockReplacedState());
    }

    @EventHandler
    public void onMultiPlaceProtect(BlockMultiPlaceEvent event) {
        checkAllowed(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMultiPlace(BlockMultiPlaceEvent event) {
        log(event);
        if (!RegionManager.isFree(event.getBlock().getLocation())) {
            return;
        }

        if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        for (BlockState state : event.getReplacedBlockStates()) {
            if (state.getBlock().equals(event.getBlock())) continue;
            ResetManager.addBlock(state);
        }
    }
}
