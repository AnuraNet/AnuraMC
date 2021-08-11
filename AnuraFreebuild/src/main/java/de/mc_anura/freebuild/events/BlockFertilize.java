package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;

import java.util.Iterator;
import java.util.UUID;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class BlockFertilize implements Listener {

    @EventHandler
    public void onBlockFertilizeProtect(BlockFertilizeEvent event) {
        Player P = event.getPlayer();
        UUID uuid = P == null ? null : P.getUniqueId();
        boolean grewCenterInside = !RegionManager.isFree(event.getBlock().getLocation());

        Iterator<BlockState> blocks = event.getBlocks().iterator();
        while (blocks.hasNext()) {
            BlockState b = blocks.next();
            UUID owner = RegionManager.getOwner(b.getLocation());
            boolean nonOwnerGrewInside = owner != null && !owner.equals(uuid);
            // Middle inside: Wächst immer (auch wenn nicht-Eigentümer mit Bonemeal)
            // Middle outside: Block außerhalb wächst, innerhalb nur wenn Eigentümer mit Bonemeal
            if (!grewCenterInside && nonOwnerGrewInside) {
                blocks.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled=true, priority= EventPriority.MONITOR)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        log(event);

        Player P = event.getPlayer();
        if (P != null && !P.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        Material mat = event.getBlock().getType();
        // TODO: Find solution for bamboo, weeping/twisting vines, kelp
        // TODO: (Problem: Player grows plant -> Plant grows naturally -> Player grown blocks are reverted -> Plant floats)
        if (Tag.CROPS.isTagged(mat) || mat == Material.COCOA_BEANS || mat == Material.SWEET_BERRY_BUSH || // Crops are allowed to grow
            Tag.TALL_FLOWERS.isTagged(mat) || // Tall flowers only drop an item
            Tag.SAPLINGS.isTagged(mat) || // trees are handled by StructureGrow
            mat == Material.BROWN_MUSHROOM || mat == Material.RED_MUSHROOM || // Mushrooms are handled by StructureGrow
            mat == Material.CRIMSON_FUNGUS || mat == Material.WARPED_FUNGUS) {
            return;
        }

        for (BlockState b : event.getBlocks()) {
            if (RegionManager.isFree(b.getLocation())) {
                ResetManager.addBlock(b.getLocation().getBlock());
            }
        }
    }
}
