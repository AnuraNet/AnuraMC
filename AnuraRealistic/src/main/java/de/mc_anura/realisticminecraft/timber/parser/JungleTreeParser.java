package de.mc_anura.realisticminecraft.timber.parser;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class JungleTreeParser extends TreeParser {

    protected JungleTreeParser(@NotNull Block first) {
        super(first);
        wood_tag = Tag.JUNGLE_LOGS;
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
