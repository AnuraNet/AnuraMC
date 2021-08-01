package de.mc_anura.freebuild;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.database.DB;
import de.mc_anura.core.database.MySQL.PreparedUpdate;
import de.mc_anura.core.util.Tuple;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class ResetManager {
    
    private static final long RESET_TIME = 10 * 1000;
    
    private static final List<Tuple<Long, BlockState>> blocks = new ArrayList<>();
    private static final ArrayList<Tuple<Long, EntityData>> entities = new ArrayList<>();

    private static final List<EntityType> respawn = Arrays.asList(
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.HORSE,
            EntityType.WOLF,
            EntityType.VILLAGER,
            EntityType.SHEEP,
            EntityType.MUSHROOM_COW,
            EntityType.PIG
        );
    
    public static void init() {
        AnuraThread.async(() -> {
            try {
                ResultSet rs = DB.querySelect("SELECT * FROM respawnBlocks");
                while (rs.next()) {
                    World w = Bukkit.getWorld(rs.getString("world"));
                    if (w == null) continue;
                    Location l = new Location(w, rs.getInt("X"), rs.getInt("Y"), rs.getInt("Z"));
                    String blockdata = rs.getString("blockdata");
                    long respawnTime = rs.getLong("time");
                    AnuraThread.queueSync(() -> {
                        BlockState s = w.getBlockAt(l).getState();
                        s.setBlockData(Bukkit.createBlockData(blockdata));
                        blocks.add(new Tuple<>(respawnTime * 1000, s));
                    });
                }
            } catch (SQLException ex) {
                Logger.getLogger(AnuraFreebuild.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        AnuraThread.add(Bukkit.getScheduler().runTaskTimer(AnuraFreebuild.getInstance(), () -> {
            int i = 0;
            Iterator<Tuple<Long, BlockState>> block_it = blocks.iterator();
            while (block_it.hasNext() && i < 500) {
                Tuple<Long, BlockState> block = block_it.next();
                if (block.x <= System.currentTimeMillis()) {
                    BlockState newState = block.y;
                    Location l = newState.getLocation();

                    boolean changed = false;
                    if (BlockTools.isOre(newState.getType())) {
                        l = BlockTools.getNewOreLocation(newState.getLocation());
                        changed = true;
                    }
                    if (!changed) {
                        newState.update(true, false);
                    } else {
                        Block b = l.getBlock();
                        b.setBlockData(newState.getBlockData());
                        if (l != newState.getLocation()) {
                            Block old = newState.getLocation().getBlock();
                            old.setType(Material.STONE);
                        }
                    }
                    block_it.remove();
                }
                i++;
            }
        }, 5, 10));

        AnuraThread.add(Bukkit.getScheduler().runTaskTimer(AnuraFreebuild.getInstance(), () -> {
            int i = 0;
            Iterator<Tuple<Long, EntityData>> entities_it = entities.iterator();
            while (entities_it.hasNext() && i < 50) {
                Tuple<Long, EntityData> entity = entities_it.next();
                if (entity.x <= System.currentTimeMillis()) {
                    EntityData data = entity.y;
                    Location loc = data.getLoc();
                    EntityType type = data.getType();
                    World w = loc.getWorld();
                    Location l = BlockTools.getPossibleSpawn(loc);
                    if (w != null)
                        w.spawnEntity(l, type);

                    entities_it.remove();
                }
                i++;
            }
        }, 7, 20));
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
            upd.add(st.y.getWorld().getName(), st.y.getX(), st.y.getY(), st.y.getZ(), st.y.getBlockData().getAsString(), (long) (st.x / 1000));
        }
        upd.done();
    }

    public static void entityDied(LivingEntity entity) {
        if (respawn.contains(entity.getType())) {
            entities.add(new Tuple<>(System.currentTimeMillis() + RESET_TIME, new EntityData(entity.getLocation(), entity.getType())));
        }
    }
    
    public static class BlockInfo {

        private final Location loc;
        private final BlockData data;

        public BlockInfo(Location l, BlockData dat) {
            loc = l;
            data = dat;
        }

        public Location getLoc() {
            return loc;
        }

        public BlockData getData() {
            return data;
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
