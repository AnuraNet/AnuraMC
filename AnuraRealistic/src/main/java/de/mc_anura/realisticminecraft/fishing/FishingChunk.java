package de.mc_anura.realisticminecraft.fishing;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.database.DB;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FishingChunk {

    private static final double CHUNK_REGENERATION = 9.0 / 60.0; // 1 per 7 minutes
    private static final int MAX_FISH = 15; // within chunk

    //////////////////
    private int id;
    private final Chunk chunk;
    private double caught;
    private long lastUpdated;

    public FishingChunk(Chunk chunk) {
        this(chunk, 0, System.currentTimeMillis() / 1000, -1);
    }

    public FishingChunk(Chunk chunk, int caught, long lastUpdated, int id) {
        this.id = id;
        this.chunk = chunk;
        this.caught = caught;
        this.lastUpdated = lastUpdated;
    }

    public Chunk getChunk() {
        return chunk;
    }

    private void update() {
        long notUpdatedTime = System.currentTimeMillis() / 1000 - lastUpdated;
        long minutes = notUpdatedTime / 60;

        caught -= minutes * CHUNK_REGENERATION;
        if (caught < 0) {
            caught = 0;
        }

        this.lastUpdated = System.currentTimeMillis() / 1000 - (notUpdatedTime % 60);
    }

    private int getFish() {
        update();
        int fishes = MAX_FISH - (int) Math.ceil(caught);
        if (fishes < 0) {
            fishes = 0;
        }
        return fishes;
    }

    public boolean isOverfished() {
        return getFish() == 0;
    }

    public void addCaught() {
        caught += 1;
        save(true);
    }

    public void save(boolean async) {
        update();
        if (id <= 0) {
            DB.queryUpdate(async, DB.getFirstKey((key) -> id = key), "INSERT INTO fishingChunks (x, z, world, catched, lastUpdated) VALUES (?, ?, ?, ?, ?)",
                    chunk.getX(), chunk.getZ(), chunk.getWorld().getName(), caught, lastUpdated);
        } else {
            DB.queryUpdate(async, "UPDATE fishingChunks SET catched = ?, lastUpdated = ? WHERE primeKey = ?",
                    caught, lastUpdated, id);
        }
    }

    /////////////////////
    private static LoadingCache<Chunk, FishingChunk> FISHING_CACHE;
    private static final List<FishingTask> tasks = Collections.synchronizedList(new LinkedList<>());

    public static void init() {
        CacheLoader<Chunk, FishingChunk> loader = new CacheLoader<>() {
            @Override
            public FishingChunk load(Chunk chunk) throws Exception {
                ResultSet rs = DB.querySelect(
                        "SELECT * FROM fishingChunks WHERE x = ? AND z = ? AND world = ?",
                        chunk.getX(), chunk.getZ(), chunk.getWorld().getName()
                );
                if (!rs.first()) {
                    return new FishingChunk(chunk);
                }
                return new FishingChunk(chunk, rs.getInt("catched"), rs.getInt("lastUpdated"), rs.getInt("primeKey"));
            }
        };

        FISHING_CACHE = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(loader);

        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(RealisticMinecraft.getInstance(), () -> {
            synchronized (tasks) {
                Iterator<FishingTask> it = tasks.iterator();
                while (it.hasNext()) {
                    FishingTask task = it.next();
                    if (task.getTimestamp() < System.currentTimeMillis()) {
                        task.getR().run();
                        it.remove();
                    }
                }
            }
        }, 2, 2));
    }

    public static FishingChunk get(Chunk chunk) {
        return FISHING_CACHE.getUnchecked(chunk);
    }

    public static boolean contains(Chunk chunk) {
        return FISHING_CACHE.getIfPresent(chunk) != null;
    }

    public static void queueTask(int seconds, Runnable r) {
        tasks.add(new FishingTask(System.currentTimeMillis() + (seconds * 1000L), r));
    }
}
