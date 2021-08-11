package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Iterator;
import java.util.UUID;

public class StructureGrow implements Listener {

    @EventHandler
    public void onStructureGrowProtect(StructureGrowEvent event) {
        Player P = event.getPlayer();
        UUID uuid = P == null ? null : P.getUniqueId();
        boolean grewSaplingInside = !RegionManager.isFree(event.getLocation());

        Iterator<BlockState> blocks = event.getBlocks().iterator();
        while (blocks.hasNext()) {
            BlockState b = blocks.next();
            UUID owner = RegionManager.getOwner(b.getLocation());
            boolean nonOwnerGrewInside = owner != null && !owner.equals(uuid);
            // Sapling inside: Wächst immer (auch wenn nicht-Eigentümer mit Bonemeal)
            // Sapling outside: Block außerhalb wächst, innerhalb nur wenn Eigentümer mit Bonemeal
            if (!grewSaplingInside && nonOwnerGrewInside) {
                blocks.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onStructureGrow(StructureGrowEvent event) {
        System.out.println("StructureGrowEvent: " + event.getLocation());

        Player P = event.getPlayer();
        if (P != null && !P.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        for (BlockState b : event.getBlocks()) {
            if (RegionManager.isFree(b.getLocation())) {
                ResetManager.addBlock(b.getLocation().getBlock());
            }
        }
    }
}
