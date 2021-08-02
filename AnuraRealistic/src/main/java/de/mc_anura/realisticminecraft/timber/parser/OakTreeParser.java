package de.mc_anura.realisticminecraft.timber.parser;

import com.google.common.collect.Sets;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class OakTreeParser extends TreeParser {

    protected OakTreeParser(Block first) {
        super(first);
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
    protected Collection<Vector> getDirsLog() {
        return Sets.newHashSet(new Vector(0, 0, -1), new Vector(1, 0, 0), new Vector(0, 0, 1), new Vector(-1, 0, 0),
                new Vector(1, 1, 1), new Vector(-1, 1, -1), new Vector(-1, 1, 1), new Vector(1, 1, -1),
                new Vector(1, 0, 1), new Vector(-1, 0, -1), new Vector(-1, 0, 1), new Vector(1, 0, -1),
                new Vector(1, 1, 0), new Vector(-1, 1, 0), new Vector(0, 1, 1), new Vector(0, 1, -1));
    }
}
