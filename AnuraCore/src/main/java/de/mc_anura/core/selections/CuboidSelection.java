package de.mc_anura.core.selections;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

public class CuboidSelection extends Selection {

    public CuboidSelection(Location corner1, Location corner2) {
        locations.add(corner1);
        locations.add(corner2);
    }

    public CuboidSelection(Block middleBlock, int radius) {
        Validate.notNull(middleBlock);
        Block b1 = middleBlock.getRelative(BlockFace.DOWN, radius).getRelative(BlockFace.NORTH_WEST, radius);
        Block b2 = middleBlock.getRelative(BlockFace.UP, radius).getRelative(BlockFace.SOUTH_EAST, radius);
        locations.add(b1.getLocation());
        locations.add(b2.getLocation());
    }

    public Location getMinCorner() {
        updateMinMax();
        return min.toLocation(getWorld());
    }

    public Location getMaxCorner() {
        updateMinMax();
        return max.toLocation(getWorld());
    }

    public void setCorner1(Location b) {
        locations.set(0, b);
    }

    public void setCorner2(Location b) {
        locations.set(1, b);
    }

    @Override
    public String getTypeID() {
        return "cuboid";
    }

    @Override
    public boolean expand(int size, BlockFace bf) {
        Location minLoc = getMinCorner(), maxLoc = getMaxCorner();
        switch (bf) {
            case DOWN:
                minLoc.setY(minLoc.getY() - size);
                break;

            case NORTH:
                minLoc.setX(minLoc.getX() - size);
                break;

            case EAST:
                minLoc.setZ(minLoc.getZ() + size);
                break;

            case UP:
                maxLoc.setY(maxLoc.getY() + size);
                break;

            case SOUTH:
                maxLoc.setX(maxLoc.getX() + size);
                break;

            case WEST:
                maxLoc.setZ(maxLoc.getZ() + size);
                break;

            default:
                return false;
        }
        setCorner1(minLoc);
        setCorner2(maxLoc);
        return true;
    }

    @Override
    public boolean contains(BlockVector bv) {
        updateMinMax();
        int X = bv.getBlockX();
        int Y = bv.getBlockY();
        int Z = bv.getBlockZ();
        if ((X >= min.getX())
                && (X <= max.getX())
                && (Y >= min.getY())
                && (Y <= max.getY())
                && (Z >= min.getZ())
                && (Z <= max.getZ())) {
            return true;
        }
        return false;
    }
}
