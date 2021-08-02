package de.mc_anura.realisticminecraft.timber.parser;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class JungleTreeParser extends TreeParser {

    protected JungleTreeParser(Block first) {
        super(first);
        wood = Material.JUNGLE_LOG;
        leave = Material.JUNGLE_LEAVES;
    }

    @Override
    protected int getMaxLeavesDistance() {
        return 2;
    }

    @Override
    protected int getMaxLogDistance() {
        return 0;
    }

    @Override
    protected int getMaxTrunkSize() {
        return 0;
    }
}
