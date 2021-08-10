package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import io.papermc.paper.event.block.PlayerShearBlockEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShearBlock implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onPlayerShearBlock(PlayerShearBlockEvent event) {
        System.out.println("PlayerShearBlockEvent: " + event.getBlock().getLocation());
        Block block = event.getBlock();
        if (block.getType() == Material.PUMPKIN) {
            if (!RegionManager.isFree(block.getLocation())) {
                return;
            }

            if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
                return;
            }

            ResetManager.addBlock(block);
        }
    }
}
