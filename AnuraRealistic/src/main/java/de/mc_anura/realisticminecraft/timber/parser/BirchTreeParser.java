package de.mc_anura.realisticminecraft.timber.parser;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BirchTreeParser extends TreeParser {

    protected BirchTreeParser(Block first) {
        super(first);
        wood = Material.BIRCH_LOG;
        leave = Material.BIRCH_LEAVES;
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
