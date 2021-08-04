package de.mc_anura.realisticminecraft.timber.parser;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class LeaveParser {

    private final Block firstBlock;
    private final TreeParser tp;
    private final HashSet<Block> leaveList = new HashSet<>();

    protected LeaveParser(@NotNull TreeParser tp, @NotNull Block firstBlock) {
        this.tp = tp;
        this.firstBlock = firstBlock;
    }

    protected HashSet<Block> parse() {
        tp.getDirsLeaves().stream()
                .map((v) -> firstBlock.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                .forEach(this::parseLeaves);
        return leaveList;
    }

    private void parseLeaves(@NotNull Block b) {
        if (leaveList.contains(b) ||
                TreeParser.isDistanceBiggerThan(firstBlock.getLocation(), b.getLocation(), tp.getMaxLeavesDistance())) {
            return;
        }
        if (isLeaves(b)) {
            leaveList.add(b);
        } else {
            return;
        }
        tp.getDirsLeaves().stream().map((v) -> b.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                .filter((r) -> (r.getLocation().getBlockY() >= firstBlock.getLocation().getBlockY()))
                .forEach(this::parseLeaves);
    }

    private boolean isLeaves(@NotNull Block b) {
        return b.getType().equals(tp.leave);
    }
}
