package de.mc_anura.freebuild;

import com.destroystokyo.paper.MaterialTags;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.database.DB;
import de.mc_anura.core.database.MySQL.PreparedUpdate;
import de.mc_anura.core.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Piston events, Water forming sources, natural falling blocks (revert or nah?), explosion debris flying into protected zones

public class ResetManager {
    
    private static final long RESET_TIME = 10 * 1000;
    
    private static final Queue<Tuple<Long, BlockState>> blocks = new ConcurrentLinkedQueue<>();
    private static final Queue<Tuple<Long, EntityData>> entities = new ConcurrentLinkedQueue<>();

    private static final List<EntityType> respawn = Arrays.asList(
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.HORSE,
            EntityType.WOLF,
            //EntityType.VILLAGER,
            EntityType.SHEEP,
            EntityType.MUSHROOM_COW,
            EntityType.PIG
        );
    
    public static void init() {
        AnuraThread.async(() -> {
            try {
                ResultSet rs = DB.querySelect("SELECT * FROM respawnBlocks ORDER BY time ASC");
                while (rs.next()) {
                    World w = Bukkit.getWorld(rs.getString("world"));
                    if (w == null) continue;
                    Location l = new Location(w, rs.getInt("X"), rs.getInt("Y"), rs.getInt("Z"));
                    String blockdata = rs.getString("blockdata");
                    long respawnTime = rs.getLong("time");
                    AnuraThread.queueSync(() -> {
                        BlockState s = w.getBlockAt(l).getState();
                        s.setBlockData(Bukkit.createBlockData(blockdata));
                        blocks.offer(new Tuple<>(respawnTime * 1000, s));
                    });
                }
            } catch (SQLException ex) {
                Logger.getLogger(AnuraFreebuild.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        AnuraThread.add(Bukkit.getScheduler().runTaskTimer(AnuraFreebuild.getInstance(), () -> {
            int i = 0;
            do {
                Tuple<Long, BlockState> block = blocks.peek();
                if (block == null) {
                    break;
                }
                if (block.x > System.currentTimeMillis()) {
                    break;
                }
                blocks.poll();

                BlockState newState = block.y;
                Location l = newState.getLocation();

                boolean changed = false;
                Material base = Material.STONE;
                if (MaterialTags.ORES.isTagged(newState)) {
                    if (newState.getType().toString().contains("DEEPSLATE")) {
                        base = Material.DEEPSLATE;
                    } else if (newState.getType().toString().contains("NETHER")) {
                        base = Material.NETHERRACK;
                    }
                    l = BlockTools.getNewOreLocation(newState.getLocation(), base);
                    changed = true;
                }
                if (!changed) {
                    boolean physics = false;
                    Block b = l.getBlock();
                    BlockData before = b.getBlockData();
                    BlockData after = newState.getBlockData();
                    if ((before.getMaterial() == Material.WATER && after.getMaterial() != Material.WATER) ||
                        (before.getMaterial() != Material.WATER && after.getMaterial() == Material.WATER) ||
                        after.getMaterial() == Material.POINTED_DRIPSTONE) {
                        physics = true;
                    }
                    newState.update(true, physics);
                } else {
                    Block b = l.getBlock();
                    b.setBlockData(newState.getBlockData());
                    if (l != newState.getLocation()) {
                        Block old = newState.getLocation().getBlock();
                        old.setType(base);
                    }
                }

                i++;
            } while (i < 500);
        }, 5, 10));

        AnuraThread.add(Bukkit.getScheduler().runTaskTimer(AnuraFreebuild.getInstance(), () -> {
            int i = 0;
            do {
                Tuple<Long, EntityData> entity = entities.peek();
                if (entity == null) {
                    break;
                }
                if (entity.x > System.currentTimeMillis()) {
                    break;
                }
                entities.poll();

                EntityData data = entity.y;
                Location loc = data.getLoc();
                EntityType type = data.getType();
                World w = loc.getWorld();
                Location l = BlockTools.getPossibleSpawn(loc);
                if (w != null)
                    w.spawnEntity(l, type);

                i++;
            } while (i < 50);
        }, 7, 20));
    }

    public static void blockDestroyed(Block b) {
        // Ignore higher-up chorus plants, respawn a flower when the lowest block was broken
        if (b.getType() == Material.CHORUS_FLOWER || b.getType() == Material.CHORUS_PLANT) {
            if (b.getRelative(BlockFace.DOWN).getType() == Material.END_STONE) {
                BlockState state = b.getState();
                state.setType(Material.CHORUS_FLOWER);
                ResetManager.addBlock(state);
            }
            return;
        }

        ResetManager.addBlock(b);
        // TODO: better ore respawning (grouping), place small crops (like chorus flower)

        Block down = b;
        while (true) {
            down = down.getRelative(BlockFace.DOWN);
            if (BlockTools.isBreakingBottom(down.getBlockData())) {
                ResetManager.addBlock(down);
            } else {
                break;
            }
        }

        Block up = b;
        while (true) {
            up = up.getRelative(BlockFace.UP);
            // Respawn a flower when the supporting end stone was broken
            if (b.getType() == Material.END_STONE && (up.getType() == Material.CHORUS_PLANT || up.getType() == Material.CHORUS_FLOWER)) {
                BlockState state = up.getState();
                state.setType(Material.CHORUS_FLOWER);
                ResetManager.addBlock(state);
                break;
            }
            if (BlockTools.isBreakingTop(up.getBlockData())) {
                ResetManager.addBlock(up);
            } else {
                break;
            }
        }

        for (BlockFace face : Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH)) {
            Block side = b.getRelative(face);
            if (BlockTools.isBreakingSide(side.getBlockData(), face.getOppositeFace())) {
                ResetManager.addBlock(side);
            }
        }
    }
    
    public static boolean blocksContains(Location loc) {
        for (Tuple<Long, BlockState> state : blocks) {
            if (state.y.getLocation().equals(loc)) return true;
        }
        return false;
    }

    public static void addBlock(Block b) {
        addBlock(b.getState());
    }

    public static void addBlock(BlockState s) {
        if (!blocksContains(s.getLocation()) && s.getWorld().equals(AnuraFreebuild.getWorld())) {
            blocks.add(new Tuple<>(System.currentTimeMillis() + RESET_TIME, s));
        }
    }

    public static void save(boolean async) {
        AnuraFreebuild.getWorld().save();
        if (async) {
            AnuraThread.async(ResetManager::saveResetBlocks);
        } else {
            saveResetBlocks();
        }
    }

    private static void saveResetBlocks() {
        DB.queryUpdate("TRUNCATE TABLE respawnBlocks");
        if (blocks.isEmpty()) return;
        PreparedUpdate upd = DB.queryPrepUpdate("INSERT INTO respawnBlocks(world, X, Y, Z, blockdata, time) VALUES (?, ?, ?, ?, ?, ?)");
        if (upd == null)
            return;

        for (Tuple<Long, BlockState> st : blocks) {
            upd.add(st.y.getWorld().getName(), st.y.getX(), st.y.getY(), st.y.getZ(), st.y.getBlockData().getAsString(), st.x / 1000);
        }
        upd.done();
    }

    public static void entityDied(LivingEntity entity) {
        if (respawn.contains(entity.getType())) {
            entities.add(new Tuple<>(System.currentTimeMillis() + RESET_TIME, new EntityData(entity.getLocation(), entity.getType())));
        }
    }

    public static class EntityData {

        private final Location loc;
        private final EntityType type;

        public EntityData(Location l, EntityType type) {
            loc = l;
            this.type = type;
        }

        public Location getLoc() {
            return loc;
        }

        public EntityType getType() {
            return type;
        }
    }
}
