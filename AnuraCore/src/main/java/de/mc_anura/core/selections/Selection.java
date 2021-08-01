package de.mc_anura.core.selections;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Selection {

    protected BlockVector min, max;
    protected List<Location> locations = new ArrayList<>();

    public List<BlockVector> getBlockVectors() {
        ArrayList<BlockVector> returnArray = new ArrayList<>();
        locations.forEach(location -> returnArray.add(location.toVector().toBlockVector()));
        return returnArray;
    }

    public World getWorld() {
        return locations.get(0).getWorld();
    }

    public void clear() {
        this.locations = new ArrayList<>();
    }

    public void clear(Location loc) {
        this.locations = new ArrayList<>();
        locations.add(loc);
    }

    public abstract boolean expand(int size, BlockFace bf);

    public boolean expandVert() {
        return (expand(255, BlockFace.UP) && expand(255, BlockFace.DOWN));
    }

    public abstract boolean contains(BlockVector bv);

    public boolean contains(Location loc) {
        return contains(loc.toVector().toBlockVector());
    }

    public int getCornerCount() {
        final int[] count = {0};
        locations.stream().filter(Objects::nonNull).forEach(location -> count[0]++);
        return count[0];
    }

    public void updateMinMax() {
        int xMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        int zMin = Integer.MAX_VALUE;
        int zMax = Integer.MIN_VALUE;
        for (Location loc : locations) {
            BlockVector vector = loc.toVector().toBlockVector();
            if (vector.getBlockX() < xMin) {
                xMin = vector.getBlockX();
            }
            if (vector.getBlockX() > xMax) {
                xMax = vector.getBlockX();
            }

            if (vector.getBlockY() < yMin) {
                yMin = vector.getBlockY();
            }
            if (vector.getBlockY() > yMax) {
                yMax = vector.getBlockY();
            }

            if (vector.getBlockZ() < zMin) {
                zMin = vector.getBlockZ();
            }
            if (vector.getBlockZ() > zMax) {
                zMax = vector.getBlockZ();
            }
        }
        min = new BlockVector(xMin, yMin, zMin);
        max = new BlockVector(xMax, yMax, zMax);
    }


    public ArrayList<Block> getWallBlocks() {
        updateMinMax();
        ArrayList<Block> blocklist = new ArrayList<>();
        World w = getWorld();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                blocklist.add(w.getBlockAt(x, y, min.getBlockZ()));
                blocklist.add(w.getBlockAt(x, y, max.getBlockZ()));
            }
        }

        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                blocklist.add(w.getBlockAt(min.getBlockX(), y, z));
                blocklist.add(w.getBlockAt(max.getBlockX(), y, z));
            }
        }
        return blocklist;
    }

    public void setWall(BlockData m) {
        getWallBlocks().forEach(b -> {
            BlockState bs = b.getState();
            bs.setBlockData(m);
            bs.update(true);
        });
    }

    public void setWall(Material m) {
        setWall(m.createBlockData());
    }

    public ArrayList<Block> getCuboidBlocks() {
        updateMinMax();
        ArrayList<Block> blocklist = new ArrayList<>();
        World w = getWorld();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    if (contains(new BlockVector(x, y, z))) {
                        blocklist.add(w.getBlockAt(x, y, z));
                    }
                }
            }
        }
        return blocklist;
    }

    public ArrayList<Block> getBlocks(BlockData m) {
        ArrayList<Block> blocklist = new ArrayList<>();
        getCuboidBlocks().forEach(b -> {
            if (m == null || b.getBlockData().equals(m)) blocklist.add(b);
        });
        return blocklist;
    }

    public ArrayList<Block> getBlocks(Material m) {
        ArrayList<Block> blocklist = new ArrayList<>();
        getCuboidBlocks().forEach(b -> {
            if (m == null || b.getType() == m) blocklist.add(b);
        });
        return blocklist;
    }

    public void replaceBlock(BlockData from, BlockData to) {
        getBlocks(from).forEach(b -> {
            BlockState bs = b.getState();
            bs.setBlockData(to);
            bs.update(true);
        });
    }

    public void replaceBlock(Material from, Material to) {
        getBlocks(from).forEach(b -> {
            BlockState bs = b.getState();
            bs.setType(to);
            bs.update(true);
        });
    }

    public void setBlock(BlockData m) {
        replaceBlock(null, m);
    }

    public void setBlock(Material m) {
        replaceBlock(null, m);
    }

    public void setAir() {
        setBlock(Material.AIR);
    }

    public int getCount() {
        return getCuboidBlocks().size();
    }

    public int getCount(BlockData m) {
        return getBlocks(m).size();
    }

    public int getCount(Material m) {
        return getBlocks(m).size();
    }

    public int getCount(Material... m) {
        int count = 0;
        final ArrayList<Block> blocks = getCuboidBlocks();
        for (Material mSingle : m) {
            count += blocks.stream().filter(block -> block.getType().equals(mSingle)).count();
        }
        return count;
    }

    public abstract String getTypeID();

    public int getArea() {
        return (int) ((max.getX() - min.getX() + 1) *
                (max.getY() - min.getY() + 1) *
                (max.getZ() - min.getZ() + 1));
    }
}
