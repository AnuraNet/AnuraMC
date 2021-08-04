package de.mc_anura.realisticminecraft.timber.parser;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class SpruceTreeParser extends TreeParser {

    SpruceTreeParser(@NotNull Block first) {
        super(first);
        wood_tag = Tag.SPRUCE_LOGS;
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
