package de.mc_anura.realisticminecraft.timber;

import de.mc_anura.freebuild.regions.RegionManager;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Tree {

    private final HashSet<Block> logList;
    private final HashSet<Block> leaveList;
    private final Block lowestLog;
    private Block highestBlock;
    private final BlockFace direction;
    private final Material wood;
    private final Material leave;
    private final HashSet<UUID> owners = new HashSet<>();

    public Tree(Block first, HashSet<Block> logs, HashSet<Block> leaves, BlockFace fallDirection, Material wood, Material leave) {
        this.logList = logs;
        this.leaveList = leaves;
        this.lowestLog = first;
        this.wood = wood;
        this.leave = leave;
        if (fallDirection == null || fallDirection.equals(BlockFace.UP) || fallDirection.equals(BlockFace.DOWN)) {
            direction = BlockFace.values()[new Random().nextInt(4)];
        } else {
            direction = fallDirection.getOppositeFace();
        }
        Location loc = first.getLocation();
        loc.setY(0);
        highestBlock = loc.getBlock();
        logs.forEach((b) -> {
            if (b.getLocation().getY() > highestBlock.getLocation().getY()) {
                highestBlock = b;
            }
            UUID owner = getBlockOwner(b);
            if (owner != null) {
                owners.add(owner);
            }
        });
        leaves.forEach((b) -> {
            if (b.getLocation().getY() > highestBlock.getLocation().getY()) {
                highestBlock = b;
            }
            UUID owner = getBlockOwner(b);
            if (owner != null) {
                owners.add(owner);
            }
        });
    }

    public HashSet<Block> getLogs() {
        return logList;
    }

    public HashSet<Block> getLeaves() {
        return leaveList;
    }

    public int getHeight() {
        return highestBlock.getLocation().getBlockY() - lowestLog.getLocation().getBlockY();
    }

    public Block getLowestLog() {
        return lowestLog;
    }

    public Location getLowestLogLoc() {
        return lowestLog.getLocation();
    }

    public BlockFace getFallDirection() {
        return direction;
    }

    public boolean isAbleToCut() {
        return leaveList.size() > 5;
    }

    public Material getWood() {
        return wood;
    }

    public Material getLeave() {
        return leave;
    }

    public HashSet<UUID> getOwners() {
        return owners;
    }

    private static UUID getBlockOwner(Block b) {
        return RegionManager.getOwner(b.getLocation());
    }
}
