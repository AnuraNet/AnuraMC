package de.mc_anura.realisticminecraft.timber;

import de.mc_anura.core.AnuraThread;
import java.util.Random;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.timber.event.AppleDropEvent;
import de.mc_anura.realisticminecraft.timber.event.TreeCutEvent;
//import de.mc_anura.realisticminecraft.util.LogBlockProvider;
import de.mc_anura.realisticminecraft.util.TimberUtil;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TreeFeller {

    private final Tree t;
    private final Player p;
    private TreeCutEvent treeCutEvent;

    public TreeFeller(Player p, Tree t) {
        this.p = p;
        this.t = t;
    }

    public void cut(ItemStack axe) {
        treeCutEvent = new TreeCutEvent(p, t.getLowestLog());
        Bukkit.getPluginManager().callEvent(treeCutEvent);
        if (treeCutEvent.isCancelled()) {
            return;
        }
        TimberUtil.calculateDamage(axe, 5);
        p.getInventory().setItemInMainHand(axe);
        World w = t.getLowestLogLoc().getWorld();
        if (w != null) {
            w.playSound(t.getLowestLogLoc(), Sound.BLOCK_GRASS_BREAK, 1, 0.1f);
        }
        for (int i = 0; i <= t.getHeight(); i++) {
            cutLog(i);
            cutLeaves(i);
        }
    }

    private void cutLog(int stage) {
        BlockData log = getLogOrientation();
        t.getLogs().stream().filter(b -> b.getLocation().getBlockY() == t.getLowestLogLoc().getBlockY() + stage).forEach(b -> AnuraThread.queueSync(() -> {
            queueBlockBreak(p, b);
            b.setType(Material.AIR);
            Location pLocation = getPossibleLocation(b, stage);
            FallingBlock spawnFallingBlock = b.getWorld().spawnFallingBlock(pLocation.add(0.5, 0, 0.5), log);
            spawnFallingBlock.setDropItem(false);
            queueFallingBlock(p, pLocation, log);
        }));
    }

    private void cutLeaves(int stage) {
        BlockData ld = getLeavesData();
        Random r = new Random();
        short appleCount = 0;
        for (Block b : t.getLeaves()) {
            if (b.getLocation().getBlockY() == t.getLowestLogLoc().getBlockY() + stage) {
                if (r.nextBoolean()) {
                    ItemStack is;
                    if (treeCutEvent.getSaplingDropChance() > 0 && r.nextInt(treeCutEvent.getSaplingDropChance()) == 0) {
                        is = new ItemStack(getSaplingMaterial());
                    } else if (treeCutEvent.getAppleDropChance() > 0 && r.nextInt(treeCutEvent.getAppleDropChance()) == 0) {
                        is = new ItemStack(Material.APPLE);
                        appleCount++;
                    } else if (treeCutEvent.getLeavesDropChance() > 0 && r.nextInt(treeCutEvent.getLeavesDropChance()) == 0) {
                        is = new ItemStack(t.getLeave());
                    } else {
                        AnuraThread.queueSync(() -> {
                            queueBlockBreak(p, b);
                            b.setType(Material.AIR);
                        });
                        continue;
                    }
                    AnuraThread.queueSync(() -> {
                        b.getWorld().dropItemNaturally(getPossibleLocation(b, stage).add(0.5, 0, 0.5), is);
                        queueBlockBreak(p, b);
                        b.setType(Material.AIR);
                    });
                } else {
                    AnuraThread.queueSync(() -> {
                        queueBlockBreak(p, b);
                        b.setType(Material.AIR);
                        Location pLocation = getPossibleLocation(b, stage);
                        b.getWorld().spawnFallingBlock(pLocation.add(0.5, 0, 0.5), ld);
                        queueFallingBlock(p, pLocation, ld);
                    });
                }
            }
        }
        if (appleCount > 0) {
            Bukkit.getPluginManager().callEvent(new AppleDropEvent(p, appleCount));
        }
    }

    private BlockData getLogOrientation() {
        Orientable o = (Orientable) t.getWood().createBlockData();
        o.setAxis(convertDirection(t.getFallDirection()));
        return o;
    }

    private BlockData getLeavesData() {
        Leaves l = (Leaves) t.getLeave().createBlockData();
        l.setPersistent(true);//@TODO: Check if needed
        return l;
    }

    private Material getSaplingMaterial() {
        return switch (t.getWood()) {
            case BIRCH_LOG -> Material.BIRCH_SAPLING;
            case SPRUCE_LOG -> Material.SPRUCE_SAPLING;
            case JUNGLE_LOG -> Material.JUNGLE_SAPLING;
            case ACACIA_LOG -> Material.ACACIA_SAPLING;
            case DARK_OAK_LOG -> Material.DARK_OAK_SAPLING;
            default -> Material.OAK_SAPLING;
        };
    }

    private Location getPossibleLocation(Block start, int stage) {
        if (stage != 0) {
            for (int i = 1; i <= stage; i++) {
                Block b = start.getRelative(t.getFallDirection(), i);
                if (b.getType().isSolid() && !t.getLogs().contains(b) && !t.getLeaves().contains(b)) {
                    return start.getRelative(t.getFallDirection(), i - 1).getLocation();
                }
            }
            return start.getRelative(t.getFallDirection(), stage).getLocation();
        }
        return start.getLocation();
    }

    private static Axis convertDirection(BlockFace bf) {
        return switch (bf) {
            case NORTH, SOUTH -> Axis.Z;
            default -> Axis.X;
        };
    }

    public static void queueBlockBreak(Player p, Block b) {
        if (RealisticMinecraft.hasLogBlock()) {
//            LogBlockProvider.queueBlockBreak(p, b);
        }
    }

    public static void queueFallingBlock(Player p, Location l, BlockData bd) {
        if (RealisticMinecraft.hasLogBlock()) {
//            LogBlockProvider.queueFalling(p, l, bd);
        }
    }
}
