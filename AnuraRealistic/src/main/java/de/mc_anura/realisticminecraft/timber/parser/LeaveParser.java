
package de.mc_anura.realisticminecraft.timber.parser;

import java.util.HashSet;
import org.bukkit.block.Block;

public class LeaveParser {

    private final Block firstBlock;
    private final TreeParser tp;
    private final HashSet<Block> leaveList = new HashSet<>();

    protected LeaveParser(TreeParser tp, Block firstBlock) {
        this.tp = tp;
        this.firstBlock = firstBlock;
    }
    
    protected HashSet<Block> parse() {
        tp.getDirsLeaves().stream()
                .map((v) -> firstBlock.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                .forEach(this::parseLeaves);
        return leaveList;
    }
    
    private void parseLeaves(Block b) {
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
    
    private boolean isLeaves(Block b) {
        return b.getType().equals(tp.leave);
    }
}
