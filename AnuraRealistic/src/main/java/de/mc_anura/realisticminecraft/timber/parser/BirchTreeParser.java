package de.mc_anura.realisticminecraft.timber.parser;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class BirchTreeParser extends TreeParser {

    protected BirchTreeParser(@NotNull Block first) {
        super(first);
        wood_tag = Tag.BIRCH_LOGS;
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
