package de.mc_anura.freebuild.events;

import de.mc_anura.freebuild.ClaimManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player P = event.getPlayer();
        if (ClaimManager.isClaiming(P)) {
            ClaimManager.endClaimMode(P);
        }
    }
}
