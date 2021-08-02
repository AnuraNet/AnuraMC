package de.mc_anura.realisticminecraft.listener;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.realisticminecraft.timber.Tree;
import java.util.Map;
import java.util.WeakHashMap;
import de.mc_anura.realisticminecraft.timber.TreeFeller;
import de.mc_anura.realisticminecraft.timber.parser.TreeParser;
import de.mc_anura.realisticminecraft.util.TimberUtil;
import java.util.Collections;
import java.util.Set;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class Timber implements Listener {

    private static final Map<Player, BlockFace> LAST_INTERACT = new WeakHashMap<>();
    private static final Set<BlockBreakEvent> IGNORE_EVENTS = Collections.newSetFromMap(new WeakHashMap<>());

    public static BlockFace getLastInteract(Player p) {
        return LAST_INTERACT.get(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (IGNORE_EVENTS.contains(e)) {
            return;
        }
        if (isTimberLog(e.getBlock().getType())) {
            EntityEquipment eq = e.getPlayer().getEquipment();
            if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                    && eq != null && TimberUtil.isAxe(eq.getItemInMainHand().getType())
                    && ((Orientable) e.getBlock().getBlockData()).getAxis().equals(Axis.Y)) {
                e.setCancelled(true);
                AnuraThread.async(() -> {
                    TreeParser treeParser = TreeParser.newTreeParser(e.getBlock());
                    Tree t = treeParser.parse(getLastInteract(e.getPlayer()));
                    if (!t.isAbleToCut() || t.getOwners().size() > 1 ||
                            (t.getOwners().size() == 1) != t.getOwners().contains(e.getPlayer().getUniqueId())) {
                        breakNaturally(e.getPlayer(), eq.getItemInMainHand(), t.getLowestLog());
                        return;
                    }
                    if ((t.getOwners().size() == 1) == e.getPlayer().isSneaking()) {
                        breakNaturally(e.getPlayer(), eq.getItemInMainHand(), t.getLowestLog());
                        return;
                    }
                    new TreeFeller(e.getPlayer(), t).cut(eq.getItemInMainHand());
                });
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
                return;
            }
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock != null && isTimberLog(clickedBlock.getType())) {
                LAST_INTERACT.put(p, e.getBlockFace());
            }
        }
    }

    public static void breakNaturally(Player p, ItemStack is, Block b) {
        AnuraThread.queueSync(() -> {
            BlockBreakEvent blockBreakEvent = new BlockBreakEvent(b, p);
            IGNORE_EVENTS.add(blockBreakEvent);
            Bukkit.getPluginManager().callEvent(blockBreakEvent);
            if (blockBreakEvent.isCancelled()) {
                return;
            }
            b.breakNaturally(is);
            TimberUtil.calculateDamage(is, 1);
            p.getInventory().setItemInMainHand(is);
        });
    }

    public static boolean isTimberLog(Material m) {
        return switch (m) {
            case ACACIA_LOG, BIRCH_LOG, DARK_OAK_LOG, JUNGLE_LOG, OAK_LOG, SPRUCE_LOG ->
                    //@TODO: Check BARK
                    true;
            default -> false;
        };
    }
}
