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
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Chairs implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent e) {
        if (e.getBlock().getBlockData() instanceof Stairs && ChairManager.isSittingAnyone(e.getBlock())) {
            ChairManager.playerStandUp(e.getBlock());
        }
    }

    @EventHandler
    public void onDismount(@NotNull EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof ArmorStand) {
            if (ChairManager.isSitting(p)) {
                ChairManager.playerStandUp(p);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(@NotNull PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (ChairManager.isDisabled(p)) {
            return;
        }
        EquipmentSlot eq = e.getHand();
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !p.isSneaking() && eq != null && eq.equals(EquipmentSlot.HAND)) {
            final Block b = e.getClickedBlock();
            if (b != null && b.getBlockData() instanceof Stairs s) {
                if (s.getHalf().equals(Half.BOTTOM)) {
                    e.setCancelled(true);
                    Material above = b.getLocation().add(0, 1, 0).getBlock().getType();
                    if (above.isSolid() && !ChairManager.allowedAbove(above)) {
                        Msg.send(p, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Da ist kein Platz zum Sitzen!");
                    } else if (ChairManager.isSittingAnyone(b)) {
                        if (!ChairManager.isSittingPlayer(b, p)) {
                            Msg.send(p, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Hier sitzt bereits jemand!");
                        }
                    } else if (ChairManager.isSitting(p)) {
                        ChairManager.changeSeat(p, s, b);
                    } else {
                        ChairManager.playerSitDown(p, s, b);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(@NotNull AnuraLeaveEvent e) {
        if (ChairManager.isSitting(e.getPlayer())) {
            ChairManager.playerStandUp(e.getPlayer());
        }
    }
}
