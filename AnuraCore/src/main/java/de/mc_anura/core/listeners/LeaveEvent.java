package de.mc_anura.core.listeners;

import de.mc_anura.core.events.AnuraLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class LeaveEvent implements Listener {

    private final Set<Player> hasLeft = Collections.newSetFromMap(new WeakHashMap<>());

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        //event.setQuitMessage(null);
        disconnect(event.getPlayer());
    }

    @EventHandler
    @SuppressWarnings("null")
    public void onKick(PlayerKickEvent event) {
        //event.setLeaveMessage(null);
        // TODO: Implement better join/leave msgs
        disconnect(event.getPlayer());
    }

    private void disconnect(Player P) {
        if (hasLeft.contains(P)) return;
        hasLeft.add(P);
        AnuraLeaveEvent ev = new AnuraLeaveEvent(P);
        Bukkit.getPluginManager().callEvent(ev);
    }
}
