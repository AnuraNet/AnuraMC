package de.mc_anura.realisticminecraft.timber.parser;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class SpruceTreeParser extends TreeParser {

    SpruceTreeParser(Block first) {
        super(first);
        wood = Material.SPRUCE_LOG;
        leave = Material.SPRUCE_LEAVES;
    }

    @Override
    protected int getMaxLeavesDistance() {
        return 3;
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
