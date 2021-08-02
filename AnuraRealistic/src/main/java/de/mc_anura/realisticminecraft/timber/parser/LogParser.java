
package de.mc_anura.realisticminecraft.timber.parser;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;

public class LogParser {

    private final Block firstBlock;
    private final TreeParser tp;
    private final HashSet<Block> logList = new HashSet<>();

    protected LogParser(TreeParser tp, Block firstBlock) {
        this.tp = tp;
        this.firstBlock = firstBlock;
    }
    
    protected HashSet<Block> parse() {
        tp.getDirsLog().stream()
                .map((v) -> firstBlock.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                .forEach(this::parseLog);
        return logList;
    }
    
    private void parseLog(Block b) {
        if (logList.contains(b) || 
                TreeParser.isDistanceBiggerThan(firstBlock.getLocation(), b.getLocation(), tp.getMaxLogDistance())) {
            return;
        }
        if (isWood(b)) {
            logList.add(b);
        } else {
            return;
        }
        tp.getDirsLog().stream().map((v) -> b.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                .forEach(this::parseLog);
    }
    
    private boolean isWood(Block b) {
        try {
            return b.getType().equals(tp.wood);
        } catch (ConcurrentModificationException ex) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex1) {
                Logger.getLogger(LogParser.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return isWood(b);
        }
    }
}
