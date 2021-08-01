package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.AnuraFreebuild;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class BedEvents implements Listener {

    private final Set<Player> sleeping = Collections.newSetFromMap(new WeakHashMap<>());
    private final double percentage = 0.4;

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player P = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(AnuraFreebuild.getInstance(), () -> {
            if (P.isSleeping()) {
                sleeping.add(P);
                checkSleeping();
            }
        }, 20 * 5);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        sleeping.remove(event.getPlayer());
    }

    private void checkSleeping() {
        sleeping.removeIf(P -> !P.isOnline());

        double user_count = Bukkit.getOnlinePlayers().size();
        if (user_count == 0) return;
        if (sleeping.size() / user_count >= percentage) {
            sleeping.iterator().next().getWorld().setTime(0);
        }
    }
}
