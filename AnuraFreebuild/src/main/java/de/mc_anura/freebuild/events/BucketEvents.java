package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ResetManager;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

public class BucketEvents implements Listener {

    @EventHandler
    public static void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Boolean owner = RegionManager.isOwner(event.getPlayer(), event.getBlock().getLocation());
        if (owner != null && !owner) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onBucketFill(PlayerBucketFillEvent event) {
        Boolean owner = RegionManager.isOwner(event.getPlayer(), event.getBlock().getLocation());
        if (owner != null && !owner) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public static void onBucketEmptyMonitor(PlayerBucketEmptyEvent event) {
        System.out.println("PlayerBucketEmptyEvent");
        handleMonitor(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public static void onBucketFillMonitor(PlayerBucketFillEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack != null && stack.getType() == Material.MILK_BUCKET) {
            return;
        }
        System.out.println("PlayerBucketFillEvent");
        handleMonitor(event);
    }

    private static void handleMonitor(PlayerBucketEvent event) {
        Player P = event.getPlayer();
        if (!P.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        if (!RegionManager.isFree(event.getBlock().getLocation())) {
            return;
        }

        ResetManager.addBlock(event.getBlock());
    }
}
