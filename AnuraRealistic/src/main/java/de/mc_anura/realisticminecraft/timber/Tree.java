package de.mc_anura.realisticminecraft.timber;

import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class Tree {

    private final HashSet<Block> logList;
    private final HashSet<Block> leaveList;
    private final Block lowestLog;
    private Block highestBlock;
    private final BlockFace direction;
    private final Material wood;
    private final Material leave;
    private final HashSet<UUID> owners = new HashSet<>();

    public Tree(@NotNull Block first, @NotNull HashSet<Block> logs, @NotNull HashSet<Block> leaves, @Nullable BlockFace fallDirection, @NotNull Material wood, @NotNull Material leave) {
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

    public @NotNull HashSet<Block> getLogs() {
        return logList;
    }

    public @NotNull HashSet<Block> getLeaves() {
        return leaveList;
    }

    public int getHeight() {
        return highestBlock.getLocation().getBlockY() - lowestLog.getLocation().getBlockY();
    }

    public @NotNull Block getLowestLog() {
        return lowestLog;
    }

    public @NotNull Location getLowestLogLoc() {
        return lowestLog.getLocation();
    }

    public @NotNull BlockFace getFallDirection() {
        return direction;
    }

    public boolean isAbleToCut() {
        return leaveList.size() > 5;
    }

    public @NotNull Material getWood() {
        return wood;
    }

    public @NotNull Material getLeave() {
        return leave;
    }

    public @NotNull HashSet<UUID> getOwners() {
        return owners;
    }

    private static @Nullable UUID getBlockOwner(@NotNull Block b) {
        return RegionManager.getOwner(b.getLocation());
    }
}
