package de.mc_anura.realisticminecraft.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.util.BukkitUtils;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class LogBlockProvider {

    private static final LogBlock plugin = (LogBlock) Bukkit.getServer().getPluginManager().getPlugin("LogBlock");
    private static final Cache<Location, Object> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS).build();
    private static final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static final Object value = new Object();
    private static boolean queueStarted = false;

    @Contract("_ -> new")
    private static @NotNull Actor getActor(@NotNull Player p) {
        return new Actor(p.getName(), p.getUniqueId());
    }

    public static void queueFalling(@NotNull Player p, @NotNull Location l, @NotNull BlockData bd) {
        startQueue();
        Actor a = getActor(p);
        queue.add(() -> queueFallingBlock(a, l, bd));
    }

    private static void queueFallingBlock(@NotNull Actor a, @NotNull Location l, @NotNull BlockData bd) {
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        // Blocks only fall if they have a chance to start a velocity
        if (l.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            while (y > 0 && canFall(l.getWorld(), x, (y - 1), z)) {
                y--;
            }
        }
        // If y is 0 then the sand block fell out of the world :(
        if (y != 0) {
            Location finalLoc = new Location(l.getWorld(), x, y, z);
            Block finalBlock = finalLoc.getBlock();
            // Run this check to avoid false positives
            if (!BukkitUtils.getFallingEntityKillers().contains(finalBlock.getType())) {
                cache.put(finalLoc, value);
                if (finalBlock.getType() == Material.AIR || finalLoc.equals(l)) {
                    plugin.getConsumer().queueBlockPlace(a, finalLoc, bd);
                } else {
                    plugin.getConsumer().queueBlockReplace(a, finalLoc, finalBlock.getBlockData(), bd);
                }
            }
        }
    }

    public static void queueBlockBreak(@NotNull Player p, @NotNull Block b) {
        plugin.getConsumer().queueBlockBreak(getActor(p), b.getState());
    }

    public static boolean canFall(@NotNull World w, int x, int y, int z) {
        return BukkitUtils.canFallIn(w, x, (y - 1), z) && cache.getIfPresent(new Location(w, x, y, z)) == null;
    }

    public static void startQueue() {
        if (queueStarted) {
            return;
        }
        queueStarted = true;
        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(RealisticMinecraft.getInstance(), () -> {
            for (int i = 0; i < 200; i++) {
                Runnable poll = queue.poll();
                if (poll == null) {
                    continue;
                }
                poll.run();//TODO: Check SYNC
            }
        }, 10, 10));
    }
}
