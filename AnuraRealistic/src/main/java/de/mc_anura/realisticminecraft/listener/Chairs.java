package de.mc_anura.realisticminecraft.listener;

import de.mc_anura.core.events.AnuraLeaveEvent;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.util.ChairManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Chairs implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getBlockData() instanceof Stairs && ChairManager.isSittingAnyone(e.getBlock())) {
            ChairManager.playerStandUp(e.getBlock());
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof ArmorStand) {
            if (ChairManager.isSitting(p)) {
                ChairManager.playerStandUp(p);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        EquipmentSlot eq = e.getHand();
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !p.isSneaking() && eq != null && eq.equals(EquipmentSlot.HAND) && !p.getName().equals("muxe666")) {
            final Block b = e.getClickedBlock();
            if (b != null && b.getBlockData() instanceof Stairs s) {
                if (s.getHalf().equals(Half.BOTTOM)) {
                    e.setCancelled(true);
                    Material above = b.getLocation().add(0, 1, 0).getBlock().getType();
                    if (!cloudAbove(above) && above.isSolid()) {
                        Msg.send(p, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Da ist kein Platz zum Sitzen!");
                    } else if (ChairManager.isSittingAnyone(b)) {
                        if (!ChairManager.isSittingPlayer(b, p)) {
                            Msg.send(p, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Hier sitzt bereits jemand!");
                        }
                    } else if (ChairManager.isSitting(p)) {
                        ChairManager.playerStandUp(p);
                        ChairManager.playerSitDown(p, s, b);
                    } else {
                        ChairManager.playerSitDown(p, s, b);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(AnuraLeaveEvent e) {
        if (ChairManager.isSitting(e.getPlayer())) {
            ChairManager.playerStandUp(e.getPlayer());
        }
    }

    private static boolean cloudAbove(Material m) {
        return switch (m) {
            case BLACK_WALL_BANNER, BLUE_WALL_BANNER, BROWN_WALL_BANNER, CYAN_WALL_BANNER, GRAY_WALL_BANNER, GREEN_WALL_BANNER, LIGHT_BLUE_WALL_BANNER, LIGHT_GRAY_WALL_BANNER, LIME_WALL_BANNER, MAGENTA_WALL_BANNER, ORANGE_WALL_BANNER, PINK_WALL_BANNER, PURPLE_WALL_BANNER, RED_WALL_BANNER, WHITE_WALL_BANNER, YELLOW_WALL_BANNER, ACACIA_WALL_SIGN, BIRCH_WALL_SIGN, DARK_OAK_WALL_SIGN, JUNGLE_WALL_SIGN, OAK_WALL_SIGN, SPRUCE_WALL_SIGN -> true;
            default -> false;
        };
    }
}
