package de.mc_anura.realisticminecraft.timber.parser;

import de.mc_anura.realisticminecraft.listener.Timber;
import de.mc_anura.realisticminecraft.timber.Tree;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public abstract class TreeParser {

    private final HashSet<Block> logList = new HashSet<>();
    private final HashSet<Block> leaveList = new HashSet<>();
    private final Block firstBlock;
    protected Tag<Material> wood_tag;
    protected Material wood;
    protected Material leave;

    protected TreeParser(@NotNull Block first) {
        firstBlock = first;
    }

    public @NotNull Tree parse(@Nullable BlockFace bf) {
        parseTrunk(firstBlock);
        if (getMaxTrunkSize() > 0) {
            getDirsStem().stream().map((v) -> firstBlock.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                    .forEach(this::parseTrunk);
        }
        new HashSet<>(logList).forEach((b) -> logList.addAll(new LogParser(this, b).parse()));
        logList.forEach((b) -> leaveList.addAll(new LeaveParser(this, b).parse()));
        return new Tree(firstBlock, logList, leaveList, bf, wood, leave);
    }

    private void parseTrunk(@NotNull Block b) {
        if (logList.size() > 1 && logList.contains(b) || TreeParser.isDistanceBiggerThan(firstBlock.getLocation(), b.getLocation(), getMaxTrunkSize())) {
            return;
        }
        if (isWood(b)) {
            logList.add(b);
        } else {
            return;
        }
        parseTrunk(b.getRelative(BlockFace.UP));
    }

    private boolean isWood(@NotNull Block b) {
        return wood_tag.isTagged(b.getType());
    }

    protected @NotNull Collection<Vector> getDirsLog() {
        return Collections.emptySet();
    }

    protected @NotNull Collection<Vector> getDirsStem() {
        return Arrays.asList(new Vector(0, 0, -1), new Vector(1, 0, 0), new Vector(0, 0, 1), new Vector(-1, 0, 0),
                new Vector(1, 0, 1), new Vector(-1, 0, 1), new Vector(-1, 0, -1), new Vector(1, 0, -1));
    }

    protected @NotNull Collection<Vector> getDirsLeaves() {
        return Arrays.asList(new Vector(0, 1, 0), new Vector(0, 0, -1), new Vector(1, 0, 0), new Vector(0, 0, 1),
                new Vector(-1, 0, 0));
    }

    protected abstract int getMaxLeavesDistance();

    protected abstract int getMaxLogDistance();

    protected abstract int getMaxTrunkSize();

    protected static boolean isDistanceBiggerThan(@NotNull Location l1, @NotNull Location l2, int distance) {
        int loc1 = l1.getBlockX();
        int loc2 = l2.getBlockX();
        int loc3 = l1.getBlockZ();
        int loc4 = l2.getBlockZ();
        return (Math.max(loc1, loc2) - Math.min(loc1, loc2)) > distance || (Math.max(loc3, loc4) - Math.min(loc3, loc4)) > distance;
    }

    public static @Nullable TreeParser newTreeParser(@NotNull Block b) {
        if (!Timber.isTimberLog(b.getType())) {
            return null;
        }
        boolean fourLog = false;
        for (Vector v : Arrays.asList(new Vector(0, 0, -1), new Vector(1, 0, 0), new Vector(0, 0, 1), new Vector(-1, 0, 0))) {
            if (b.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()).getType().equals(b.getType())) {
                fourLog = true;
                break;
            }
        }
        switch (b.getType()) {
            case DARK_OAK_LOG:
                return new DarkOakTreeParser(b);
            case ACACIA_LOG:
                return new AcaciaTreeParser(b);
            case JUNGLE_LOG:
                if (fourLog) {
                    return new BigJungleTreeParser(b);
                }
                return new JungleTreeParser(b);
            case SPRUCE_LOG:
                if (fourLog) {
                    return new BigSpruceTreeParser(b);
                }
                return new SpruceTreeParser(b);
            case BIRCH_LOG:
                if (fourLog) {
                    return new BigBirchTreeParser(b);
                }
                return new BirchTreeParser(b);
            case OAK_LOG:
                if (fourLog) {
                    return new BigOakTreeParser(b);
                }
                return new OakTreeParser(b);
            default:
                return null;
        }
    }
}
