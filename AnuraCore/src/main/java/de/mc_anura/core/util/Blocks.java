package de.mc_anura.core.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public abstract class Blocks {

    public static Block getMin(Block block1, Block block2) {
        checkWorld(block1, block2);
        int x = Math.min(block1.getX(), block2.getX());
        int y = Math.min(block1.getY(), block2.getY());
        int z = Math.min(block1.getZ(), block2.getZ());
        return block1.getWorld().getBlockAt(x, y, z);
    }

    public static Block getMax(Block block1, Block block2) {
        checkWorld(block1, block2);
        int x = Math.max(block1.getX(), block2.getX());
        int y = Math.max(block1.getY(), block2.getY());
        int z = Math.max(block1.getZ(), block2.getZ());
        return block1.getWorld().getBlockAt(x, y, z);
    }

    public static Block center(Block block1, Block block2) {
        checkWorld(block1, block2);
        Block min = getMin(block1, block2);
        Block max = getMax(block1, block2);
        int x = min.getX() + Math.round(max.getX() - min.getX() / 2);
        int y = min.getY() + Math.round(max.getY() - min.getY() / 2);
        int z = min.getZ() + Math.round(max.getZ() - min.getZ() / 2);
        return block1.getWorld().getBlockAt(x, y, z);
    }

    private static void checkWorld(Block block1, Block block2) {
        if (!block1.getWorld().equals(block2.getWorld()))
            throw new IllegalArgumentException("The blocks have to be in the same world");
    }

    public static float faceToYaw(BlockFace face) {
        return switch (face) {
            case NORTH -> 0;
            case EAST -> 90;
            case WEST -> 270;
            default -> 180;
        };
    }

    public static boolean blockEquals(Location l1, Location l2) {
        return l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
    }
}
