package de.mc_anura.freebuild.events;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class ElytraEvents implements Listener {

    private static final Set<Player> glidingPlayers = Collections.newSetFromMap(new WeakHashMap<>());

    private static final double Y = 87;
    private static final double X1 = -40;//MIN
    private static final double X2 = -10;//MAX
    private static final double Z1 = 180;//MIN
    private static final double Z2 = 208;//MAX

    @EventHandler(ignoreCancelled = true)
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.isFlying() || p.getVehicle() != null || p.isGliding() || p.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        Location from = e.getFrom();
        Location to = e.getTo();
        if (from.getY() > Y && to.getY() <= Y && to.getX() >= X1 && to.getX() <= X2
                && to.getZ() >= Z1 && to.getZ() <= Z2) {
            Block b1 = to.getBlock().getRelative(BlockFace.DOWN);
            Block b2 = b1.getRelative(BlockFace.DOWN);
            if (b1.isEmpty() && b2.isEmpty()) {
                glidingPlayers.add(p);
                p.setGliding(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleGlide(@NotNull EntityToggleGlideEvent e) {
        if (e.isGliding()) {
            return;
        }
        if (e.getEntity() instanceof Player p) {
            if (p.isFlying()) {
                return;
            }
            if (glidingPlayers.contains(p)) {
                if (!p.isOnGround()) {
                    e.setCancelled(true);
                } else {
                    glidingPlayers.remove(p);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMount(@NotNull EntityMountEvent e) {
        if (e.getEntity() instanceof Player p) {
            p.setGliding(false);
        }
    }
}
