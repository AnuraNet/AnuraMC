package de.mc_anura.realisticminecraft.timber.parser;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class OakTreeParser extends TreeParser {

    protected OakTreeParser(@NotNull Block first) {
        super(first);
        wood_tag = Tag.OAK_LOGS;
        wood = Material.OAK_LOG;
        leave = Material.OAK_LEAVES;
    }

    @Override
    protected int getMaxLeavesDistance() {
        return 3;
    }

    @Override
    protected int getMaxLogDistance() {
        return 5;
    }

    @Override
    protected int getMaxTrunkSize() {
        return 1;
    }

    @Override
    protected @NotNull Collection<Vector> getDirsLog() {
        return Sets.newHashSet(new Vector(0, 0, -1), new Vector(1, 0, 0), new Vector(0, 0, 1), new Vector(-1, 0, 0),
                new Vector(1, 1, 1), new Vector(-1, 1, -1), new Vector(-1, 1, 1), new Vector(1, 1, -1),
                new Vector(1, 0, 1), new Vector(-1, 0, -1), new Vector(-1, 0, 1), new Vector(1, 0, -1),
                new Vector(1, 1, 0), new Vector(-1, 1, 0), new Vector(0, 1, 1), new Vector(0, 1, -1));
    }
}
