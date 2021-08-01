package de.mc_anura.freebuild.events;

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

public class LightningStrike implements Listener {

    @EventHandler
    public void onLightning(LightningStrikeEvent event) {
        Location l = event.getLightning().getLocation();
        l.getWorld().spawn(l.clone().subtract(0, 1, 0), TNTPrimed.class).setFuseTicks(1);
        l.getWorld().createExplosion(l.getBlockX(), l.getBlockY(), l.getBlockZ(), 6, true, false);
    }
}
