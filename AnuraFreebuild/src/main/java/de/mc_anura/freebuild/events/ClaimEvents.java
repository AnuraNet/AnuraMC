package de.mc_anura.freebuild.events;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.freebuild.AnuraFreebuild;
import de.mc_anura.freebuild.ClaimManager;
import jdk.jfr.Enabled;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ClaimEvents implements Listener {

    private static final int MAX_DISTANCE_SQ = 100*100;

    public static Map<Player, Integer> dropped = new WeakHashMap<>();

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player P = event.getPlayer();
        if (inClaim(P)) {
            dropped.put(P, Bukkit.getCurrentTick());
            event.setCancelled(true);
            ClaimManager.qButton(P);
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        event.setCancelled(inClaim(event.getPlayer()));
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        event.setCancelled(inClaim(event.getPlayer()));
    }

    @EventHandler
    public void onInvInteract(InventoryClickEvent event) {
        event.setCancelled(inClaim((Player) event.getWhoClicked()));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        event.setCancelled(inClaim((Player) event.getEntity()));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location old = ClaimManager.getOldLocation(event.getPlayer());
        if (old != null && event.getTo().distanceSquared(old) > MAX_DISTANCE_SQ) {
            event.setCancelled(true);
            Msg.send(event.getPlayer(), AnuraFreebuild.getInstance(), Msg.MsgType.ERROR, "Du bist zu weit entfernt!");
        }
    }

    private boolean inClaim(Player P) {
        return ClaimManager.isClaiming(P);
    }
}
